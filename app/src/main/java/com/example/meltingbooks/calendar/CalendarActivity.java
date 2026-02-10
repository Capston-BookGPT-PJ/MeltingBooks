package com.example.meltingbooks.calendar;

import android.os.Bundle;
import android.view.View;

import com.example.meltingbooks.R;
import com.example.meltingbooks.base.BaseActivity;
import com.example.meltingbooks.calendar.aichat.AiChatFragment;
import com.example.meltingbooks.calendar.record.AddReadingRecordFragment;


public class CalendarActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // 하단 메뉴
        setupBottomNavigation();

        // ✅ 리포트 버튼 (Activity 레이아웃에 있을 때)
        View MonthlyReportButton = findViewById(R.id.btn_monthly_report);
        if (MonthlyReportButton != null) {
            MonthlyReportButton.setOnClickListener(v -> {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MonthlyReportFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        //fragment_container
        findViewById(R.id.ai_button).setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new AiChatFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }


    //bottom Navigation의 위치 설정
    @Override
    protected int getCurrentNavItemId() {
        return R.id.Calendar;
    }
}