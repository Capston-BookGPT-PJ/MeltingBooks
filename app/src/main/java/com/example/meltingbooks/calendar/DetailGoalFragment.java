package com.example.meltingbooks.calendar;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.meltingbooks.R;
import com.example.meltingbooks.calendar.utils.BookListHelper;
import com.example.meltingbooks.calendar.utils.BookListHelper.BookItem;
import com.example.meltingbooks.calendar.utils.ProgressBarUtil;
import com.example.meltingbooks.calendar.view.CircularProgressView;
import com.example.meltingbooks.calendar.view.GoalProgressView;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.goal.GoalApi;
import com.example.meltingbooks.network.goal.GoalController;
import com.example.meltingbooks.network.goal.GoalResponse;
import com.example.meltingbooks.network.growth.GrowthApi;
import com.example.meltingbooks.network.growth.GrowthController;
import com.example.meltingbooks.network.log.LogApi;
import com.example.meltingbooks.network.log.LogController;
import com.example.meltingbooks.network.log.ReadingLogResponse;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DetailGoalFragment extends Fragment {
    public DetailGoalFragment() { }

    private String token;
    private int userId;
    private GoalController goalController;

    private LogController logController;

    private GrowthController growthController;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_detail_goal, container, false);

        TextView cheerMessage = view.findViewById(R.id.cheer_message);

        TextView btnMonthly = view.findViewById(R.id.set_goal_monthly);
        TextView btnYearly = view.findViewById(R.id.set_goal_yearly);

        // 초기 상태 설정
        btnMonthly.setSelected(true);
        btnYearly.setSelected(false);

        // SharedPreferences에서 토큰, userId 가져오기
        SharedPreferences prefs = requireContext().getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("jwt", null);
        userId = prefs.getInt("userId", -1);

        GoalApi apiService = ApiClient.getClient(token).create(GoalApi.class);
        goalController = new GoalController(apiService);

        LogApi logApi = ApiClient.getClient(token).create(LogApi.class);
        logController = new LogController(logApi);

        GrowthApi growthApi = ApiClient.getClient(token).create(GrowthApi.class);
        growthController = new GrowthController(growthApi, token);

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

        // 초기값: 월간 불러오기
        loadGoal("MONTHLY");

        // 그래프 처리
        loadWeeklyLogs(userId, view);

        // btn_set_goal 클릭 시 SetGoalFragment로 전환
        View SetlGoalButton = view.findViewById(R.id.btn_set_goal);

        if (SetlGoalButton != null) {
            SetlGoalButton.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new SetGoalFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        return view;
    }

    private void loadWeeklyLogs(int userId, View rootView) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        Calendar calendar = Calendar.getInstance();

        String to = sdf.format(calendar.getTime()); // 오늘
        calendar.add(Calendar.DAY_OF_YEAR, -6);     // 7일 전
        String from = sdf.format(calendar.getTime());

        logController.getLogsByPeriod(token,userId, from, to, new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ReadingLogResponse>>> call,
                                   Response<ApiResponse<List<ReadingLogResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    // ✅ logs + rootView 같이 넘김
                    setupWeeklyGraph(response.body().getData(), rootView);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ReadingLogResponse>>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
    //막대그래프 생성 함수
    public void setupWeeklyGraph(List<ReadingLogResponse> logs, View view) {
        CombinedChart combinedChart = view.findViewById(R.id.weekly_combined_chart);

        // 1. 예시 데이터: 최근 7일 읽은 시간 (시간 단위)
        //float[] readingHours = new float[]{1.5f, 0.5f, 2f, 0f, 3f, 1f, 2.5f}; // 지난 7일 데이터
        List<BarEntry> barEntries = new ArrayList<>();
        List<Entry> lineEntries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();

        Calendar calendar = Calendar.getInstance(); // 오늘 기준
        SimpleDateFormat sdf = new SimpleDateFormat("d", Locale.KOREA); //날짜

        // 날짜별 읽은 시간 합산용 Map
        Map<String, Float> logMap = new HashMap<>();
        for (ReadingLogResponse log : logs) {
            // minutesRead → 시간 단위 변환
            float hours = log.getMinutesRead() / 60f;
            String dateKey = log.getReadAt().substring(0, 10); // yyyy-MM-dd
            logMap.put(dateKey, logMap.getOrDefault(dateKey, 0f) + hours);
        }

        float[] readingHours = new float[7];

        // 최근 7일 데이터 채우기
        for (int i = 6; i >= 0; i--) {
            calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -i);
            String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(calendar.getTime());
            String label = sdf.format(calendar.getTime());

            float hours = logMap.getOrDefault(dateKey, 0f);

            int index = 6 - i;
            barEntries.add(new BarEntry(index, hours));
            lineEntries.add(new Entry(index, hours));
            xLabels.add(label);

            // ✅ 배열에 값 저장
            readingHours[index] = hours;
        }


        // 2. 막대그래프
        BarDataSet barDataSet = new BarDataSet(barEntries, "Hours");
        barDataSet.setDrawValues(false);
        barDataSet.setColors(getBarColors(readingHours));
        barDataSet.setValueTextSize(5f);
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.1f); // 막대 폭 설정


        // 3. 꺾은선 그래프
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Line");
        lineDataSet.setColor(Color.GRAY);
        lineDataSet.setLineWidth(1.5f);
        lineDataSet.enableDashedLine(10f, 5f, 0f);
        lineDataSet.setCircleColor(Color.GRAY);
        lineDataSet.setDrawValues(false);
        lineDataSet.setMode(LineDataSet.Mode.LINEAR);
        LineData lineData = new LineData(lineDataSet);


        // 4. Combine
        CombinedData combinedData = new CombinedData();
        combinedData.setData(barData);
        combinedData.setData(lineData);


        // 5. X축 설정
        XAxis xAxis = combinedChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(barEntries.size() - 0.5f);
        xAxis.setAxisLineColor(Color.TRANSPARENT);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int index = (int) value;
                if (index >= 0 && index < xLabels.size()) {
                    return xLabels.get(index);
                } else {
                    return "";
                }
            }
        });

        // 오늘에 하이라이트
        int todayIndex = 6;
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(12f);
        xAxis.setLabelRotationAngle(0f);


        // 6. Y축 설정
        YAxis leftAxis = combinedChart.getAxisLeft();
        leftAxis.setEnabled(false);

        combinedChart.getAxisRight().setEnabled(false);
        combinedChart.getDescription().setEnabled(false);
        combinedChart.setDrawGridBackground(false);
        combinedChart.setDrawBorders(false);
        combinedChart.setTouchEnabled(false);
        combinedChart.setDoubleTapToZoomEnabled(false);

        Legend legend = combinedChart.getLegend();
        legend.setEnabled(false);

        combinedChart.setData(combinedData);
        combinedChart.invalidate(); // 갱신
    }

    // 막대그래프 색상 지정 (읽은 시간에 따라 색 다르게)
    private List<Integer> getBarColors(float[] hours) {
        List<Integer> colors = new ArrayList<>();
        for (float h : hours) {
            if (h == 0f) {
                colors.add(Color.parseColor("#DC8686"));
            } else if (h <= 1f) {
                colors.add(Color.parseColor("#86DCA4"));
            } else if (h <= 2f) {
                colors.add(Color.parseColor("#86B4DC"));
            } else {
                colors.add(Color.parseColor("#9A86DC"));
            }
        }
        return colors;
    }

    private void loadGoal(String goalType) {
        goalController.getGoals(token, new GoalController.GoalCallback<List<GoalResponse>>() {
            @Override
            public void onSuccess(List<GoalResponse> goals) {
                if (goals != null && !goals.isEmpty()) {

                    // 현재 날짜 구하기
                    Calendar calendar = Calendar.getInstance();
                    int currentYear = calendar.get(Calendar.YEAR);
                    int currentMonth = calendar.get(Calendar.MONTH) + 1; // 0부터 시작하므로 +1

                    GoalResponse selectedGoal = null;

                    for (GoalResponse goal : goals) {
                        if ("MONTHLY".equalsIgnoreCase(goalType)) {
                            // 월간 목표 → goalType = MONTHLY, 현재 연도/월 일치
                            if ("MONTHLY".equalsIgnoreCase(goal.getGoalType())
                                    && goal.getYear() == currentYear
                                    && goal.getMonth() != null
                                    && goal.getMonth() == currentMonth) {
                                selectedGoal = goal;
                                break;
                            }
                        } else if ("YEARLY".equalsIgnoreCase(goalType)) {
                            // 연간 목표 → goalType = YEARLY, 현재 연도 일치
                            if ("YEARLY".equalsIgnoreCase(goal.getGoalType())
                                    && goal.getYear() == currentYear) {
                                selectedGoal = goal;
                                break;
                            }
                        }
                    }

                    if (selectedGoal != null) {
                        bindGoalData(selectedGoal);
                    } else {
                        Log.w("DetailGoalFragment", "해당하는 목표 없음 (" + goalType + ")");
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("DetailGoalFragment", "loadGoal error: " + errorMessage);
            }
        });
    }


    private void bindGoalData(GoalResponse goal) {

        //제목 설정
        TextView title = getView().findViewById(R.id.reading_goal); // fragment_detail_goal.xml 안에 제목 TextView 있어야 함

        if ("MONTHLY".equalsIgnoreCase(goal.getGoalType())) {
            title.setText(goal.getMonth() + "월 독서 목표 달성 현황");
        } else if ("YEARLY".equalsIgnoreCase(goal.getGoalType())) {
            title.setText(goal.getYear() + "년 독서 목표 달성 현황");
        }


        // 응원 메시지
        TextView cheerMessage = getView().findViewById(R.id.cheer_message);
        float totalProgress = (float) ((goal.getBookProgress() + goal.getReviewProgress() + goal.getTimeProgress()) / 3.0);


        String message;
        if (totalProgress >= 100) {
            message = "목표 달성! 너무 멋져요! 🏆";
        } else if (totalProgress >= 70) {
            message = "거의 다 왔어요! 끝까지 힘내요! ✨";
        } else if (totalProgress >= 40) {
            message = "지금까지 잘해왔어요! 남은 절반도 화이팅! 💪";
        } else {
            message = "시작이 반이에요! 천천히 함께 해요! 🌱";
        }
        cheerMessage.setText(message);

        // 원형 전체 진행률
        CircularProgressView circleProgress = getView().findViewById(R.id.circle_progress);
        ProgressBarUtil.setCircularProgress(circleProgress, totalProgress);

        // 목표 1 (책 권수)
        GoalProgressView goal1 = getView().findViewById(R.id.goal1_view);
        goal1.setUnit("권");
        goal1.setProgressWithGoal(goal.getCompletedBooks(), goal.getTargetBooks(),goal.getBookProgress());

        // 목표 2 (리뷰 개수)
        GoalProgressView goal2 = getView().findViewById(R.id.goal2_view);
        goal2.setUnit("개");
        goal2.setProgressWithGoal(goal.getCompletedReviews(), goal.getTargetReviews(),goal.getReviewProgress());

        // 목표 3 (독서 시간)
        GoalProgressView goal3 = getView().findViewById(R.id.goal3_view);
        goal3.setUnit("시간");
        float completedHours = goal.getCompletedMinutes() / 60f;
        float targetHours = goal.getTargetMinutes() / 60f;
        goal3.setProgressWithGoal(completedHours, targetHours,goal.getTimeProgress());

        // 목표 100% 달성 시 경험치 지급
        if (totalProgress >= 100f) {
            String eventType = "MONTHLY".equalsIgnoreCase(goal.getGoalType()) ?
                    "ACHIEVE_MONTHLY_GOAL" : "ACHIEVE_YEARLY_GOAL";
            giveCompletionExp(eventType);
        }

        // 20dp 높이, 제목 텍스트 크기 20sp → px 변환
        float titlePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics());

        GoalProgressView goal1Detail = getView().findViewById(R.id.goal1_view_detail);
        goal1Detail.setUnit("권");
        goal1Detail.setProgressWithGoal(goal.getCompletedBooks(), goal.getTargetBooks(), 20, titlePx,goal.getBookProgress());

        GoalProgressView goal2Detail = getView().findViewById(R.id.goal2_view_detail);
        goal2Detail.setUnit("개");
        goal2Detail.setProgressWithGoal(goal.getCompletedReviews(), goal.getTargetReviews(), 20, titlePx,goal.getReviewProgress());

        GoalProgressView goal3Detail = getView().findViewById(R.id.goal3_view_detail);
        goal3Detail.setUnit("시간");
        goal3Detail.setProgressWithGoal(completedHours, targetHours, 20, titlePx,goal.getTimeProgress());


        // BookListHelper용 리스트 생성
        LinearLayout bookListContainer = getView().findViewById(R.id.book_list_container);
        bookListContainer.removeAllViews();

        List<GoalResponse.BookInfo> books = goal.getBooks();
        if (books != null && !books.isEmpty()) {
            List<BookItem> bookItems = new ArrayList<>();
            for (GoalResponse.BookInfo book : books) {
                // URL 기반 BookItem 생성
                bookItems.add(new BookItem(book.getCoverUrl(),true, false)); // true -> 완독 표시
            }

            // BookListHelper로 세팅
            BookListHelper.setupBooks(requireContext(), bookListContainer, bookItems, false);

        } else {
            // 책이 없는 경우 안내 문구
            TextView emptyMessage = new TextView(requireContext());
            emptyMessage.setText("아직 완독한 책이 없어요!");
            emptyMessage.setTextColor(Color.DKGRAY);
            emptyMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            emptyMessage.setPadding(16, 16, 16, 16);
            bookListContainer.addView(emptyMessage);
        }
    }


    private void giveCompletionExp(String eventType) {
        growthController.giveExp(userId, eventType, new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body();

                    // 이미 지급된 이벤트 메시지는 토스트 표시 안 함
                    if (message.contains("이미 지급된 이벤트입니다")) {
                        Log.i("ExpAPI", "이미 지급된 이벤트: " + message);
                        return;
                    }

                    // 그 외 성공 메시지만 토스트
                    Toast.makeText(requireContext(), "경험치 지급 완료: " + message, Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(requireContext(), "경험치 지급 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(requireContext(), "서버 통신 실패", Toast.LENGTH_SHORT).show();
                Log.e("ExpAPI", "서버 통신 실패", t);
            }
        });
    }

}

