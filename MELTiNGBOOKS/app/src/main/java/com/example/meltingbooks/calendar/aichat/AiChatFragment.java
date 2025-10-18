package com.example.meltingbooks.calendar.aichat;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.BuildConfig;
import com.example.meltingbooks.R;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.goal.GoalApi;
import com.example.meltingbooks.network.goal.GoalController;
import com.example.meltingbooks.network.goal.GoalResponse;
import com.example.meltingbooks.network.log.LogApi;
import com.example.meltingbooks.network.log.LogController;
import com.example.meltingbooks.network.log.ReadingLogResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class AiChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText etMessage;
    private Button btnSend;
    private ChatAdapter chatAdapter;
    private List<String> chatList;

    private OkHttpClient client;
    private String apiKey = BuildConfig.OPENAI_API_KEY;

    private GoalResponse latestGoal;
    private GoalResponse lastMonthGoal;
    private List<ReadingLogResponse> recentLogs = new ArrayList<>();

    private GoalController goalController;
    private LogController logController;

    private String token;
    private int userId;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_chat, container, false);

        // SharedPreferences에서 토큰, userId 가져오기
        SharedPreferences prefs = requireContext().getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("jwt", null);
        userId = prefs.getInt("userId", -1);

        recyclerView = view.findViewById(R.id.recyclerViewChat);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);

        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(chatAdapter);

        client = new OkHttpClient();

        // ✅ 처음 안내 메시지 표시
        showIntroMessage();

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                addMessageToChat("You: " + message);
                etMessage.setText("");
                callChatGPT(message);
            }
        });

        GoalApi apiService = ApiClient.getClient(token).create(GoalApi.class);
        goalController = new GoalController(apiService);

        // onCreateView()에서
        LogApi logApi = ApiClient.getClient(token).create(LogApi.class);
        logController = new LogController(logApi);

        loadUserData();

        return view;
    }

    private void addMessageToChat(String message) {
        chatList.add(message);
        chatAdapter.notifyItemInserted(chatList.size() - 1);
        recyclerView.scrollToPosition(chatList.size() - 1);
    }

    private void callChatGPT(String userMessage) {
        JSONObject object = new JSONObject();

        // 1️⃣ 유저 입력으로 프롬프트 타입 자동 감지
        String promptType = detectPromptType(userMessage);

        // 2️⃣ 시스템 메시지를 프롬프트 타입에 따라 변경
        String systemPrompt = getSystemPrompt(promptType);

        try {
            object.put("model", "gpt-3.5-turbo");
            JSONArray messagesArray = new JSONArray();


            // system 메시지
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messagesArray.put(systemMessage);

            // 이전 대화 추가
            for (String chat : chatList) {
                if (chat.contains("AI 독서 도우미")) continue; // intro 메시지는 제외
                JSONObject msgObj = new JSONObject();
                if (chat.startsWith("You: ")) {
                    msgObj.put("role", "user");
                    msgObj.put("content", chat.substring(5));
                } else if (chat.startsWith("AI: ")) {
                    msgObj.put("role", "assistant");
                    msgObj.put("content", chat.substring(4));
                }
                messagesArray.put(msgObj);
            }

            // 새 사용자 입력 추가
            JSONObject userObj = new JSONObject();
            userObj.put("role", "user");
            userObj.put("content", buildUserContextMessage(userMessage));
            messagesArray.put(userObj);

            object.put("messages", messagesArray);
            object.put("temperature", 0.7);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(object.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "ChatGPT 요청 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                if (getActivity() == null) return;

                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful() && !responseBody.isEmpty()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray choices = jsonResponse.getJSONArray("choices");
                        JSONObject messageObj = choices.getJSONObject(0).optJSONObject("message");
                        String reply = messageObj != null ? messageObj.optString("content", "응답 없음") : "응답 없음";

                        getActivity().runOnUiThread(() -> addMessageToChat("AI: " + reply));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "API 호출 오류", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private String detectPromptType(String input) {
        input = input.toLowerCase();

        if (input.contains("목표") || input.contains("추천")) {
            return "goal_recommendation";
        } else if (input.contains("요약") || input.contains("정리")) {
            return "summary";
        } else if (input.contains("동기") || input.contains("응원")) {
            return "motivation";
        } else {
            return "default";
        }
    }

    private String getSystemPrompt(String type) {
        switch (type) {
            case "goal_recommendation":
                return "You are a reading coach AI. Based on the user's current goals and reading progress data, recommend personalized reading goals or improvements.";
            case "summary":
                return "You are an assistant that summarizes the user's reading activity logs and gives insights or reflections based on them.";
            case "motivation":
                return "You are a motivational coach for readers. Use the user's recent reading records to give personalized encouragement.";
            default:
                return "You are a friendly book chat assistant that helps with reading discussions and questions.";
        }
    }

    private void showIntroMessage() {
        String intro = "💬 **AI 독서 도우미**\n\n"
                + "안녕하세요! 😊 저는 독서 활동을 도와주는 AI 챗봇이에요.\n\n"
                + "아래 키워드 중 하나로 대화해보세요 👇\n\n"
                + "📘 '목표 추천' — 다음 달 독서 목표를 추천받아요\n"
                + "🧾 '기록 요약' — 독서 기록을 간단히 요약해드려요\n"
                + "💪 '동기 부여' — 의욕을 잃었을 때 응원과 격려를 받아요\n"
                + "💬 '자유 대화' — 책이나 독서에 대해 아무거나 물어보세요!";

        addMessageToChat("AI: " + intro);
    }

    private void loadUserData() {
        goalController.getGoals(token, new GoalController.GoalCallback<List<GoalResponse>>() {
            @Override
            public void onSuccess(List<GoalResponse> goals) {
                if (goals != null && !goals.isEmpty()) {

                    // 📅 현재 연도/월 계산
                    java.util.Calendar calendar = java.util.Calendar.getInstance();
                    int currentYear = calendar.get(java.util.Calendar.YEAR);
                    int currentMonth = calendar.get(java.util.Calendar.MONTH) + 1; // 0부터 시작하므로 +1
                    int previousMonth = currentMonth - 1;
                    int previousYear = currentYear;

                    // 1월일 경우 → 이전 달은 작년 12월
                    if (previousMonth == 0) {
                        previousMonth = 12;
                        previousYear--;
                    }

                    GoalResponse currentGoal = null;
                    GoalResponse prevGoal = null;

                    // 🎯 목표 리스트에서 현재달 / 이전달 골 찾기
                    for (GoalResponse goal : goals) {
                        if ("MONTHLY".equalsIgnoreCase(goal.getGoalType())) {
                            if (goal.getYear() == currentYear && goal.getMonth() != null && goal.getMonth() == currentMonth) {
                                currentGoal = goal;
                            } else if (goal.getYear() == previousYear && goal.getMonth() != null && goal.getMonth() == previousMonth) {
                                prevGoal = goal;
                            }
                        }
                    }

                    latestGoal = currentGoal;
                    lastMonthGoal = prevGoal;

                    if (latestGoal != null) {
                        Log.d("AiChat", "이번 달 목표 로드 완료: " + latestGoal.getGoalType());
                    }
                    if (lastMonthGoal != null) {
                        Log.d("AiChat", "이전 달 목표 로드 완료: " + lastMonthGoal.getGoalType());
                    }
                } else {
                    Log.w("AiChat", "목표 데이터 없음");
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("AiChat", "Goal load error: " + errorMessage);
            }
        });

        // 📚 최근 일주일 독서 기록 불러오기
        String startDate = LocalDate.now().minusDays(7).toString();
        String endDate = LocalDate.now().toString();

        logController.getLogsByPeriod(token, userId, startDate, endDate,
                new retrofit2.Callback<ApiResponse<List<ReadingLogResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ReadingLogResponse>>> call,
                                           Response<ApiResponse<List<ReadingLogResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            recentLogs = response.body().getData();
                            Log.d("AiChat", "최근 독서 로그 불러오기 성공: " + recentLogs.size() + "개");
                        } else {
                            Log.w("AiChat", "독서 로그 응답 없음");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ReadingLogResponse>>> call, Throwable t) {
                        Log.e("AiChat", "Log fetch failed", t);
                    }
                });
    }


    private String buildUserContextMessage(String userMessage) {
        StringBuilder context = new StringBuilder();

        // 이전 달 목표
        if (lastMonthGoal != null) {
            context.append("이전 달 목표 달성 현황:\n")
                    .append(String.format("- 책 목표: %.1f%% 달성\n", lastMonthGoal.getBookProgress()))
                    .append(String.format("- 리뷰 목표: %.1f%% 달성\n", lastMonthGoal.getReviewProgress()))
                    .append(String.format("- 독서 시간 목표: %.1f%% 달성\n\n", lastMonthGoal.getTimeProgress()));
        }

        // 이번 달 목표
        if (latestGoal != null) {
            context.append("이번 달 목표 현황:\n")
                    .append(String.format("- 목표 책 수: %d권 중 %d권 (%.1f%%)\n",
                            latestGoal.getTargetBooks(),
                            latestGoal.getCompletedBooks(),
                            latestGoal.getBookProgress()))
                    .append(String.format("- 리뷰 작성: %d개 / %d개 (%.1f%%)\n",
                            latestGoal.getCompletedReviews(),
                            latestGoal.getTargetReviews(),
                            latestGoal.getReviewProgress()))
                    .append(String.format("- 독서 시간: %d분 / %d분 (%.1f%%)\n\n",
                            latestGoal.getCompletedMinutes(),
                            latestGoal.getTargetMinutes(),
                            latestGoal.getTimeProgress()));
        }

        // 최근 독서 기록
        if (recentLogs != null && !recentLogs.isEmpty()) {
            int totalPages = 0, totalMinutes = 0, finishedCount = 0;
            for (ReadingLogResponse log : recentLogs) {
                totalPages += log.getPagesRead();
                totalMinutes += log.getMinutesRead();
                if (log.isFinished()) finishedCount++;
            }
            context.append("최근 7일 독서 기록:\n")
                    .append(String.format("- 총 읽은 페이지: %d쪽\n", totalPages))
                    .append(String.format("- 총 독서 시간: %d분\n", totalMinutes))
                    .append(String.format("- 완독한 책: %d권\n\n", finishedCount));
        }

        // 사용자 메시지 추가
        context.append("사용자 요청: ").append(userMessage);
        return context.toString();
    }


}
