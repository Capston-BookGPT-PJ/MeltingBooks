package com.example.meltingbooks.group;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;


import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.meltingbooks.base.BaseActivity;
import com.example.meltingbooks.group.profile.GroupCreateActivity;
import com.example.meltingbooks.group.profile.GroupProfileActivity;
import com.example.meltingbooks.group.profile.GroupProfileItem;
import com.example.meltingbooks.R;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.group.GroupAllList;
import com.example.meltingbooks.network.group.GroupApi;
import com.example.meltingbooks.network.group.GroupController;
import com.example.meltingbooks.network.group.GroupProfileResponse;
import com.example.meltingbooks.network.group.GroupProfileResponseAdapter;
import com.example.meltingbooks.network.group.feed.GroupPostResponse;
import com.example.meltingbooks.network.group.MyGroup;
import com.example.meltingbooks.search.SearchActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupActivity extends BaseActivity {

    private MaterialButton createGroupButton, searchGroupButton;
    private RecyclerView popularGroupsRecyclerView, recentGroupsRecyclerView;
    private GroupProfileResponseAdapter popularAdapter, recentAdapter;
    private ImageButton myGroup1, myGroup2, myGroup3;
    private List<ImageButton> groupButtons;

    private String token;
    private int userId;

    private GroupApi groupApi;



    // 🔹 그룹 클릭 리스너
    private GroupProfileResponseAdapter.OnItemClickListener listener = group -> {
        String imageUrl = group.getGroupImageUrl();
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = ""; // 기본 이미지
        }

        GroupProfileItem profileItem = new GroupProfileItem(
                group.getName(),
                imageUrl,
                group.getCategory(),
                "그룹 소개",
                group.getDescription()
        );

        Intent intent = new Intent(GroupActivity.this, GroupProfileActivity.class);
        intent.putExtra("groupId", group.getId());
        intent.putExtra("groupProfile", profileItem);
        startActivity(intent);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        setupBottomNavigation();


        // token 받아오기
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("jwt", null);
        userId = prefs.getInt("userId", -1);

        if (token == null || userId == -1) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        createGroupButton = findViewById(R.id.createGroupButton);
        searchGroupButton = findViewById(R.id.searchGroupButton);
        popularGroupsRecyclerView = findViewById(R.id.popularGroupsRecyclerView);
        recentGroupsRecyclerView = findViewById(R.id.recentGroupsRecyclerView);

        setupRecyclerViews();

        createGroupButton.setOnClickListener(v -> {
            Intent intent = new Intent(GroupActivity.this, GroupCreateActivity.class);
            startActivity(intent);
        });

        searchGroupButton.setOnClickListener(v -> {
            Intent intent = new Intent(GroupActivity.this, SearchActivity.class);
            startActivity(intent);
        });


        // 내 그룹 클릭시 -> 각 그룹 피드로 이동
        myGroup1 = findViewById(R.id.myGroup1);
        myGroup2 = findViewById(R.id.myGroup2);
        myGroup3 = findViewById(R.id.myGroup3);
        groupButtons = Arrays.asList(myGroup1, myGroup2, myGroup3);

        groupApi = ApiClient.getClient(token).create(GroupApi.class);

        // 3️⃣ 내 그룹 가져오기
        loadMyGroups();


        // 새 그룹이 있으면 Adapter에 추가
        GroupProfileResponse newGroup = (GroupProfileResponse) getIntent().getSerializableExtra("newGroup");
        if (newGroup != null) {
            List<GroupProfileResponse> list = new ArrayList<>();
            list.add(newGroup);
            popularAdapter.addItem(newGroup);
        }
    }

    private void openGroupActivity(int groupId) {
        Intent intent = new Intent(this, GroupFeedActivity.class);
        intent.putExtra("groupId", groupId); // 그룹 ID 전달
        startActivity(intent);
    }
    private void setupRecyclerViews() {
        //popularGroupsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        //recentGroupsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));


        // 가로 스크롤, 2행
        popularGroupsRecyclerView.setLayoutManager(
                new GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
        );

        recentGroupsRecyclerView.setLayoutManager(
                new GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
        );
        popularAdapter = new GroupProfileResponseAdapter(this, new ArrayList<>(), listener);
        recentAdapter = new GroupProfileResponseAdapter(this, new ArrayList<>(), listener);

        popularGroupsRecyclerView.setAdapter(popularAdapter);
        recentGroupsRecyclerView.setAdapter(recentAdapter);

        fetchGroupsFromServer(); // 서버에서 그룹 가져오기

        // ✅  카드 폭 150dp 유지
        //adjustCardSpacing(popularGroupsRecyclerView, 150);
        //adjustCardSpacing(recentGroupsRecyclerView, 150);
    }
/*
    //✅ 카드 폭을 RecyclerView 폭에 맞춰 2개씩 표시
    private void adjustCardSpacing(RecyclerView recyclerView, int cardWidthDp) {
        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int childCount = recyclerView.getChildCount();
            if (childCount == 0) return;

            int totalWidth = recyclerView.getWidth();
            int cardWidthPx = dpToPx(cardWidthDp);

            int visibleCards = 2; // 한 행에 표시할 카드 수
            int extraSpace = totalWidth - (cardWidthPx * visibleCards);
            int spacing = extraSpace / (visibleCards + 1);

            for (int i = 0; i < childCount; i++) {
                View child = recyclerView.getChildAt(i);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                params.width = cardWidthPx;

                int row = i / visibleCards; // 몇 번째 행인지 계산

                if (i % visibleCards == 0) {
                    // 첫 번째 카드
                    params.leftMargin = spacing;
                    params.rightMargin = spacing / 2;
                } else {
                    // 두 번째 카드
                    params.leftMargin = spacing / 2;
                    params.rightMargin = spacing;

                    // 2행(즉 row==1)만 오른쪽으로 살짝 더 밀기
                    if (row == 1) {
                        params.leftMargin += dpToPx(8); // 8dp 정도 밀기, 필요에 따라 조정
                    }
                }

                child.setLayoutParams(params);
            }
        });
    }


    // dp -> px 변환
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }*/


    //그룹 전체 조회-> GroupAllList 사용
    private void fetchGroupsFromServer() {
        GroupController groupController = new GroupController(this);

        groupController.searchGroups(null, null, new Callback<GroupAllList>() {
            @Override
            public void onResponse(Call<GroupAllList> call, Response<GroupAllList> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GroupProfileResponse> allGroups = response.body().getData(); // 🔹 리스트 접근

                    // 인기 그룹: 멤버 수 기준 상위 4개
                    allGroups.sort((g1, g2) -> Integer.compare(g2.getMemberCount(), g1.getMemberCount()));
                    List<GroupProfileResponse> popularGroups = new ArrayList<>(allGroups.subList(0, Math.min(12, allGroups.size())));

                    // 최신 그룹: 생성일 기준 상위 4개
                    allGroups.sort((g1, g2) -> g2.getCreatedAt().compareTo(g1.getCreatedAt()));
                    List<GroupProfileResponse> recentGroups = new ArrayList<>(allGroups.subList(0, Math.min(12, allGroups.size())));

                    popularAdapter.updateData(popularGroups);
                    recentAdapter.updateData(recentGroups);
                }
            }

            @Override
            public void onFailure(Call<GroupAllList> call, Throwable t) {
                t.printStackTrace();
            }


        });

    }

    //내 그룹 이미지 설정 및 클릭
    private void loadMyGroups() {
        groupApi.getMyGroups().enqueue(new Callback<ApiResponse<List<MyGroup>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MyGroup>>> call,
                                   Response<ApiResponse<List<MyGroup>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MyGroup> groups = response.body().getData();

                    for (int i = 0; i < groups.size() && i < groupButtons.size(); i++) {
                        int groupId = groups.get(i).getGroupId();
                        String groupName = groups.get(i).getName();
                        ImageButton button = groupButtons.get(i);

                        // 그룹 이미지 적용
                        groupApi.getGroupById(groupId).enqueue(new Callback<GroupPostResponse>() {
                            @Override
                            public void onResponse(Call<GroupPostResponse> call, Response<GroupPostResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    String imageUrl = response.body().getData().getGroupImageUrl();

                                    ///모서리 둥글게
                                    int cornerRadiusDp = 16;
                                    int cornerRadiusPx = (int) TypedValue.applyDimension(
                                            TypedValue.COMPLEX_UNIT_DIP,
                                            cornerRadiusDp,
                                            getResources().getDisplayMetrics()
                                    );

                                    RequestOptions requestOptions = new RequestOptions()
                                            .transform(new CenterCrop(), new RoundedCorners(cornerRadiusPx));

                                    if (imageUrl == null || imageUrl.isEmpty()) {
                                        // null 또는 빈 문자열이면 샘플 이미지 표시
                                        Glide.with(GroupActivity.this)
                                                .load(R.drawable.sample_profile)
                                                .apply(requestOptions)
                                                .into(button);
                                    } else {
                                        // 실제 URL 있으면 Glide로 로드
                                        Glide.with(GroupActivity.this)
                                                .load(imageUrl)
                                                .apply(requestOptions)
                                                .into(button);
                                    }
                                }
                            }
                            @Override
                            public void onFailure(Call<GroupPostResponse> call, Throwable t) {
                                Log.e("GroupProfile", "그룹 조회 실패", t);
                            }
                        });

                        // 클릭 시 해당 그룹 피드로 이동
                        button.setOnClickListener(v -> {
                            Intent intent = new Intent(GroupActivity.this, GroupFeedActivity.class);
                            intent.putExtra("groupId", groupId);
                            //intent.putExtra("groupName", groupName);
                            startActivity(intent);
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<MyGroup>>> call, Throwable t) {
                Log.e("MyGroups", "내 그룹 불러오기 실패", t);
            }
        });
    }
    //bottom Navigation의 위치 설정
    @Override
    protected int getCurrentNavItemId() {
        return R.id.Group;
    }
}
