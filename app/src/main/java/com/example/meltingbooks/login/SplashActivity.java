package com.example.meltingbooks.login;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.meltingbooks.R;
import com.example.meltingbooks.feed.FeedActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000; // 3초 정도
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 상태바 색상 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // Splash 텍스트에 위에서부터 사라지는 효과 적용
        TextView splashText = findViewById(R.id.splashText);
        splashText.post(() -> {
            float textHeight = splashText.getHeight();

            // LinearGradient: 위쪽부터 투명하게
            LinearGradient shader = new LinearGradient(
                    0, 0, 0, textHeight,
                    new int[]{0x00000000, 0xFF000000}, // 위: 투명, 아래: 검정
                    new float[]{0f, 1f},
                    Shader.TileMode.CLAMP
            );
            splashText.getPaint().setShader(shader);
            splashText.invalidate();

            // ValueAnimator로 위에서부터 흐려지는 애니메이션 적용
            ValueAnimator animator = ValueAnimator.ofFloat(textHeight, 0f); // offset 반대로
            animator.setDuration(SPLASH_DELAY); // 예: 2초
            animator.addUpdateListener(animation -> {
                float offset = (float) animation.getAnimatedValue();
                LinearGradient movingShader = new LinearGradient(
                        0, -offset, 0, textHeight - offset,
                        new int[]{0x00000000, 0xFF000000}, // 위: 투명, 아래: 검정
                        new float[]{0f, 1f},
                        Shader.TileMode.CLAMP
                );
                splashText.getPaint().setShader(movingShader);
                splashText.invalidate();
            });
            animator.start();
        });


        tokenManager = new TokenManager(this);

        // 일정 시간 후 토큰 검사 및 이동
        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
            String token = prefs.getString("jwt", null);
            String refreshToken = prefs.getString("refreshToken", null);

            if (token != null && refreshToken != null) {
                if (TokenManager.isTokenExpired(token)) {
                    // 토큰 만료 → 리프레시 요청
                    TokenManager.refreshAccessToken(token, refreshToken, this, success -> {
                        runOnUiThread(() -> {
                            if (success) goToFeed();
                            else goToLogin();
                        });
                    });
                } else {
                    // 유효 → 바로 Feed 이동
                    goToFeed();
                }
            } else {
                // 토큰 없음 → 로그인 화면
                goToLogin();
            }
        }, SPLASH_DELAY);
    }

    private void goToFeed() {
        startActivity(new Intent(this, FeedActivity.class));
        finish();
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
