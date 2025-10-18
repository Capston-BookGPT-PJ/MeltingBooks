package com.example.meltingbooks.group.profile;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import com.example.meltingbooks.group.GroupActivity;
import com.example.meltingbooks.network.group.GroupController;
import com.example.meltingbooks.network.group.feed.GroupPostResponse;
import com.example.meltingbooks.network.group.GroupProfileResponse;

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
    private GroupProfileResponse groupInfo;

    private int groupId; // 서버에서 가져올 그룹 ID

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_profile);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        groupName = findViewById(R.id.groupName);
        groupIntroImage = findViewById(R.id.groupIntroImage);
        groupCategory = findViewById(R.id.groupCategory);
        groupIntroTile = findViewById(R.id.groupIntroTile);
        groupIntroDetail = findViewById(R.id.groupIntroDetail);
        joinGroupButton = findViewById(R.id.joinGroupButton);
        updateGroupButton = findViewById(R.id.group_update_btn);
        // Intent로 받은 그룹 ID
        groupId = getIntent().getIntExtra("groupId", -1);
        if (groupId == -1) {
            Toast.makeText(this, "그룹 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchGroupInfo();

        // 기본은 숨김
        joinGroupButton.setVisibility(View.GONE);
        updateGroupButton.setVisibility(View.GONE);

    }

    // 그룹 단일 타입-> GroupSingleList 사용
    private void fetchGroupInfo() {
        GroupController groupController = new GroupController(this);

        groupController.getGroupById(groupId, new Callback<GroupPostResponse>() {
            @Override
            public void onResponse(Call<GroupPostResponse> call, Response<GroupPostResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    GroupProfileResponse group = response.body().getData();
                    updateUI(group);
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
        groupInfo = group;
        groupName.setText(group.getName());
        groupCategory.setText(group.getCategory());
        groupIntroTile.setText("그룹 소개");
        groupIntroDetail.setText(group.getDescription());

        String imageUrl = group.getGroupImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            groupIntroImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(imageUrl)
                    .into(groupIntroImage);
        } else {
            groupIntroImage.setVisibility(View.GONE);
        }

        // ✅ 그룹 정보 로드 완료 후 버튼 세팅
        setupButtons();
    }


    private void setupButtons() {
        int currentUserId = getSharedPreferences("auth", MODE_PRIVATE)
                .getInt("userId", -1);

        if (groupInfo != null && currentUserId == groupInfo.getOwnerId()) {
            // 그룹장인 경우
            updateGroupButton.setVisibility(View.VISIBLE);
            joinGroupButton.setVisibility(View.GONE);
            updateGroupButton.setOnClickListener(v -> {
                Intent intent = new Intent(GroupProfileActivity.this, GroupUpdateActivity.class);
                intent.putExtra("groupInfo", groupInfo); // Serializable 전달
                startActivity(intent);
            });
        } else {
            // 일반 회원인 경우
            joinGroupButton.setVisibility(View.VISIBLE);
            updateGroupButton.setVisibility(View.GONE);
            joinGroupButton.setOnClickListener(v -> {
                if (groupId != -1) {
                    new GroupController(this).joinGroup(groupId);
                    finish();
                    startActivity(new Intent(GroupProfileActivity.this, GroupActivity.class));
                } else {
                    Toast.makeText(this, "그룹 ID가 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
