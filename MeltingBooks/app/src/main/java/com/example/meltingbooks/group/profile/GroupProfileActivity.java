package com.example.meltingbooks.group.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;
import com.example.meltingbooks.group.GroupFeedActivity;
import com.example.meltingbooks.group.menu.GroupMemberItem;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.group.GroupController;
import com.example.meltingbooks.network.group.feed.GroupPostResponse;
import com.example.meltingbooks.network.group.GroupProfileResponse;
import com.example.meltingbooks.network.profile.UserResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupProfileActivity extends AppCompatActivity {

    private TextView groupName;
    private ImageView groupIntroImage;
    private TextView groupCategory;
    private TextView groupIntroTile;
    private TextView groupIntroDetail;
    private ImageButton joinGroupButton;
    private ImageButton updateGroupButton;
    private ImageButton goToGroupButton;

    private GroupProfileResponse groupInfo;
    private UserResponse currentUser;

    private int groupId;
    private int currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_profile);

        // 상태바 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // 뷰 초기화
        groupName = findViewById(R.id.groupName);
        groupIntroImage = findViewById(R.id.groupIntroImage);
        groupCategory = findViewById(R.id.groupCategory);
        groupIntroTile = findViewById(R.id.groupIntroTile);
        groupIntroDetail = findViewById(R.id.groupIntroDetail);
        joinGroupButton = findViewById(R.id.joinGroupButton);
        updateGroupButton = findViewById(R.id.group_update_btn);
        goToGroupButton = findViewById(R.id.go_to_group_btn);

        // SharedPreferences에서 로그인 유저 ID 가져오기
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        currentUserId = prefs.getInt("userId", -1);

        // Intent로 전달받은 그룹 ID
        groupId = getIntent().getIntExtra("groupId", -1);
        if (groupId == -1) {
            Toast.makeText(this, "그룹 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 버튼 기본 숨김
        joinGroupButton.setVisibility(View.GONE);
        updateGroupButton.setVisibility(View.GONE);
        goToGroupButton.setVisibility(View.GONE);

        // 1️⃣ 그룹 정보 가져오기
        fetchGroupInfo();
    }

    private void fetchGroupInfo() {
        GroupController groupController = new GroupController(this);
        groupController.getGroupById(groupId, new Callback<GroupPostResponse>() {
            @Override
            public void onResponse(Call<GroupPostResponse> call, Response<GroupPostResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    groupInfo = response.body().getData();
                    updateUI(groupInfo);
                    // 2️⃣ 그룹 정보 준비 후, 유저 정보 가져오기
                    loadUserProfile(currentUserId);
                } else {
                    Toast.makeText(GroupProfileActivity.this,
                            "그룹 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GroupPostResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(GroupProfileActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(GroupProfileResponse group) {
        groupName.setText(group.getName());
        groupCategory.setText(group.getCategory());
        groupIntroTile.setText("그룹 소개");
        groupIntroDetail.setText(group.getDescription());

        String imageUrl = group.getGroupImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            groupIntroImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUrl).into(groupIntroImage);
        } else {
            groupIntroImage.setVisibility(View.GONE);
        }
    }

    private void loadUserProfile(int viewedUserId) {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
        if (token == null) return;

        ApiService apiService = ApiClient.getClient(token).create(ApiService.class);
        Call<UserResponse> call = apiService.getUser("Bearer " + token, viewedUserId);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();

                    if (currentUser.getGroupNames() != null) {
                        Log.d("UserProfile", "가입 그룹 수: " + currentUser.getGroupNames().size());
                        Log.d("UserProfile", "가입 그룹 이름: " + currentUser.getGroupNames().toString());
                    } else {
                        Log.d("UserProfile", "가입 그룹 정보가 없습니다.");
                    }

                    // 3️⃣ 그룹 + 유저 정보 모두 준비 완료 → 버튼 세팅
                    setupButtons();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void setupButtons() {
        if (groupInfo == null || currentUser == null) return;

        // 버튼 초기화
        updateGroupButton.setVisibility(View.GONE);
        joinGroupButton.setVisibility(View.GONE);
        goToGroupButton.setVisibility(View.GONE);

        final int userGroupCount = currentUser.getGroupNames() != null ?
                currentUser.getGroupNames().size() : 0;

        if (currentUserId == groupInfo.getOwnerId()) {
            // 그룹장
            updateGroupButton.setVisibility(View.VISIBLE);
            updateGroupButton.setOnClickListener(v -> {
                Intent intent = new Intent(GroupProfileActivity.this, GroupUpdateActivity.class);
                intent.putExtra("groupInfo", groupInfo); // Serializable 전달
                startActivity(intent);
                finish();
            });
        } else if (isMember(currentUserId)) {
            // 이미 가입된 멤버
            goToGroupButton.setVisibility(View.VISIBLE);
            goToGroupButton.setOnClickListener(v -> {
                Toast.makeText(this, "이미 그룹에 가입되어 있습니다.", Toast.LENGTH_SHORT).show();
            });
        } else {
            // 가입되지 않은 일반 멤버
            joinGroupButton.setVisibility(View.VISIBLE);
            joinGroupButton.setOnClickListener(v -> {
                if (userGroupCount >= 3) {
                    Toast.makeText(this, "그룹은 최대 3개까지 가입할 수 있습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (groupInfo.getId() != -1) {
                    new GroupController(this).joinGroup(groupInfo.getId());
                    Toast.makeText(this, "그룹 가입 신청 완료" +
                            ".", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "그룹 ID가 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean isMember(int userId) {
        if (groupInfo.getMembers() == null) return false;
        for (GroupMemberItem member : groupInfo.getMembers()) {
            if (member.getUserId() == userId) return true;
        }
        return false;
    }
}
