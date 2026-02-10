package com.example.meltingbooks.profile;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.R;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.profile.FollowApi;
import com.example.meltingbooks.network.profile.FollowUser;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FollowUsersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_list);

        // 상태바 색상 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        recyclerView = findViewById(R.id.recyclerViewFollow);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        int userId = getIntent().getIntExtra("userId", -1);
        Log.d("FollowListActivity", "userId: " + userId); //userId 확인용

        String type = getIntent().getStringExtra("type"); // "followers" or "following"

        TextView title = findViewById(R.id.textFollowTitle);
        if ("followers".equals(type)) {
            title.setText("팔로워 목록");
        } else {
            title.setText("팔로잉 목록");
        }

        loadFollowList(userId, type);
    }

    private void loadFollowList(int userId, String type) {

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
        if (token == null || userId == -1) {
            Log.e("Follow", "토큰 또는 사용자 ID가 없습니다.");
            return;
        }

        FollowApi api = ApiClient.getClient(token).create(FollowApi.class);

        Call<ApiResponse<List<FollowUser>>> call;
        if ("followers".equals(type)) {
            call = api.getFollowers("Bearer " + token,userId);
        } else {
            call = api.getFollowing("Bearer " + token,userId);
        }

        call.enqueue(new Callback<ApiResponse<List<FollowUser>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<FollowUser>>> call, Response<ApiResponse<List<FollowUser>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // API 응답 전체 로그 출력
                    Log.d("FollowListActivity", "API Response: " + response.body().toString());

                    // 데이터가 있을 경우 RecyclerView에 반영
                    List<FollowUser> users = response.body().getData();
                    if (users != null) {
                        for (FollowUser user : users) {
                            Log.d("FollowListActivity", "User ID: " + user.getId() +
                                    ", Nickname: " + user.getNickname() +
                                    ", ProfileImageUrl: " + user.getProfileImageUrl());
                        }
                    }

                    adapter = new FollowUsersAdapter(users);
                    recyclerView.setAdapter(adapter);
                } else {
                    Log.e("FollowListActivity", "API 응답 비정상: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<FollowUser>>> call, Throwable t) {
                Log.e("FollowListActivity", "API 호출 실패: " + t.getMessage());
                t.printStackTrace();
            }

        });
    }
}