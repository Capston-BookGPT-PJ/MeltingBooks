package com.example.meltingbooks.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.meltingbooks.feed.FeedActivity;
import com.example.meltingbooks.R;

public class LoginActivity extends AppCompatActivity {

    private ImageButton kakaoLoginBtn;
    private ImageButton googleLoginBtn;
    private ImageButton naverLoginBtn;


    private TextView signupText;
    private TextView findAccountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 상태바 색상 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // 버튼 및 뷰 초기화
        kakaoLoginBtn = findViewById(R.id.kakaoLoginBtn);
        naverLoginBtn = findViewById(R.id.naverLoginBtn);
        googleLoginBtn = findViewById(R.id.googleLoginBtn);

        signupText = findViewById(R.id.signUp);
        findAccountText = findViewById(R.id.findAccount);

        // 카카오 로그인 버튼 클릭 이벤트
        kakaoLoginBtn.setOnClickListener(v -> {
            String loginUrl = "http://meltingbooks.o-r.kr:8080/auth/KAKAO";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(loginUrl));
            startActivity(intent);

            // 현재 액티비티는 바로 닫기
            finish();
        });


        // 네이버 로그인 클릭 이벤트
        naverLoginBtn.setOnClickListener(v -> {
            String loginUrl = "http://meltingbooks.o-r.kr:8080/auth/NAVER";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(loginUrl));
            startActivity(intent);

            // 현재 액티비티는 바로 닫기
            finish();
        });

        //구글 로그인
        googleLoginBtn.setOnClickListener(v -> {
            String loginUrl = "http://meltingbooks.o-r.kr:8080/auth/GOOGLE";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(loginUrl));
            startActivity(intent);

            // 현재 액티비티는 바로 닫기
            finish();
        });

        // 하단 링크 클릭 이벤트
        signupText.setOnClickListener(v -> {
            Toast.makeText(this, "회원가입 클릭", Toast.LENGTH_SHORT).show();
            // 회원가입 화면으로 이동
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        findAccountText.setOnClickListener(v -> {
            Toast.makeText(this, "계정 찾기 클릭", Toast.LENGTH_SHORT).show();
            // TODO: 계정 찾기 화면으로 이동
            Intent intent = new Intent(LoginActivity.this, FeedActivity.class);
            startActivity(intent);
        });
    }
}
