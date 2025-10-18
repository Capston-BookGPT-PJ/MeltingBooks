package com.example.meltingbooks.group;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.meltingbooks.R;
import com.example.meltingbooks.base.BaseActivity;
import com.example.meltingbooks.calendar.utils.ProgressBarUtil;
import com.example.meltingbooks.calendar.view.CircularProgressView;
import com.example.meltingbooks.calendar.view.GoalProgressView;
import com.example.meltingbooks.group.goal.GroupGoalFragment;
import com.example.meltingbooks.group.goal.GroupSetGoalFragment;
import com.example.meltingbooks.group.menu.GroupJoinRequestAdapter;
import com.example.meltingbooks.group.menu.GroupListAdapter;
import com.example.meltingbooks.group.menu.GroupListItem;
import com.example.meltingbooks.group.menu.GroupMemberAdapter;
import com.example.meltingbooks.group.menu.GroupMemberItem;
import com.example.meltingbooks.group.write.GroupWriteActivity;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.goal.GoalApi;
import com.example.meltingbooks.network.goal.GoalController;
import com.example.meltingbooks.network.goal.GoalResponse;
import com.example.meltingbooks.network.group.feed.CreateGroupNotice;
import com.example.meltingbooks.network.group.feed.CreateGroupRecommend;
import com.example.meltingbooks.network.group.GroupApi;
import com.example.meltingbooks.network.group.comment.GroupCommonResponse;
import com.example.meltingbooks.network.group.GroupController;
import com.example.meltingbooks.network.group.feed.GroupFeedPageResponse;
import com.example.meltingbooks.network.group.feed.GroupFeedResponse;
import com.example.meltingbooks.network.group.GroupJoinRequestResponse;
import com.example.meltingbooks.network.group.GroupProfileResponse;
import com.example.meltingbooks.network.group.feed.GroupPostResponse;
import com.example.meltingbooks.network.group.feed.GroupReviewResponse;
import com.example.meltingbooks.network.group.MyGroup;
import com.example.meltingbooks.network.group.goal.GroupGoalApi;
import com.example.meltingbooks.network.group.goal.GroupGoalController;
import com.example.meltingbooks.network.group.goal.GroupGoalResponse;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupFeedActivity extends BaseActivity {

    private int groupId;
    private String groupName;

    private EditText noticeTitleEditText, noticeContentEditText;
    private ImageButton noticeSubmitButton;

    private EditText recommendTitleEditText, recommendContentEditText;
    private ImageButton  recommendSubmitButton;
    private ImageButton groupWriteButton;
    private RecyclerView groupRecyclerView, groupMenuRecyclerView, groupMemberRecyclerView;

    private ImageButton menuButton;
    private View groupScrollView, menuLayout;
    private GestureDetector gestureDetector;
    private GroupProfileResponse groupInfo;
    private TextView groupTitle;

    private String token;
    private int userId;

    private GroupApi groupApi;
    private ApiService apiService;


    private RecyclerView groupFeedRecyclerView;
    private GroupFeedAdapter groupFeedAdapter;
    private List<GroupFeedItem> groupFeedList = new ArrayList<>(); //Null 방지 초기화
    private boolean isRefreshingFromIntent = false;

    //그룹요청
    private RecyclerView groupJoinRecyclerView;
    private GroupJoinRequestAdapter joinRequestAdapter;
    private List<GroupJoinRequestResponse.JoinRequest> joinRequestList = new ArrayList<>();
    private GroupMemberAdapter memberAdapter;

    private GroupGoalController goalController;

    /*// --- GoalProgressView 변수 선언 ---
    private GoalProgressView goal1;
    private GoalProgressView goal2;
    private GoalProgressView goal3;*/

    //⭐새로 고침 및 무한 스크롤 관련 변수
    private SwipeRefreshLayout swipeRefreshLayout; //⭐
    private int currentPage = 0; //⭐ 페이징 현재 페이지
    private final int PAGE_SIZE = 10; // ⭐한 페이지에 불러올 항목 수
    private boolean isLoading = false; //⭐
    private boolean isLastPage = false; //⭐

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_feed);
        setupBottomNavigation();



        // 상태바 색상 조정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // token 받아오기
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("jwt", null);
        userId = prefs.getInt("userId", -1);

        if (token == null || userId == -1) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        groupApi = ApiClient.getClient(token).create(GroupApi.class);
        apiService = ApiClient.getClient(token).create(ApiService.class);
        GroupGoalApi groupGoalApi = ApiClient.getClient(token).create(GroupGoalApi.class);

        // 그룹 Id
        groupId = getIntent().getIntExtra("groupId", -1);
        Log.d("GroupFeedActivity", "현재 그룹 ID: " + groupId);
        if (groupId == -1) {
            Toast.makeText(this, "그룹 정보가 없습니다. 일부 기능이 비활성화됩니다.", Toast.LENGTH_SHORT).show();
        }


        // 그룹명
        groupTitle = findViewById(R.id.groupTitle);

        groupName = getIntent().getStringExtra("groupName");
        Log.d("GroupFeedActivity", "현재 그룹 Name: " + groupName);
        // 그룹명이 있으면 세팅, 없으면 기본값
        if (groupName != null) {
            groupTitle.setText(groupName);
        } else {
            groupTitle.setText("그룹");
        }


        //리사이클러뷰 설정
        groupFeedRecyclerView = findViewById(R.id.groupRecyclerView);

        groupFeedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupFeedAdapter = new GroupFeedAdapter(this, groupFeedList, groupId, feedDetailLauncher);;
        groupFeedRecyclerView.setAdapter(groupFeedAdapter);


        //⭐ 무한 스크롤 리스너 추가
        groupFeedRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (layoutManager != null && !isLoading) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // 마지막 항목에 도달하면 다음 페이지 호출
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        if (!isLastPage) {
                            currentPage++;
                            loadGroupFeeds(groupId, false); // 다음 페이지 로드
                        }
                    }
                }
            }
        });

        //⭐ 새로고침 관련 뷰
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        //⭐ 새로고침 동작
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 0;
            loadGroupFeeds(groupId, true); // true: 새로고침
        });


        //서버에서 피드 불러오기
        loadGroupFeeds(groupId, false);

        // --- 공지 뷰 초기화 ---
        noticeTitleEditText = findViewById(R.id.noticeTitleEditText);
        noticeContentEditText = findViewById(R.id.noticeContentEditText);
        noticeSubmitButton = findViewById(R.id.noticeSubmitButton);

        // --- 추천 도서 뷰 초기화 ---
        recommendTitleEditText = findViewById(R.id.recommendTitleEditText);
        recommendContentEditText = findViewById(R.id.recommendContentEditText);
        recommendSubmitButton = findViewById(R.id.recommendSubmitButton);

        groupWriteButton = findViewById(R.id.groupWrite);
        groupScrollView = findViewById(R.id.groupScrollView);


        menuLayout = findViewById(R.id.group_menu_button_layout);
        menuLayout.setVisibility(View.GONE);
        menuLayout.setClickable(true);
        menuLayout.setFocusable(true);
        menuLayout.setFocusableInTouchMode(true);

        menuButton = findViewById(R.id.menuButton);
       // menuButton.setOnClickListener(v -> showGroupInfo());

        // 메뉴 버튼 클릭 시 토글
        menuButton.setOnClickListener(v -> {
            if (menuLayout.getVisibility() == View.VISIBLE) {
                hideGroupInfoAndFinish();
            } else {
                showGroupInfo();
            }
        });



        gestureDetector = new GestureDetector(this, new SwipeGestureListener());
        menuLayout.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        groupWriteButton.setOnClickListener(v -> {
            Intent intent = new Intent(GroupFeedActivity.this, GroupWriteActivity.class);
            intent.putExtra("groupId", groupId); // ✅ 반드시 전달
            startActivity(intent);
        });


        // 그룹 탈퇴/삭제 버튼 클릭
        menuLayout.findViewById(R.id.btn_leave_group).setOnClickListener(v -> handleLeaveOrDeleteGroup());

        ///그룹 메뉴
        // 내 그룹 리스트
        groupMenuRecyclerView = menuLayout.findViewById(R.id.myGroupRecyclerView);
        if(groupMenuRecyclerView == null){
            Log.e("GroupFeedActivity", "groupMenuRecyclerView is null!");
        }
        groupMenuRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // ← 꼭 먼저
        List<GroupListItem> menuGroupList = new ArrayList<>();
        GroupListAdapter adapter = new GroupListAdapter(menuGroupList, groupId); // groupId = 현재 피드 그룹
        groupMenuRecyclerView.setAdapter(adapter);

        // 그룹 멤버 RecyclerView
        groupMemberRecyclerView = menuLayout.findViewById(R.id.groupMemberRecyclerView);
        groupMemberRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<GroupMemberItem> menuMemberList = new ArrayList<>();
        memberAdapter = new GroupMemberAdapter(menuMemberList, userId, -1); // -1 = 임시 ownerId
        groupMemberRecyclerView.setAdapter(memberAdapter);

        // 서버 호출은 Activity에서 처리
        memberAdapter.setOnDelegateClickListener(member -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("그룹장 위임")
                    .setMessage(member.getNickname() + "님에게 그룹장을 위임하시겠습니까?")
                    .setPositiveButton("예", (dialog, which) -> {
                        delegateGroupOwner(member.getUserId(), memberAdapter);
                    })
                    .setNegativeButton("아니오", null)
                    .show();
        });

        fetchGroupInfo(memberAdapter);


        // 내 그룹 조회
        groupApi.getMyGroups().enqueue(new Callback<ApiResponse<List<MyGroup>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MyGroup>>> call,
                                   Response<ApiResponse<List<MyGroup>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MyGroup> myGroups = response.body().getData();

                    // 각 내 그룹의 프로필 API 조회
                    for (MyGroup g : myGroups) {
                        groupApi.getGroupById(g.getGroupId()).enqueue(new Callback<GroupPostResponse>() {
                            @Override
                            public void onResponse(Call<GroupPostResponse> call, Response<GroupPostResponse> res) {
                                if (res.isSuccessful() && res.body() != null) {
                                    GroupPostResponse groupDetail = res.body();

                                    // 내 그룹 RecyclerView에 추가
                                    GroupListItem item = new GroupListItem(
                                            groupDetail.getData().getId(),
                                            groupDetail.getData().getName(),
                                            groupDetail.getData().getGroupImageUrl()
                                    );

                                    // ✅ 현재 피드 그룹이면 맨 위로 추가
                                    if (groupDetail.getData().getId() == groupId) {
                                        menuGroupList.add(0, item); // 맨 위로 추가
                                    } else {
                                        menuGroupList.add(item); // 일반 그룹은 뒤에 추가
                                    }
                                    Log.d("GroupFeed", "불러온 게시글 개수: " + groupFeedList.size());
                                    adapter.notifyDataSetChanged(); // 데이터 갱신
                                }
                            }

                            @Override
                            public void onFailure(Call<GroupPostResponse> call, Throwable t) {
                                Log.e("GroupAPI", "그룹 프로필 조회 실패", t);
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<MyGroup>>> call, Throwable t) {
                Log.e("GroupAPI", "내 그룹 조회 실패", t);
            }
        });


        // 그룹 멤버 조회
        groupApi.getGroupById(groupId).enqueue(new Callback<GroupPostResponse>() {
            @Override
            public void onResponse(Call<GroupPostResponse> call, Response<GroupPostResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GroupProfileResponse groupData = response.body().getData();
                    int groupId = groupData.getId();

                    List<GroupMemberItem> memberItems = groupData.getMembers();

                    // 본인이면 맨 위로
                    List<GroupMemberItem> sortedMembers = new ArrayList<>();

                    for (GroupMemberItem member : memberItems) {
                        member.setGroupId(groupId); // groupId 세팅

                        if (member.getUserId() == userId) {
                            // 본인은 맨 위로 추가
                            sortedMembers.add(0, member);
                        } else {
                            // 나머지는 뒤로 추가
                            sortedMembers.add(member);
                        }
                    }

                    // RecyclerView에 반영
                    menuMemberList.clear();
                    menuMemberList.addAll(memberItems);
                    memberAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<GroupPostResponse> call, Throwable t) {
                Log.e("GroupAPI", "그룹 멤버 조회 실패", t);
            }
        });

        goalController = new GroupGoalController(groupGoalApi);// groupInfo는 Activity에서 서버 호출 후 받은 그룹 정보 객체


    }

    // 피드 갱신
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && !isRefreshingFromIntent) {
            isRefreshingFromIntent = true; // ✅ 중복 호출 방지

            Log.d("GroupFeedRefresh", "onNewIntent 호출됨");

            GroupFeedResponse.Post updatedPost =
                    (GroupFeedResponse.Post) intent.getSerializableExtra("updatedPost");
            if (updatedPost != null) {
                Log.d("GroupFeedRefresh", "updatedPost 존재");
                updatePostInList(updatedPost);
                isRefreshingFromIntent = false;
                return;
            }

            boolean refresh = intent.getBooleanExtra("refreshPost", false);
            Log.d("GroupFeedRefresh", "refreshPost: " + refresh);

            if (refresh) {
                currentPage = 0;
                groupFeedList.clear();
                loadGroupFeeds(groupId, true);
                // ✅ 그룹 정보도 다시 불러오기
                fetchGroupInfo(memberAdapter);
            }

            // ✅ 1초 뒤 플래그 해제 (다음 Intent 대비)
            new Handler(Looper.getMainLooper()).postDelayed(() -> isRefreshingFromIntent = false, 1000);
        }
    }

    private void loadGroupFeeds(int groupId, boolean isRefresh) {
        if (isLoading) return; // 중복 로딩 방지
        isLoading = true; // 로딩 시작

        groupApi = ApiClient.getClient(token).create(GroupApi.class);

        Call<ApiResponse<GroupFeedPageResponse>> call =
                groupApi.getGroupFeed("Bearer " + token, groupId, currentPage, PAGE_SIZE);

        call.enqueue(new Callback<ApiResponse<GroupFeedPageResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<GroupFeedPageResponse>> call,
                                   Response<ApiResponse<GroupFeedPageResponse>> response) {
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null &&
                        response.body().getData() != null) {

                    GroupFeedPageResponse pageResponse = response.body().getData();
                    List<GroupFeedResponse.Post> posts = pageResponse.getPosts().getContent();
                    List<GroupFeedResponse.Post> notices = pageResponse.getNotices();
                    List<GroupFeedResponse.Post> recommendedBooks = pageResponse.getRecommendedBooks();
                    List<GroupFeedResponse.Post> goals = pageResponse.getGoals();

                    // 새로고침이면 리스트 초기화
                    if (isRefresh) {
                        currentPage = 0;
                        groupFeedList.clear();
                    }

                    // 공지/추천 단일 세팅
                    if (notices != null && !notices.isEmpty()) {
                        setSingleNoticeOrRecommend(notices.get(0), "NOTICE", groupId);
                    }
                    if (recommendedBooks != null && !recommendedBooks.isEmpty()) {
                        setSingleNoticeOrRecommend(recommendedBooks.get(0), "RECOMMENDED_BOOK", groupId);
                    }

                    // 단일 게시글 비동기 처리용 임시 리스트
                    List<GroupFeedItem> tempList = Collections.synchronizedList(new ArrayList<>());
                    AtomicInteger counter = new AtomicInteger(
                            (posts != null ? posts.size() : 0) + (goals != null ? goals.size() : 0)
                    );

                    // 리뷰 게시글 처리
                    if (posts != null) {
                        for (GroupFeedResponse.Post post : posts) {
                            addSinglePost(post, "REVIEW", groupId, tempList, counter);
                        }
                    }

                    // 목표 게시글 처리
                    if (goals != null) {
                        for (GroupFeedResponse.Post post : goals) {
                            addSinglePost(post, "GOAL_SHARE", groupId, tempList, counter);
                        }
                    }

                    isLastPage = pageResponse.getPosts().isLast(); // 마지막 페이지 여부 업데이트
                    Log.d("GroupFeed", "전체 페이지: " + pageResponse.getPosts().getTotalPages()
                            + ", 마지막 페이지 여부: " + pageResponse.getPosts().isLast());

                } else {
                    Log.e("GroupFeed", "GroupFeed 응답 비정상: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<GroupFeedPageResponse>> call, Throwable t) {
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
                Log.e("GroupFeed", "GroupFeed API 실패: " + t.getMessage());
            }
        });
    }

    // 단일 게시글 처리 함수 (임시 리스트, 카운터 추가)
    private void addSinglePost(GroupFeedResponse.Post post, String postType, int groupId,
                               List<GroupFeedItem> tempList, AtomicInteger counter) {

        groupApi.getPost("Bearer " + token, groupId, post.getReviewId(), post.getUserId())
                .enqueue(new Callback<ApiResponse<GroupReviewResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<GroupReviewResponse>> call,
                                           Response<ApiResponse<GroupReviewResponse>> reviewResponse) {

                        if (reviewResponse.isSuccessful() && reviewResponse.body() != null &&
                                reviewResponse.body().getData() != null) {

                            GroupReviewResponse review = reviewResponse.body().getData();

                            String createdAtFormatted = post.getCreatedAt();
                            if (createdAtFormatted != null && createdAtFormatted.length() >= 19) {
                                createdAtFormatted = createdAtFormatted.substring(0, 19).replace("T", " ");
                            }

                            List<String> firstImages = (review.getImageUrls() != null) ? review.getImageUrls() : new ArrayList<>();


                            GroupFeedItem item = new GroupFeedItem(
                                    postType,
                                    post.getNickname(),
                                    review.getTitle(),
                                    post.getContent(),
                                    createdAtFormatted,
                                    firstImages,
                                    post.getUserProfileImage(),
                                    post.getCommentCount(),
                                    post.getLikeCount(),
                                    post.getTagId(),
                                    groupId,
                                    post.getUserId()
                            );
                            item.setPostId(post.getReviewId());
                            tempList.add(item);
                        } else {
                            Log.e("GroupFeed", "단일 게시글 조회 실패: " + reviewResponse.message());
                        }

                        // 모든 단일 게시글 호출이 끝나면 정렬 후 추가
                        if (counter.decrementAndGet() == 0) {
                            tempList.sort(Comparator.comparing(GroupFeedItem::getCreatedAt).reversed());
                            groupFeedList.addAll(tempList);
                            groupFeedAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<GroupReviewResponse>> call, Throwable t) {
                        Log.e("GroupFeed", "단일 게시글 API 실패: " + t.getMessage());
                        if (counter.decrementAndGet() == 0) {
                            tempList.sort(Comparator.comparing(GroupFeedItem::getCreatedAt).reversed());
                            groupFeedList.addAll(tempList);
                            groupFeedAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }


    // 공지/추천 단일 게시글 세팅
    private void setSingleNoticeOrRecommend(GroupFeedResponse.Post post, String postType, int groupId) {
        if (post == null) return;

        String firstImage = null;

        groupApi.getPost("Bearer " + token, groupId, post.getReviewId(), post.getUserId())
                .enqueue(new Callback<ApiResponse<GroupReviewResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<GroupReviewResponse>> call,
                                           Response<ApiResponse<GroupReviewResponse>> response) {
                        if (response.isSuccessful() && response.body() != null &&
                                response.body().getData() != null) {

                            GroupReviewResponse review = response.body().getData();

                            EditText titleEditText;
                            EditText contentEditText;

                            if ("NOTICE".equals(postType)) {
                                titleEditText = noticeTitleEditText;
                                contentEditText = noticeContentEditText;
                            } else if ("RECOMMENDED_BOOK".equals(postType)) {
                                titleEditText = recommendTitleEditText;
                                contentEditText = recommendContentEditText;
                            } else {
                                return;
                            }

                            titleEditText.setText(review.getTitle());
                            contentEditText.setText(post.getContent());

                        } else {
                            Log.e("GroupFeed", "단일 게시글 조회 실패: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<GroupReviewResponse>> call, Throwable t) {
                        Log.e("GroupFeed", "단일 게시글 API 실패: " + t.getMessage());
                    }
                });
    }

    private void showGroupInfo() {
        menuLayout.setVisibility(View.VISIBLE);
        groupScrollView.setVisibility(View.GONE);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        menuLayout.startAnimation(slideUp);
    }

    private void hideGroupInfoAndFinish() {
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        menuLayout.startAnimation(slideDown);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation) {
                menuLayout.setVisibility(View.GONE);
                groupScrollView.setVisibility(View.VISIBLE);
            }
            @Override public void onAnimationRepeat(Animation animation) {}
        });
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 50, SWIPE_VELOCITY_THRESHOLD = 50;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(e1==null||e2==null) return false;
            float diffY = e2.getY()-e1.getY();
            if(Math.abs(diffY)>SWIPE_THRESHOLD && Math.abs(velocityY)>SWIPE_VELOCITY_THRESHOLD){
                if(diffY>0){ hideGroupInfoAndFinish(); return true;}
            }
            return false;
        }
    }


    //그룹 탈퇴하기/삭제하기
    private void handleLeaveOrDeleteGroup() {
        if (groupId == -1) return;

        if (groupInfo == null) {
            Toast.makeText(this, "그룹 정보를 불러오는 중입니다. 잠시만 기다려주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("GroupFeedActivity", "groupInfo: " + groupInfo);
        Log.d("GroupFeedActivity", "currentUserId: " + getCurrentUserId());


        GroupController controller = new GroupController(this);

        if (isGroupOwner()) {
            // 그룹장인 경우 -> 그룹 삭제
            controller.deleteGroup(groupId, new GroupController.OnDeleteGroupCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(GroupFeedActivity.this, "그룹 삭제 완료", Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(new Intent(GroupFeedActivity.this, GroupActivity.class));
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(GroupFeedActivity.this, "삭제 실패: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        } else { // 일반 멤버 -> 그룹 탈퇴
            controller.leaveGroup(groupId, new GroupController.OnLeaveGroupCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(GroupFeedActivity.this, "그룹 탈퇴 완료", Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(new Intent(GroupFeedActivity.this, GroupActivity.class));
                }

                @Override
                public void onFailure(String message) {
                    Toast.makeText(GroupFeedActivity.this, "탈퇴 실패: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //그룹장이면 그룹 삭제하기, 일반 멤버이면 그룹 탈퇴하기 표시
    private void updateLeaveOrDeleteButton() {
        MaterialButton leaveButton = menuLayout.findViewById(R.id.btn_leave_group);
        if (leaveButton == null || groupInfo == null) return;

        if (isGroupOwner()) {
            leaveButton.setText("그룹 삭제하기");
            leaveButton.setTextColor(ContextCompat.getColor(this, R.color.red));
        } else {
            leaveButton.setText("그룹 탈퇴하기");
            leaveButton.setTextColor(ContextCompat.getColor(this, R.color.red));
        }

        leaveButton.setEnabled(true); // 버튼 활성화
    }

    private void updateJoinRequestSection() {
        TextView groupJoinTitle = findViewById(R.id.groupJoin);
        RecyclerView groupJoinRecyclerView = findViewById(R.id.groupJoinRecyclerView);

        if (groupInfo == null) {
            groupJoinTitle.setVisibility(View.GONE);
            groupJoinRecyclerView.setVisibility(View.GONE);
            return;
        }

        if (isGroupOwner()) {
            // 그룹장일 경우 보여주기
            groupJoinTitle.setVisibility(View.VISIBLE);
            groupJoinRecyclerView.setVisibility(View.VISIBLE);

            // 어댑터 세팅
            joinRequestAdapter = new GroupJoinRequestAdapter(this, joinRequestList, groupApi, token, groupId);
            groupJoinRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            groupJoinRecyclerView.setAdapter(joinRequestAdapter);

            // 서버에서 가입 요청 리스트 불러오기
            groupApi.getJoinRequests("Bearer " + token, groupId, 0, 20)
                    .enqueue(new Callback<GroupJoinRequestResponse>() {
                        @Override
                        public void onResponse(Call<GroupJoinRequestResponse> call, Response<GroupJoinRequestResponse> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                joinRequestList.clear();
                                joinRequestList.addAll(response.body().getData().getContent());
                                joinRequestAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(GroupFeedActivity.this, "가입 요청 조회 실패", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<GroupJoinRequestResponse> call, Throwable t) {
                            Toast.makeText(GroupFeedActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            // 일반 멤버일 경우 숨기기
            groupJoinTitle.setVisibility(View.GONE);
            groupJoinRecyclerView.setVisibility(View.GONE);
        }
    }


    // 현재 로그인한 유저가 그룹장인지 확인
    private boolean isGroupOwner() {
        if (groupInfo == null) return false;
        int currentUserId = getCurrentUserId();
        return currentUserId == groupInfo.getOwnerId();
    }

    //userId 가져오기
    private int getCurrentUserId() {
        return getSharedPreferences("auth", MODE_PRIVATE)
                .getInt("userId", -1);
    }



    private void fetchGroupInfo(GroupMemberAdapter memberAdapter) {
        GroupController groupController = new GroupController(this);

        groupController.getGroupById(groupId, new Callback<GroupPostResponse>() {
            @Override
            public void onResponse(Call<GroupPostResponse> call, Response<GroupPostResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    groupInfo = response.body().getData();
                    // fragment에 전달
                    int ownerId = groupInfo.getOwnerId();

                    // Fragment에 groupId와 ownerId 전달
                    Bundle args = new Bundle();
                    args.putInt("groupId", groupId);
                    args.putInt("ownerId", ownerId);

                    GroupGoalFragment goalFragment = new GroupGoalFragment();
                    goalFragment.setArguments(args);

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.group_goal_fragment, goalFragment)
                            .commitNow();

                    // ownerId 업데이트
                    memberAdapter.setOwnerId(groupInfo.getOwnerId());
                    memberAdapter.notifyDataSetChanged();

                    Log.d("GroupFeedActivity", "서버에서 받은 그룹 정보: "
                            + "id=" + groupInfo.getId()
                            + ", ownerId=" + groupInfo.getOwnerId()
                            + ", name=" + groupInfo.getName());

                    Log.d("GroupFeedActivity", "현재 로그인 유저 ID: " + getCurrentUserId());
                    updateLeaveOrDeleteButton();
                    updateJoinRequestSection();
                    updateLeaderViews();  // ← 그룹장 UI 보이기/숨기기
                   // menuLayout.findViewById(R.id.btn_leave_group).setEnabled(true);

                    // ★ 여기서 버튼 리스너 설정
                    if (isGroupOwner()) {
                        // 숨겨놨던 버튼 보이게 하기
                        noticeSubmitButton.setVisibility(View.VISIBLE);
                        recommendSubmitButton.setVisibility(View.VISIBLE);

                        // 리스너 설정
                        noticeSubmitButton.setOnClickListener(v ->
                                submitNotice(token, groupId, noticeTitleEditText.getText().toString(),
                                        noticeContentEditText.getText().toString()));
                        recommendSubmitButton.setOnClickListener(v ->
                                submitRecommendBook(token, groupId, recommendTitleEditText.getText().toString(),
                                        recommendContentEditText.getText().toString()));
                    } else {
                        // 일반 멤버는 버튼 숨기기
                        noticeSubmitButton.setVisibility(View.GONE);
                        recommendSubmitButton.setVisibility(View.GONE);
                    }

                } else {
                    Toast.makeText(GroupFeedActivity.this,
                            "그룹 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<GroupPostResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(GroupFeedActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

 // 그룹장 권한 위임
 private void delegateGroupOwner(int newOwnerId, GroupMemberAdapter memberAdapter) {
     groupApi.delegateGroupOwner("Bearer " + token, groupId, newOwnerId)
             .enqueue(new retrofit2.Callback<GroupCommonResponse>() {
                 @Override
                 public void onResponse(retrofit2.Call<GroupCommonResponse> call,
                                        retrofit2.Response<GroupCommonResponse> response) {
                     Log.d("API_DELEGATE_OWNER", "HTTP code: " + response.code());
                     if (response.errorBody() != null) {
                         try {
                             Log.e("API_DELEGATE_OWNER", "Error body: " + response.errorBody().string());
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
                     }

                     // 204 No Content도 성공으로 처리
                     if (response.isSuccessful() || response.code() == 204) {
                         Log.d("API_DELEGATE_OWNER", "위임 성공: " + response.body().toString());
                         memberAdapter.setOwnerId(newOwnerId);
                         Toast.makeText(GroupFeedActivity.this,
                                 "그룹장 위임 완료", Toast.LENGTH_SHORT).show();
                     } else {
                         Toast.makeText(GroupFeedActivity.this,
                                 "서버 오류: " + response.code(), Toast.LENGTH_SHORT).show();
                     }
                 }

                 @Override
                 public void onFailure(retrofit2.Call<GroupCommonResponse> call, Throwable t) {
                     Log.e("API_DELEGATE_OWNER", "onFailure", t);
                     Toast.makeText(GroupFeedActivity.this,
                             "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                 }
             });

 }

    private void updateLeaderViews() {
        boolean isLeader = isGroupOwner();

        // 공지
        noticeTitleEditText.setEnabled(isLeader);   // 입력 가능 여부
        noticeContentEditText.setEnabled(isLeader);
        noticeSubmitButton.setOnClickListener(v -> {
            if (!isGroupOwner()) {
                Toast.makeText(this, "권한이 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
        });



        // 추천 도서
        recommendTitleEditText.setEnabled(isLeader);
        recommendContentEditText.setEnabled(isLeader);
        recommendSubmitButton.setOnClickListener(v -> {
            if (!isGroupOwner()) {
                Toast.makeText(this, "권한이 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
        });
    }



    private void submitNotice(String token, int groupId, String title, String content) {
            Call<ApiResponse<GroupFeedPageResponse>> call =
                    groupApi.getGroupFeed("Bearer " + token, groupId, currentPage, PAGE_SIZE);

            call.enqueue(new Callback<ApiResponse<GroupFeedPageResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<GroupFeedPageResponse>> call,
                                       Response<ApiResponse<GroupFeedPageResponse>> response) {
                    if (response.isSuccessful() && response.body() != null &&
                            response.body().getData() != null) {

                        GroupFeedPageResponse pageResponse = response.body().getData();
                        List<GroupFeedResponse.Post> notices = pageResponse.getNotices();

                        // ✅ 기존 공지 ID 수집
                        List<Integer> noticeIds = new ArrayList<>();
                        if (notices != null && !notices.isEmpty()) {
                            for (GroupFeedResponse.Post notice : notices) {
                                noticeIds.add(notice.getReviewId());
                            }
                        }

                        // ✅ 기존 공지 삭제 후 새 공지 생성
                        deleteNoticesSequentially(token, groupId, noticeIds, () -> createNotice(token, groupId, title, content));
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<GroupFeedPageResponse>> call, Throwable t) {
                    Log.e("FETCH_NOTICE", "공지 조회 실패", t);
                    Toast.makeText(GroupFeedActivity.this, "공지 조회 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

// 공지를 순차적으로 삭제하는 함수
        private void deleteNoticesSequentially(String token, int groupId, List<Integer> noticeIds, Runnable onComplete) {
            if (noticeIds.isEmpty()) {
                onComplete.run();
                return;
            }

            int reviewId = noticeIds.remove(0);
            Log.d("NOTICE_DELETE", "삭제 시도 reviewId=" + reviewId);

            groupApi.deletePost("Bearer " + token, groupId, reviewId)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Log.d("NOTICE_DELETE", "공지 삭제 성공 reviewId=" + reviewId);
                            } else {
                                Log.e("NOTICE_DELETE", "공지 삭제 실패 reviewId=" + reviewId + " code=" + response.code());
                            }
                            deleteNoticesSequentially(token, groupId, noticeIds, onComplete);
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e("NOTICE_DELETE", "공지 삭제 에러 reviewId=" + reviewId, t);
                            // 실패해도 다음 공지 삭제
                            deleteNoticesSequentially(token, groupId, noticeIds, onComplete);
                        }
                    });
        }

        // 실제 공지 생성
        private void createNotice(String token, int groupId, String title, String content) {
            CreateGroupNotice notice = new CreateGroupNotice(title, content, null);
            groupApi.createNotice("Bearer " + token, groupId, notice)
                    .enqueue(new Callback<ApiResponse<GroupReviewResponse>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<GroupReviewResponse>> call, Response<ApiResponse<GroupReviewResponse>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                                Log.d("NOTICE_CREATE", "공지 생성 성공! postId=" + response.body().getData().getId());
                                Toast.makeText(GroupFeedActivity.this, "공지가 등록되었습니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(GroupFeedActivity.this, "공지 생성 실패", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<GroupReviewResponse>> call, Throwable t) {
                            Toast.makeText(GroupFeedActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }


    // 기존 submitNotice 패턴 그대로, 추천 도서용
    private void submitRecommendBook(String token, int groupId, String title, String content) {
        Call<ApiResponse<GroupFeedPageResponse>> call =
                groupApi.getGroupFeed("Bearer " + token, groupId, currentPage, PAGE_SIZE);

        call.enqueue(new Callback<ApiResponse<GroupFeedPageResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<GroupFeedPageResponse>> call,
                                   Response<ApiResponse<GroupFeedPageResponse>> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().getData() != null) {

                    GroupFeedPageResponse pageResponse = response.body().getData();
                    List<GroupFeedResponse.Post> recommendedBooks = pageResponse.getRecommendedBooks();

                    // 기존 추천 도서 ID 수집
                    List<Integer> recommendIds = new ArrayList<>();
                    if (recommendedBooks != null && !recommendedBooks.isEmpty()) {
                        for (GroupFeedResponse.Post book : recommendedBooks) {
                            recommendIds.add(book.getReviewId());
                        }
                    }

                    // 기존 추천 도서 삭제 후 새 추천 도서 생성
                    deleteRecommendSequentially(token, groupId, recommendIds, () ->
                            createRecommendBook(token, groupId, title, content));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<GroupFeedPageResponse>> call, Throwable t) {
                Log.e("FETCH_RECOMMEND", "추천 도서 조회 실패", t);
                Toast.makeText(GroupFeedActivity.this, "추천 도서 조회 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 추천 도서를 순차적으로 삭제
    private void deleteRecommendSequentially(String token, int groupId, List<Integer> recommendIds, Runnable onComplete) {
        if (recommendIds.isEmpty()) {
            onComplete.run();
            return;
        }

        int reviewId = recommendIds.remove(0);
        Log.d("RECOMMEND_DELETE", "삭제 시도 reviewId=" + reviewId);

        groupApi.deletePost("Bearer " + token, groupId, reviewId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d("RECOMMEND_DELETE", "추천 도서 삭제 성공 reviewId=" + reviewId);
                        } else {
                            Log.e("RECOMMEND_DELETE", "추천 도서 삭제 실패 reviewId=" + reviewId + " code=" + response.code());
                        }
                        deleteRecommendSequentially(token, groupId, recommendIds, onComplete);
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("RECOMMEND_DELETE", "추천 도서 삭제 에러 reviewId=" + reviewId, t);
                        deleteRecommendSequentially(token, groupId, recommendIds, onComplete);
                    }
                });
    }

    // 실제 추천 도서 생성
    private void createRecommendBook(String token, int groupId, String title, String content) {
        CreateGroupRecommend recommend = new CreateGroupRecommend(title, content, null);
        groupApi.createRecommend("Bearer " + token, groupId, recommend)
                .enqueue(new Callback<ApiResponse<GroupReviewResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<GroupReviewResponse>> call,
                                           Response<ApiResponse<GroupReviewResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            Log.d("RECOMMEND_CREATE", "추천 도서 생성 성공! postId=" + response.body().getData().getId());
                            Toast.makeText(GroupFeedActivity.this, "추천 도서가 등록되었습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GroupFeedActivity.this, "추천 도서 생성 실패", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<GroupReviewResponse>> call, Throwable t) {
                        Toast.makeText(GroupFeedActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
/*
    private final ActivityResultLauncher<Intent> feedDetailLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Intent data = result.getData();

                            // 삭제 처리
                            int deletedPostId = data.getIntExtra("deletedPostId", -1);
                            if (deletedPostId != -1) removePostFromList(deletedPostId);

                            // 수정 처리
                            GroupFeedResponse.Post updatedPost =
                                    (GroupFeedResponse.Post) data.getSerializableExtra("updatedPost");
                            if (updatedPost != null) {
                                int postId = data.getIntExtra("postId", -1);
                                updatePostInList(postId, updatedPost);
                            }

                            // 새 글 작성 처리
                            boolean refreshPost = data.getBooleanExtra("refreshPost", false);
                            if (refreshPost) {
                                fetchGroupInfo(memberAdapter); // 전체 피드 새로고침
                            }
                        }
                    }
            );

    // 피드 삭제 갱신
    private void removePostFromList(int postId) {
        for (int i = 0; i < groupFeedList.size(); i++) {
            if (groupFeedList.get(i).getPostId() == postId) { // FeedResponse에 reviewId가 있다고 가정
                groupFeedList.remove(i);
                groupFeedAdapter.notifyItemRemoved(i);
                break;
            }
        }
    }
    private void updatePostInList(int postId, GroupFeedResponse.Post updatedPost) {
        for (int i = 0; i < groupFeedList.size(); i++) {
            GroupFeedItem item = groupFeedList.get(i);
            if (item.getPostId() == postId) {
                // FeedItem 필드 업데이트
                item.setTitle(updatedPost.getTitle());
                item.setContent(updatedPost.getContent());

                List<String> images = updatedPost.getReviewImageUrls();
                if (images != null && !images.isEmpty()) {
                    item.setImageUrl(images.get(0)); // 첫 번째 이미지 사용
                } else {
                    item.setImageUrl(null);
                }

                groupFeedAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    /*
    //피드 수정 갱신
    private void updateFeedInList(GroupFeedResponse updatedFeed) {
        for (int i = 0; i < groupFeedList.size(); i++) {
            GroupFeedItem item = groupFeedList.get(i);
            if (item.getPostId() == updatedFeed.getPId()) { // postId와 reviewId 비교
                // FeedItem 필드 업데이트
                item.setTitle(updatedFeed.getTitle());
                item.setContent(updatedFeed.getContent());

                List<String> images = updatedFeed.getReviewImageUrls();
                if (images != null && !images.isEmpty()) {
                    item.setImageUrl(images.get(0)); // 첫 번째 이미지 사용
                } else {
                    item.setImageUrl(null); // 이미지 없으면 null
                }

                groupFeedAdapter.notifyItemChanged(i);
                break;
            }
        }
    }
*/

    // 그룹 피드 갱신 런처
    private final ActivityResultLauncher<Intent> feedDetailLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Log.d("GroupFeedRefresh", "feedDetailLauncher 호출, resultCode=" + result.getResultCode());

                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Intent data = result.getData();

                            // --- 삭제 처리 ---
                            int deletedPostId = data.getIntExtra("deletedPostId", -1);
                            if (deletedPostId != -1) {
                                Log.d("GroupFeedRefresh", "삭제된 postId=" + deletedPostId);
                                removePostFromList(deletedPostId);
                                return; // 삭제는 즉시 반환
                            }

                            // --- 수정 처리 ---
                            GroupFeedResponse.Post updatedPost =
                                    (GroupFeedResponse.Post) data.getSerializableExtra("updatedPost");
                            if (updatedPost != null) {
                                Log.d("GroupFeedRefresh", "수정된 post 수신, postId=" + updatedPost.getReviewId());
                                updatePostInList(updatedPost);
                                return;
                            }

                            // --- 새 글 작성 or 전체 새로고침 ---
                            boolean refreshPost = data.getBooleanExtra("refreshPost", false);
                            if (refreshPost) {
                                Log.d("GroupFeedRefresh", "전체 피드 새로고침 요청");
                                loadGroupFeeds(groupId, true); // 전체 새로고침
                            }
                        }
                    }
            );

    // 그룹 피드 삭제 갱신
    private void removePostFromList(int postId) {
        for (int i = 0; i < groupFeedList.size(); i++) {
            if (groupFeedList.get(i).getPostId() == postId) {
                Log.d("GroupFeedRefresh", "삭제 대상 발견, position=" + i);
                groupFeedList.remove(i);
                groupFeedAdapter.notifyItemRemoved(i);
                break;
            }
        }
    }

    // 그룹 피드 수정 갱신
    private void updatePostInList(GroupFeedResponse.Post updatedPost) {
        Log.d("GroupFeedRefresh", "updatePostInList 호출, postId=" + updatedPost.getReviewId());
        for (int i = 0; i < groupFeedList.size(); i++) {
            GroupFeedItem item = groupFeedList.get(i);
            if (item.getPostId() == updatedPost.getReviewId()) {
                Log.d("GroupFeedRefresh", "수정 대상 게시글 발견, position=" + i);

                // 필드 업데이트
                item.setTitle(updatedPost.getTitle());
                item.setContent(updatedPost.getContent());

                // ✅ imageUrls 리스트 업데이트
                List<String> images = updatedPost.getReviewImageUrls();
                if (images != null && !images.isEmpty()) {
                    item.setImageUrls(images); // 전체 리스트 저장
                } else {
                    item.setImageUrls(new ArrayList<>()); // 비어있는 리스트로 초기화
                }


                // 필요한 필드가 있다면 여기에 추가 (예: 작성자명, 태그 등)
                groupFeedAdapter.notifyItemChanged(i);
                Log.d("GroupFeedRefresh", "groupFeedAdapter.notifyItemChanged 완료, position=" + i);
                break;
            }
        }
    }

    @Override
    protected int getCurrentNavItemId() {
        return R.id.Group;
    }
}
