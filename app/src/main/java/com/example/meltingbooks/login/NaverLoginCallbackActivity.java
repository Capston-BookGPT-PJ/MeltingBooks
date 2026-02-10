package com.example.meltingbooks.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.meltingbooks.feed.FeedActivity;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.TokenRequestBody;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NaverLoginCallbackActivity extends AppCompatActivity {

    /**@Override
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
                //Toast.makeText(this, "네이버 로그인 성공: " + userId, Toast.LENGTH_SHORT).show();
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
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uri = getIntent().getData();
        if (uri != null) {
            Log.d("NAVER_CALLBACK", "Redirect URI: " + uri.toString());

            // ✅ 백엔드가 token, userID 같은 값 붙여서 redirect 해준다고 가정
            String token = uri.getQueryParameter("accessToken"); //accessToken
            String refreshToken = uri.getQueryParameter("refreshToken"); //refreshToken
            String userId = uri.getQueryParameter("userId");


            if (token != null && refreshToken != null) {
                saveTokenAndUser(token, refreshToken, userId);
                //Toast.makeText(this, "로그인 성공: " + userId + token, Toast.LENGTH_SHORT).show();
                //Log.d("CALLBACK", "받은 토큰: " + token); //토큰 확인용

                Log.d("FCM_DEBUG", "Calling fetchAndSendFcmTokenToServer() with userId=" + userId);
                // 서버에 FCM 토큰 전송
                fetchAndSendFcmTokenToServer(Integer.parseInt(userId), token);

            } else {
                Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show();
            }
        }

        // 피드 화면으로 이동
        startActivity(new Intent(this, FeedActivity.class));
        finish();
    }

    private void saveTokenAndUser(String token, String refreshToken, String userId) {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        prefs.edit()
                .putString("jwt", token)
                .putString("refreshToken", refreshToken)
                .putInt("userId", Integer.parseInt(userId))
                .apply();
    }
    // 기존과 동일한 위치에 넣을 fetchAndSendFcmTokenToServer
    private void fetchAndSendFcmTokenToServer(int userId, String jwt) {
        Log.d("FCM_DEBUG", "fetchAndSendFcmTokenToServer() called. userId=" + userId + " jwtPresent=" + (jwt != null));
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM_ERR", "getToken failed", task.getException()); // 토큰 조회 자체 실패
                        return;
                    }
                    String fcmToken = task.getResult();
                    Log.d("FCM_DEBUG", "getToken succeeded. fcmTokenNull? " + (fcmToken == null));

                    // (디버그) 토큰 길이 확인 — 토큰이 너무 짧으면 문제의심
                    if (fcmToken != null) {
                        Log.d("FCM_DEBUG", "fcm token length=" + fcmToken.length());
                    }

                    // SharedPreferences 저장 직전/직후 로그
                    SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                    Log.d("FCM_DEBUG", "Saving fcmToken to SharedPreferences...");
                    prefs.edit().putString("fcmToken", fcmToken).apply();
                    Log.d("FCM_DEBUG", "Saved fcmToken to SharedPreferences");

                    // 서버 전송 호출 직전 로그
                    Log.d("FCM_DEBUG", "Calling sendTokenToServer(userId=" + userId + ")");
                    sendTokenToServer(userId, jwt, fcmToken);
                });
    }


    // 기존과 동일한 위치에 넣을 sendTokenToServer
    private void sendTokenToServer(int userId, String jwt, String fcmToken) {
        // 아주 간단한 사전 체크와 로그들
        Log.d("FCM_DEBUG", "sendTokenToServer() called. userId=" + userId
                + " jwtPresent=" + (jwt != null)
                + " fcmTokenNull? " + (fcmToken == null));

        if (jwt == null) {
            Log.w("FCM_ERR", "JWT is null — cannot authenticate to server. Save pending or prompt login.");
            return;
        }
        if (fcmToken == null || fcmToken.isEmpty()) {
            Log.w("FCM_ERR", "FCM token is null/empty — abort sending to server.");
            return;
        }

        // Retrofit 클라이언트 생성 (기존대로)
        ApiService apiService = ApiClient.getClient(jwt).create(ApiService.class);

        //String deviceInfo = android.os.Build.MODEL + " / Android " + android.os.Build.VERSION.SDK_INT;
        //TokenRequestBody body = new TokenRequestBody(fcmToken, deviceInfo);

        String deviceInfo = android.os.Build.MODEL + " / Android " + android.os.Build.VERSION.SDK_INT;

        String deviceIdentifier = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        TokenRequestBody body = new TokenRequestBody(fcmToken, deviceInfo, deviceIdentifier);

        // (디버그) 보낼 페이로드 내용 로그 (토큰 일부만 마스킹해서 노출)
        String tokenPreview = fcmToken.length() > 12 ? fcmToken.substring(0, 6) + "..." + fcmToken.substring(fcmToken.length() - 6) : fcmToken;
        Log.d("FCM_DEBUG", "Prepared TokenRequestBody deviceInfo=" + deviceInfo + " tokenPreview=" + tokenPreview);

        Call<Void> call = apiService.registerToken("Bearer " + jwt, userId, body);

        Log.d("FCM_DEBUG", "Enqueuing Retrofit call to register token...");
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("FCM_DEBUG", "token registered on server. HTTP " + response.code());
                } else {
                    Log.w("FCM_ERR", "token register failed: HTTP " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            String err = response.errorBody().string();
                            Log.w("FCM_ERR", "server error body: " + err);
                        }
                    } catch (Exception e) {
                        Log.w("FCM_ERR", "error reading errorBody", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("FCM_ERR", "token register error (network/conversion)", t);
            }
        });
    }
}