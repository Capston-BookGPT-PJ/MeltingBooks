package com.example.meltingbooks.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.meltingbooks.R;
import com.example.meltingbooks.feed.FeedActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 5000; // 5초 (원하는 시간 조절 가능)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // splash 레이아웃 적용

        // 상태바 색상 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
/*
        // 일정 시간 뒤 자동 로그인 검사
        new Handler().postDelayed(() -> {
            // token 받아오기
            SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
            String token = prefs.getString("jwt", null);
            int userId = prefs.getInt("userId", -1);


            Intent intent;
/*
            if (token != null && userId != -1) {
                // ✅ 자동 로그인 성공 → FeedActivity로 이동
                intent = new Intent(SplashActivity.this, FeedActivity.class);
            } else {
                // ❌ 토큰 없음 → LoginActivity로 이동
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();

        }, SPLASH_DELAY);*/

        // 일정 시간 뒤 LoginActivity로 이동
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // SplashActivity 종료
            }
        }, SPLASH_DELAY);
    }
}
