package com.example.meltingbooks;


import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;


import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.Locale;

public class CalendarContentFragment extends Fragment {
    public CalendarContentFragment() { }

    //달력 생성
    private GridLayout calendarGrid;
    private TextView textMonth;
    private Calendar currentCalendar;

    private TextView selectedDayView = null;
    private Calendar selectedDate = Calendar.getInstance();  // 기본: 오늘


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar_content, container, false);


        //달력 생성 관련--------------------------
        calendarGrid = view.findViewById(R.id.calendarGrid);
        textMonth = view.findViewById(R.id.textMonth);

        currentCalendar = Calendar.getInstance();
        updateCalendar(view);

        // 이전 달
        view.findViewById(R.id.btnPrev).setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendar(view);
        });

        // 다음 달
        view.findViewById(R.id.btnNext).setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendar(view);
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

        // btn_monthly_report 클릭 시 MonthlyReportFragment 로 전환
        View MonthlyReportButton = view.findViewById(R.id.btn_monthly_report);
        if (MonthlyReportButton != null) {
            MonthlyReportButton.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MonthlyReportFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        return view;
    }

    //달력 생성 알고리즘
    private void updateCalendar(View view) {
        // 요일 표시
        LinearLayout weekdaysRow = view.findViewById(R.id.weekdaysRow);
        weekdaysRow.removeAllViews();

        String[] weekdays = {"S", "M", "T", "W", "T", "F", "S"};
        for (int i = 0; i < weekdays.length; i++) {
            TextView dayLabel = new TextView(getContext());
            dayLabel.setText(weekdays[i]);
            dayLabel.setTextSize(14);
            dayLabel.setGravity(Gravity.CENTER);
            dayLabel.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            // 색상 지정
            if (i == 0) {
                dayLabel.setTextColor(Color.parseColor("#FC1F8E")); // 일요일
            } else if (i == 6) {
                dayLabel.setTextColor(Color.parseColor("#1D9BF0")); // 토요일
            } else {
                dayLabel.setTextColor(Color.BLACK); // 평일
            }

            weekdaysRow.addView(dayLabel);
        }

        calendarGrid.removeAllViews();

        // 현재 월의 정보
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH);

        // 달 이름 표시
        java.text.SimpleDateFormat monthFormat = new java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.ENGLISH);
        textMonth.setText(monthFormat.format(currentCalendar.getTime()));

        // 1일이 무슨 요일인지 계산 (0:일 ~ 6:토)
        Calendar tempCal = Calendar.getInstance();
        tempCal.set(year, month, 1);
        int startDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) - 1;

        int maxDay = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 빈칸 먼저 채우기
        for (int i = 0; i < startDayOfWeek; i++) {
            TextView emptyView = new TextView(getContext());
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            emptyView.setLayoutParams(params);
            calendarGrid.addView(emptyView);
        }

        //날짜 채우기
        for (int day = 1; day <= maxDay; day++) {
            TextView dayView = new TextView(getContext());
            dayView.setText(String.valueOf(day));
            dayView.setGravity(Gravity.CENTER);
            dayView.setTextSize(16);
            dayView.setPadding(8, 8, 8, 8);

            // 크기 설정 (정사각형)
            int sizeInDp = 35;
            int sizeInPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, sizeInDp, getResources().getDisplayMetrics()
            );
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = sizeInPx;
            params.height = sizeInPx;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            dayView.setLayoutParams(params);

            Calendar thisDate = Calendar.getInstance();
            thisDate.set(year, month, day);

            // 초기 스타일 적용
            if (thisDate.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                    thisDate.get(Calendar.MONTH) == selectedDate.get(Calendar.MONTH) &&
                    thisDate.get(Calendar.DAY_OF_MONTH) == selectedDate.get(Calendar.DAY_OF_MONTH)) {
                dayView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_selected_date));
                dayView.setTextColor(Color.WHITE);
                selectedDayView = dayView;
            } else {
                dayView.setTextColor(Color.BLACK);
            }

            // 클릭 이벤트 처리
            dayView.setOnClickListener(v -> {
                // 기존 선택 해제
                if (selectedDayView != null) {
                    selectedDayView.setBackground(null);
                    selectedDayView.setTextColor(Color.BLACK);
                }

                // 새로 선택
                selectedDayView = (TextView) v;
                selectedDayView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_selected_date));
                selectedDayView.setTextColor(Color.WHITE);

                // 날짜 저장
                selectedDate.set(year, month, Integer.parseInt(dayView.getText().toString()));

                // 날짜별 기록 표시
                SimpleDateFormat format = new SimpleDateFormat("M/d (E)", Locale.KOREA);
                TextView goalByDate = getActivity().findViewById(R.id.goal_by_date);
                if (goalByDate != null) {
                    goalByDate.setText(format.format(selectedDate.getTime()));
                }
            });

            calendarGrid.addView(dayView);
        }

    }
}
