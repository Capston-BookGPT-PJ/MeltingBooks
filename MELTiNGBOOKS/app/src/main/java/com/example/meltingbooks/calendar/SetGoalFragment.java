package com.example.meltingbooks.calendar;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.meltingbooks.R;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.goal.GoalApi;
import com.example.meltingbooks.network.goal.GoalController;
import com.example.meltingbooks.network.goal.GoalRequest;
import com.example.meltingbooks.network.goal.GoalResponse;
import com.google.gson.Gson;

import java.time.LocalDate;
import java.util.List;

public class SetGoalFragment extends Fragment {

    private GoalController goalController;
    private String token;
    private int userId;
    private Integer currentGoalId = null;

    private EditText editPage, editReview, editTime;
    private TextView btnMonthly, btnYearly, btnSaveGoal, btnDeleteGoal;
    private TextView tvTitle;

    public SetGoalFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_goal, container, false);

        tvTitle = view.findViewById(R.id.set_goal_title);

        btnMonthly = view.findViewById(R.id.set_goal_monthly);
        btnYearly = view.findViewById(R.id.set_goal_yearly);
        btnSaveGoal = view.findViewById(R.id.btn_save_goal);
        btnDeleteGoal = view.findViewById(R.id.btn_delete_goal);

        editPage = view.findViewById(R.id.edit_page);
        editReview = view.findViewById(R.id.edit_review);
        editTime = view.findViewById(R.id.edit_time);

        // SharedPreferences에서 토큰, userId 가져오기
        SharedPreferences prefs = requireContext().getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("jwt", null);
        userId = prefs.getInt("userId", -1);

        GoalApi apiService = ApiClient.getClient(token).create(GoalApi.class);
        goalController = new GoalController(apiService);

        // 초기 상태 (월간 선택)
        btnMonthly.setSelected(true);
        btnYearly.setSelected(false);

        btnMonthly.setOnClickListener(v -> {
            btnMonthly.setSelected(true);
            btnYearly.setSelected(false);
            loadGoal("MONTHLY");
        });

        btnYearly.setOnClickListener(v -> {
            btnMonthly.setSelected(false);
            btnYearly.setSelected(true);
            loadGoal("YEARLY");
        });

        btnSaveGoal.setOnClickListener(v -> saveGoal());
        btnDeleteGoal.setOnClickListener(v -> deleteGoal());

        // 진입 시 월간 목표 자동 조회
        loadGoal("MONTHLY");

        return view;
    }

    private void loadGoal(String goalType) {
        LocalDate now = LocalDate.now();

        if (goalType.equals("YEARLY")) {
            tvTitle.setText(String.format("%d년 독서 목표 설정", now.getYear()));
        } else { // MONTHLY
            tvTitle.setText(String.format("%d월 독서 목표 설정", now.getMonthValue()));
        }

        goalController.getGoals(token, new GoalController.GoalCallback<List<GoalResponse>>() {
            @Override
            public void onSuccess(List<GoalResponse> result) {
                currentGoalId = null;

                for (GoalResponse goal : result) {
                    if (goal.getGoalType().equals(goalType)) {
                        if (goalType.equals("YEARLY") && goal.getYear() == now.getYear()) {
                            currentGoalId = goal.getId();
                            applyGoalToUI(goal);
                            break;
                        } else if (goalType.equals("MONTHLY")
                                && goal.getYear() == now.getYear()
                                && goal.getMonth() == now.getMonthValue()) {
                            currentGoalId = goal.getId();
                            applyGoalToUI(goal);
                            break;
                        }
                    }
                }

                if (currentGoalId == null) {
                    clearUI();
                    Toast.makeText(getContext(), goalType + " 목표 없음. 새로 등록하세요.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(getContext(), "목표 조회 실패: " + errorMsg, Toast.LENGTH_SHORT).show();
                Log.e("GoalResponse", "목표 조회 실패: " + errorMsg); // 로그 출력
            }
        });
    }

    private void saveGoal() {
        int books = Integer.parseInt(editPage.getText().toString());
        int reviews = Integer.parseInt(editReview.getText().toString());
        //int time = Integer.parseInt(editTime.getText().toString());

        // ✅ UI는 시간 단위 입력 → 서버 저장은 분 단위
        int inputHours = Integer.parseInt(editTime.getText().toString());
        int time = inputHours * 60;


        String goalType = btnMonthly.isSelected() ? "MONTHLY" : "YEARLY";

        LocalDate now = LocalDate.now();
        String startDate = goalType.equals("YEARLY")
                ? String.format("%04d-01-01", now.getYear())
                : String.format("%04d-%02d-01", now.getYear(), now.getMonthValue());

        String endDate = goalType.equals("YEARLY")
                ? String.format("%04d-12-31", now.getYear())
                : now.withDayOfMonth(now.lengthOfMonth()).toString(); // endDate는 toString()이 yyyy-MM-dd 형식이라 그대로 사용 가능



        GoalRequest request = new GoalRequest(
                userId, goalType, books, reviews, time, startDate, endDate
        );

        goalController.saveGoal(token, request, currentGoalId, new GoalController.GoalCallback<GoalResponse>() {
            @Override
            public void onSuccess(GoalResponse result) {
                currentGoalId = result.getId();
                Toast.makeText(getContext(), "목표 저장 성공!", Toast.LENGTH_SHORT).show();

                // ✅ 목표 저장 후 재계산 API 호출
                goalController.recomputeGoal(currentGoalId, new GoalController.GoalCallback<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        Toast.makeText(getContext(), "달성률 재계산 완료!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorMsg) {
                        Toast.makeText(getContext(), "재계산 실패: " + errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e("SetGoalFragment", "Recompute 실패: " + errorMsg);
                    }
                });
            }

            @Override
            public void onError(String errorMsg) {
                Log.e("SetGoalFragment", "저장 실패 원인: " + errorMsg);
                Toast.makeText(getContext(), "저장 실패: " + errorMsg, Toast.LENGTH_SHORT).show();
                Gson gson = new Gson();
                Log.d("SetGoalFragment", "보내는 GoalRequest JSON: " + gson.toJson(request));
            }
        });
    }

    private void deleteGoal() {
        if (currentGoalId != null) {
            goalController.deleteGoal(token, currentGoalId, new GoalController.GoalCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    clearUI();
                    currentGoalId = null;
                    Toast.makeText(getContext(), "목표 삭제 완료", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String errorMsg) {
                    Toast.makeText(getContext(), "삭제 실패: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void applyGoalToUI(GoalResponse goal) {
        editPage.setText(String.valueOf(goal.getTargetBooks()));
        editReview.setText(String.valueOf(goal.getTargetReviews()));
        //editTime.setText(String.valueOf(goal.getTargetMinutes()));

        // ✅ DB는 분 단위 → UI는 시간 단위로 변환
        int totalMinutes = goal.getTargetMinutes();
        int hours = totalMinutes / 60;
        editTime.setText(String.valueOf(hours));
    }

    private void clearUI() {
        editPage.setText("");
        editReview.setText("");
        editTime.setText("");
    }
}
