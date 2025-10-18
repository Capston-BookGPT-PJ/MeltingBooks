package com.example.meltingbooks.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.meltingbooks.feed.FeedActivity;

public class GoogleLoginCallbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uri = getIntent().getData();
        if (uri != null) {
            Log.d("CALLBACK", "Redirect URI: " + uri.toString());

            // ✅ 백엔드가 token, userID 같은 값 붙여서 redirect 해준다고 가정
            String token = uri.getQueryParameter("accessToken");
            String userId = uri.getQueryParameter("userId");

            if (token != null) {
                saveTokenAndUser(token,userId);
                Toast.makeText(this, "로그인 성공: " + userId + token, Toast.LENGTH_SHORT).show();
                //Log.d("CALLBACK", "받은 토큰: " + token); //토큰 확인용
            } else {
                Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show();
            }
        }

        // 피드 화면으로 이동
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