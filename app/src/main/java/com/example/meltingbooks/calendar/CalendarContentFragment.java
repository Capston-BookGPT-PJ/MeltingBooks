package com.example.meltingbooks.calendar;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.meltingbooks.R;
import com.example.meltingbooks.calendar.record.AddReadingRecordFragment;
import com.example.meltingbooks.calendar.utils.BookListHelper;
import com.example.meltingbooks.calendar.utils.ProgressBarUtil;
import com.example.meltingbooks.calendar.view.CircularProgressView;
import com.example.meltingbooks.calendar.view.GoalProgressView;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.book.Book;
import com.example.meltingbooks.network.book.BookApi;
import com.example.meltingbooks.network.goal.GoalApi;
import com.example.meltingbooks.network.goal.GoalController;
import com.example.meltingbooks.network.goal.GoalResponse;
import com.example.meltingbooks.network.growth.GrowthApi;
import com.example.meltingbooks.network.growth.GrowthController;
import com.example.meltingbooks.network.log.LogApi;
import com.example.meltingbooks.network.log.LogController;
import com.example.meltingbooks.network.log.ReadingLogResponse;
import com.example.meltingbooks.network.book.BookController;


import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Date;


public class CalendarContentFragment extends Fragment {
    public CalendarContentFragment() { }

    //달력 생성
    private GridLayout calendarGrid;
    private TextView textMonth;
    private Calendar currentCalendar;

    private TextView selectedDayView = null;
    private Calendar selectedDate = Calendar.getInstance();  // 기본: 오늘

    private String token;
    private int userId;

    private GoalController goalController;
    private LogController logController;
    private BookController bookController;
    private GrowthController growthController;

    //기록이 있는 날짜 캘린더에 표시
    private Set<Integer> loggedDays = new HashSet<>();




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar_content, container, false);


        // SharedPreferences에서 토큰, userId 가져오기
        SharedPreferences prefs = requireContext().getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("jwt", null);
        userId = prefs.getInt("userId", -1);


        //달력 생성 관련--------------------------
        calendarGrid = view.findViewById(R.id.calendarGrid);
        textMonth = view.findViewById(R.id.textMonth);


        currentCalendar = Calendar.getInstance();
        updateCalendar(view);



        // 오늘 날짜로 초기화
        SimpleDateFormat format = new SimpleDateFormat("M/d (E)", Locale.KOREA);
        TextView goalByDate = view.findViewById(R.id.goal_by_date);
        if (goalByDate != null) {
            goalByDate.setText(format.format(selectedDate.getTime()));
        }


        // 이전 달
        view.findViewById(R.id.btnPrev).setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendar(view);
            loadLoggedDaysOfMonth(view); // ← 기록 있는 날짜 다시 불러오기
        });

        // 다음 달
        view.findViewById(R.id.btnNext).setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendar(view);
            loadLoggedDaysOfMonth(view); // ← 기록 있는 날짜 다시 불러오기
        });

        // detail_goal_button 클릭 시 DetailGoalFragment로 전환
        View detailGoalButton = view.findViewById(R.id.detail_goal_button);
        if (detailGoalButton != null) {
            detailGoalButton.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new DetailGoalFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        // ✅ 독서 기록 추가 버튼
        View addReadingRecord = view.findViewById(R.id.add_reading_record);
        if (addReadingRecord != null) {
            addReadingRecord.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new AddReadingRecordFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }


        GoalApi apiService = ApiClient.getClient(token).create(GoalApi.class);
        goalController = new GoalController(apiService);

        // onCreateView()에서
        LogApi logApi = ApiClient.getClient(token).create(LogApi.class);
        logController = new LogController(logApi);

        BookApi bookApi = ApiClient.getClient(token).create(BookApi.class);
        bookController = new BookController(getContext());

        GrowthApi growthApi = ApiClient.getClient(token).create(GrowthApi.class);
        growthController = new GrowthController(growthApi, token);

        // 월간 불러오기
        loadGoal("MONTHLY");

        // 책 리스트 UI 생성
        setupBooks(view);

        //이번 달 기록 조회
        loadLoggedDaysOfMonth(view);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        String todayStr = sdf.format(selectedDate.getTime());
        loadLogsByDate(todayStr);

        return view;
    }

    //달력 생성 알고리즘
    private void updateCalendar(View view) {
        LinearLayout weekdaysRow = view.findViewById(R.id.weekdaysRow);
        weekdaysRow.removeAllViews();

        String[] weekdays = {"S", "M", "T", "W", "T", "F", "S"};
        for (int i = 0; i < weekdays.length; i++) {
            TextView dayLabel = new TextView(getContext());
            dayLabel.setText(weekdays[i]);
            dayLabel.setTextSize(14);
            dayLabel.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            dayLabel.setLayoutParams(params);

            if (i == 0)
                dayLabel.setTextColor(Color.parseColor("#FC1F8E"));
            else if (i == 6)
                dayLabel.setTextColor(Color.parseColor("#1D9BF0"));
            else
                dayLabel.setTextColor(Color.BLACK);

            weekdaysRow.addView(dayLabel);
        }

        calendarGrid.removeAllViews();

        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH);

        java.text.SimpleDateFormat monthFormat =
                new java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.ENGLISH);
        textMonth.setText(monthFormat.format(currentCalendar.getTime()));

        Calendar tempCal = Calendar.getInstance();
        tempCal.set(year, month, 1);
        int startDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1;
        int maxDay = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 빈칸 채우기
        for (int i = 0; i < startDayOfWeek; i++) {
            TextView emptyView = new TextView(getContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(i, 1f);
            params.setMargins(1, 5, 1, 5); // ✅ 수평/수직 간격 줄임
            emptyView.setLayoutParams(params);
            calendarGrid.addView(emptyView);
        }

        for (int day = 1; day <= maxDay; day++) {
            // ✅ 각 날짜 셀을 감싸는 ConstraintLayout
            ConstraintLayout cellLayout = new ConstraintLayout(getContext());
            GridLayout.LayoutParams cellParams = new GridLayout.LayoutParams();
            cellParams.width = 0;
            cellParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            cellParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            cellParams.setMargins(1, 5, 1, 5); // ✅ 수평/수직 간격 줄임
            cellLayout.setLayoutParams(cellParams);

            // ✅ 날짜 TextView 생성
            TextView dayView = new TextView(getContext());
            dayView.setId(View.generateViewId());
            dayView.setText(String.valueOf(day));
            dayView.setTextSize(16);
            dayView.setGravity(Gravity.CENTER);
            dayView.setTextColor(Color.BLACK);

            cellLayout.addView(dayView);

            // ConstraintLayout 안에 넣기 위한 파라미터
            /**ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
             0, 0
             );*/

            // ✅ 원 크기 줄이기
            int circleSizeInDp = 30; // 원하는 원 크기 (기존보다 작게)
            int circleSizeInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, circleSizeInDp, getResources().getDisplayMetrics()
            );

            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    circleSizeInPx, circleSizeInPx
            );

            // ✅ 정사각형 비율 강제
            params.dimensionRatio = "1:1";
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            dayView.setLayoutParams(params);


            // ✅ 작은 점(기록 표시용) 추가
            View dotView = new View(getContext());
            dotView.setId(View.generateViewId());

            int dotSize = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics()
            );

            ConstraintLayout.LayoutParams dotParams =
                    new ConstraintLayout.LayoutParams(dotSize, dotSize);

            // ✅ 셀의 '아래쪽 중앙'에 고정 → 무조건 보임
            dotParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            dotParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            dotParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            dotParams.bottomMargin = 6;

            dotView.setLayoutParams(dotParams);

            // ✅ 동그라미 + 연한 하늘색
            dotView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.dot_logged_date));
            dotView.setVisibility(View.GONE);

            cellLayout.addView(dotView);

            Calendar thisDate = Calendar.getInstance();
            thisDate.set(year, month, day);

            // ✅ 기록 있는 날짜 표시 (여기 추가)
            if (loggedDays.contains(day)) {
                dotView.setVisibility(View.VISIBLE);
            }


            // ✅ 선택된 날짜 배경
            if (thisDate.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)
                    && thisDate.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH)
                    && thisDate.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH)) {
                dayView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_selected_date));

                dayView.setTextColor(Color.WHITE);
                selectedDayView = dayView;
            }

            // ✅ 클릭 이벤트
            dayView.setOnClickListener(v -> {
                if (selectedDayView != null) {
                    selectedDayView.setBackground(null);
                    selectedDayView.setTextColor(Color.BLACK);
                }

                selectedDayView = (TextView) v;
                selectedDayView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_selected_date));
                selectedDayView.setTextColor(Color.WHITE);

                selectedDate.set(year, month, Integer.parseInt(dayView.getText().toString()));

                SimpleDateFormat format = new SimpleDateFormat("M/d (E)", Locale.KOREA);
                TextView goalByDate = getActivity().findViewById(R.id.goal_by_date);
                if (goalByDate != null) goalByDate.setText(format.format(selectedDate.getTime()));

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
                loadLogsByDate(sdf.format(selectedDate.getTime()));
            });

            calendarGrid.addView(cellLayout);
        }
    }

    private List<BookListHelper.BookItem> bookItems = new ArrayList<>();

    private void setupBooks(View view) {
        LinearLayout container = view.findViewById(R.id.book_list_container);

        // 샘플 데이터
        bookItems.clear();
        bookItems.add(new BookListHelper.BookItem(R.drawable.book_image, false));

        BookListHelper.setupBooks(getContext(), container, bookItems, true);
    }

    private void loadGoal(String goalType) {
        goalController.getGoals(token, new GoalController.GoalCallback<List<GoalResponse>>() {
            @Override
            public void onSuccess(List<GoalResponse> goals) {
                if (goals != null && !goals.isEmpty()) {

                    // 현재 날짜 구하기
                    java.util.Calendar calendar = java.util.Calendar.getInstance();
                    int currentYear = calendar.get(java.util.Calendar.YEAR);
                    int currentMonth = calendar.get(java.util.Calendar.MONTH) + 1; // 0부터 시작하므로 +1

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
        float totalProgress = (float) ((goal.getBookProgress() + goal.getReviewProgress() + goal.getTimeProgress()) / 3.0);

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
        goal3.setProgressWithGoal(completedHours, targetHours, goal.getTimeProgress());

        // 목표 100% 달성 시 경험치 지급
        if (totalProgress >= 100f) {
            String eventType = "MONTHLY".equalsIgnoreCase(goal.getGoalType()) ?
                    "ACHIEVE_MONTHLY_GOAL" : "ACHIEVE_YEARLY_GOAL";
            giveCompletionExp(eventType);
        }
    }

    private void loadLogsByDate(String dateStr) {
        logController.getLogsByPeriod(token, userId, dateStr, dateStr,
                new retrofit2.Callback<ApiResponse<List<ReadingLogResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<ReadingLogResponse>>> call,
                                           Response<ApiResponse<List<ReadingLogResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<ReadingLogResponse> logs = response.body().getData();
                            displayLog(dateStr, logs);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ReadingLogResponse>>> call, Throwable t) {
                        Log.e("CalendarContent", "Log fetch failed", t);
                    }
                });
    }

    private void displayLog(String selectedDateStr, List<ReadingLogResponse> logs) {
        View root = getView();
        if (root == null) return;

        // ✅ 숨길 모든 View 미리 가져오기
        View bookImageContainer = root.findViewById(R.id.book_image_container);
        View grayLine1 = root.findViewById(R.id.gray_line_1);

        View pageInputContainer = root.findViewById(R.id.page_input_container);
        View grayLine2 = root.findViewById(R.id.gray_line_2);

        View timeInputContainer = root.findViewById(R.id.time_input_container);
        View grayLine4 = root.findViewById(R.id.gray_line_4);

        View startTimeContainer = root.findViewById(R.id.start_time_input_container);
        View grayLine3 = root.findViewById(R.id.gray_line_3);

        // 숫자 표시 뷰 참조
        TextView editPage = root.findViewById(R.id.edit_page);
        TextView editHours = root.findViewById(R.id.edit_hour);
        TextView editMinutes = root.findViewById(R.id.edit_minute);
        CheckBox checkboxFinished = root.findViewById(R.id.checkboxFinished);
        TextView spinnerAmPm = root.findViewById(R.id.spinner_am_pm);
        TextView editStartHour = root.findViewById(R.id.edit_start_hour);
        TextView editStartMinute = root.findViewById(R.id.edit_start_minute);

        int totalPages = 0;
        int totalMinutes = 0;

        /*if (logs != null && !logs.isEmpty()) {
            for (ReadingLogResponse log : logs) {
                totalPages += log.getPagesRead();
                totalMinutes += log.getMinutesRead();
            }
        } else {
            // 기록 없을 때
            Toast.makeText(getContext(), "기록이 없습니다.", Toast.LENGTH_SHORT).show();
        }*/

        // ✅ 기록 없을 때 처리
        if (logs == null || logs.isEmpty()) {

            Toast.makeText(getContext(), "오늘의 독서 기록이 없습니다.", Toast.LENGTH_SHORT).show();

            // ✅ 4개 영역 + 4개 구분선 숨기기
            bookImageContainer.setVisibility(View.GONE);
            grayLine1.setVisibility(View.GONE);

            pageInputContainer.setVisibility(View.GONE);
            grayLine2.setVisibility(View.GONE);

            timeInputContainer.setVisibility(View.GONE);
            grayLine4.setVisibility(View.GONE);

            startTimeContainer.setVisibility(View.GONE);
            grayLine3.setVisibility(View.GONE);

            // ✅ 필드 초기화 (기존 코드 유지)
            editPage.setText("0");
            editHours.setText("0");
            editMinutes.setText("0");
            checkboxFinished.setChecked(false);
            spinnerAmPm.setText("오전/오후");
            editStartHour.setText("0");
            editStartMinute.setText("0");

            return; // ✅ 여기서 종료 (중복 실행 방지)
        }

        // ✅ 기록 있을 때 다시 보이기
        bookImageContainer.setVisibility(View.VISIBLE);
        grayLine1.setVisibility(View.VISIBLE);

        pageInputContainer.setVisibility(View.VISIBLE);
        grayLine2.setVisibility(View.VISIBLE);

        timeInputContainer.setVisibility(View.VISIBLE);
        grayLine4.setVisibility(View.VISIBLE);

        startTimeContainer.setVisibility(View.VISIBLE);
        grayLine3.setVisibility(View.VISIBLE);


        // ✅ 누적 값 계산 (기존 코드 유지)
        for (ReadingLogResponse log : logs) {
            totalPages += log.getPagesRead();
            totalMinutes += log.getMinutesRead();
        }

        // 값 세팅
        editPage.setText(String.valueOf(totalPages));

        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        editHours.setText(String.valueOf(hours));
        editMinutes.setText(String.valueOf(minutes));


        // 책 표지 처리
        LinearLayout container = root.findViewById(R.id.book_list_container);
        container.removeAllViews();
        bookItems.clear();

        if (logs != null) {
            for (ReadingLogResponse log : logs) {
                int bookId = log.getBookId();

                //bookId 기반으로 책 상세 조회
                bookController.getBookDetail(bookId, new retrofit2.Callback<Book>() {
                    @Override
                    public void onResponse(Call<Book> call, Response<Book> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Book book = response.body();
                            String coverUrl = book.getCover();

                            // 커버 URL을 BookItem에 넣어주도록 BookListHelper 확장
                            BookListHelper.BookItem item = new BookListHelper.BookItem(coverUrl, false);
                            bookItems.add(item);

                            // UI 갱신
                            BookListHelper.setupBooks(getContext(), container, bookItems, true);
                        }
                    }

                    @Override
                    public void onFailure(Call<Book> call, Throwable t) {
                        Log.e("CalendarContent", "책 상세 조회 실패: " + bookId, t);
                    }
                });
            }
        }

        // --- 추가: 완독/읽기 시작 시간 표시 ---
        if (logs != null && !logs.isEmpty()) {
            ReadingLogResponse log = logs.get(0);

            checkboxFinished.setChecked(log.isFinished());

            if (log.getReadAt() != null && !log.getReadAt().isEmpty()) {
                String timeStr = log.getReadAt().substring(11, 16); // HH:mm
                String[] parts = timeStr.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);

                boolean isPm = hour >= 12;
                int displayHour = hour % 12;
                if (displayHour == 0) displayHour = 12;

                spinnerAmPm.setText(isPm ? "오후" : "오전");
                editStartHour.setText(String.valueOf(displayHour));
                editStartMinute.setText(String.valueOf(minute));
            }
        } else {
            checkboxFinished.setChecked(false);
            spinnerAmPm.setText("오전/오후");
            editStartHour.setText("0");
            editStartMinute.setText("0");
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

    //달력에 기록있는 날 표시
    // ✅ 해당 월에 독서 기록이 있는 날을 Set에 저장
    private void loadLoggedDaysOfMonth(View view) {
        loggedDays.clear();

        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH);

        Calendar startCal = Calendar.getInstance();
        startCal.set(year, month, 1);

        Calendar endCal = Calendar.getInstance();
        endCal.set(year, month, startCal.getActualMaximum(Calendar.DAY_OF_MONTH));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);

        String startStr = sdf.format(startCal.getTime());
        String endStr = sdf.format(endCal.getTime());

        logController.getLogsByPeriod(token, userId, startStr, endStr,
                new Callback<ApiResponse<List<ReadingLogResponse>>>() {

                    @Override
                    public void onResponse(Call<ApiResponse<List<ReadingLogResponse>>> call,
                                           Response<ApiResponse<List<ReadingLogResponse>>> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            List<ReadingLogResponse> logs = response.body().getData();

                            if (logs != null) {
                                for (ReadingLogResponse log : logs) {
                                    String dateStr = log.getReadAt().substring(0, 10);

                                    try {
                                        java.util.Date d = sdf.parse(dateStr);
                                        Calendar c = Calendar.getInstance();
                                        c.setTime(d);
                                        int day = c.get(Calendar.DAY_OF_MONTH);
                                        loggedDays.add(day);
                                    } catch (Exception ignored) {}
                                }
                            }
                        }

                        // ✅ 기록 다 받은 후 달력 다시 업데이트
                        if (view != null) updateCalendar(view);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<ReadingLogResponse>>> call, Throwable t) {
                        Log.e("Calendar", "달 기록 조회 실패", t);
                        if (view != null) updateCalendar(view);
                    }
                });
    }



}