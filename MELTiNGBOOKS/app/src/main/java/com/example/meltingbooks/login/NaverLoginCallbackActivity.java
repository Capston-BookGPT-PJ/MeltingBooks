package com.example.meltingbooks.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.meltingbooks.feed.FeedActivity;

public class NaverLoginCallbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uri = getIntent().getData();
        if (uri != null) {
            Log.d("NAVER_CALLBACK", "Redirect URI: " + uri.toString());

            // 백엔드가 redirect 시에 붙여주는 값 받기
            String token = uri.getQueryParameter("accessToken");
            String userId = uri.getQueryParameter("userId");

            if (token != null && userId != null) {
                saveTokenAndUser(token, userId);
                Toast.makeText(this, "네이버 로그인 성공: " + userId, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "네이버 로그인 실패", Toast.LENGTH_SHORT).show();
            }
        }

        // 로그인 후 피드로 이동
        startActivity(new Intent(this, FeedActivity.class));
        finish();
    }

    private void saveTokenAndUser(String token, String userId) {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        prefs.edit()
                .putString("jwt", token)
                .putInt("userId", Integer.parseInt(userId))
                .apply();
    }
}
