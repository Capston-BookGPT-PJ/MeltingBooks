package com.example.meltingbooks.profile;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.meltingbooks.R;
import com.example.meltingbooks.login.LoginActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SettingApp extends AppCompatActivity {
    private String baseUrl = "http://meltingbooks.o-r.kr:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_app);

        // 상태바 색상
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // SharedPreferences에서 토큰, refreshToken, userId 가져오기
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
        String refreshToken = prefs.getString("refreshToken", null);
        String userId = String.valueOf(prefs.getInt("userId", -1));

        // TextView 연결
        TextView logout = findViewById(R.id.logout);
        TextView deleteAccount = findViewById(R.id.deleteAccount);
        TextView feedback = findViewById(R.id.feedback);
        TextView versionInfo = findViewById(R.id.versionInfo);
        TextView policy = findViewById(R.id.policy);
        TextView notification = findViewById(R.id.pushToggle);

        logout.setOnClickListener(v -> new Thread(() -> {
            try {
                URL url = new URL(baseUrl + "auth/logout");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("refreshToken", refreshToken);

                // 요청 바디 로그
                //Log.d("LogoutRequest", "Request JSON: " + body.toString());

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(body.toString());
                writer.flush();
                writer.close();

                int responseCode = conn.getResponseCode();
                //Log.d("LogoutResponse", "Response code: " + responseCode);

                // 서버 응답 본문 읽기 (204인 경우 빈 스트림)
                StringBuilder responseBody = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream()
                        )
                )) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBody.append(line);
                    }
                } catch (Exception e) {
                    Log.d("LogoutResponse", "No response body or empty (204)");
                }

                //Log.d("LogoutResponse", "Response body: " + responseBody.toString());

                runOnUiThread(() -> {
                    if (responseCode == 200 || responseCode == 204) {
                        prefs.edit().clear().apply();
                        Toast.makeText(SettingApp.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SettingApp.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(SettingApp.this, "로그아웃 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(SettingApp.this, "에러 발생: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start());


        // 계정 탈퇴 (확인 다이얼로그 포함)
        deleteAccount.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("계정 탈퇴")
                .setMessage("정말로 계정을 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.")
                .setPositiveButton("탈퇴", (dialog, which) -> deleteUserAccount(token, userId, prefs))
                .setNegativeButton("취소", null)
                .show());

        // 문의하기 / 피드백
        feedback.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:meltingbookscs@gmail.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[MELTiNG BOOKS] 문의 / 피드백");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "문의 내용을 작성해주세요.\n\n");
            try {
                startActivity(Intent.createChooser(emailIntent, "이메일 보내기"));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "이메일 앱이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 버전 정보
        versionInfo.setOnClickListener(v -> {
            String versionName = "1.0.0"; // BuildConfig.VERSION_NAME 사용 가능
            Toast.makeText(this, "현재 앱 버전: " + versionName, Toast.LENGTH_SHORT).show();
        });

        // 약관 및 개인정보 처리방침
        policy.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.setting_fragment_container, new TermsPolicyFragment())
                    .addToBackStack(null)
                    .commit();
        });

        notification.setOnClickListener(v -> {
            Intent intent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0 이상: 앱별 알림 설정 화면
                intent = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, getPackageName());
            } else {
                // Android 8.0 미만: 앱 정보 화면으로 이동
                intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:" + getPackageName()));
            }
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "설정 화면을 열 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });


    }

    // 계정 탈퇴 함수
    private void deleteUserAccount(String token, String userId, SharedPreferences prefs) {
        new Thread(() -> {
            try {
                URL url = new URL(baseUrl + "api/users/" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("Authorization", "Bearer " + token);

                int responseCode = conn.getResponseCode();
                runOnUiThread(() -> {
                    if (responseCode == 200 || responseCode == 204) {
                        // SharedPreferences 초기화
                        prefs.edit().clear().apply();

                        Toast.makeText(this, "계정이 탈퇴되었습니다.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "계정 탈퇴 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "에러 발생: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}
