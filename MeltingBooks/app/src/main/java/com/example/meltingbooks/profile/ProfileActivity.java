package com.example.meltingbooks.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;
import com.example.meltingbooks.base.BaseActivity;
import com.example.meltingbooks.calendar.utils.BookListHelper;
import com.example.meltingbooks.feed.FeedAdapter;
import com.example.meltingbooks.feed.FeedItem;
import com.example.meltingbooks.feed.FullscreenImageFragment;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.profile.BadgeCatalogResponse;
import com.example.meltingbooks.network.profile.FollowApi;
import com.example.meltingbooks.network.profile.FollowUser;
import com.example.meltingbooks.network.profile.RepresentativeBadgeRequest;
import com.example.meltingbooks.network.profile.UserResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends BaseActivity {

    private TextView profileName, profileLevel, profileBio;
    private TextView followerCount, followingCount, reviewCount;
    private TextView profileTagId;
    private ImageView profileImage;

    // 🔥 추가: 피드 관련
    private RecyclerView feedRecyclerView;
    private FeedAdapter feedAdapter;
    private List<FeedItem> feedList = new ArrayList<>();

    //피드 갱신용
    private ActivityResultLauncher<Intent> feedDetailLauncher;

    private TextView actionButton; // goTo_Profile_setting
    private boolean isFollowing = false; // 현재 팔로우 상태
    private int viewedUserId; // 지금 보고 있는 프로필 주인
    private int myUserId; // 내 ID

    private List<UserResponse.Badge> userBadges = new ArrayList<>(); // 배지 추가

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // ActivityResultLauncher 초기화
        feedDetailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // FeedDetailActivity에서 삭제/수정 후 돌아올 때 처리
                        loadUserProfile(); // 프로필 화면의 피드 갱신
                    }
                }
        );

        setupBottomNavigation();

        // SharedPreferences에서 내 userId
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        myUserId = prefs.getInt("userId", -1);

        // Intent에서 다른 사람 userId 받기
        viewedUserId = getIntent().getIntExtra("userId", myUserId);

        actionButton = findViewById(R.id.goTo_Profile_setting);

        if (viewedUserId == myUserId) {
            // 내 프로필일 때 → 프로필 수정 이동
            actionButton.setText("Edit Profile");
            actionButton.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, SettingProfile.class);
                startActivity(intent);
            });
        } else {
            // 다른 사람 프로필 일 때 → 팔로우 버튼
            checkFollowStatus();
            actionButton.setOnClickListener(v -> toggleFollow());
        }

        // 앱 설정 화면 이동
        ImageButton goToAppSet = findViewById(R.id.goTo_App_setting);
        goToAppSet.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SettingApp.class);
            startActivity(intent);
        });

        // UI 연결
        profileName = findViewById(R.id.profile_name);
        profileLevel = findViewById(R.id.profile_level);
        profileTagId = findViewById(R.id.profile_tagid);
        profileBio = findViewById(R.id.profile_bio);
        profileImage = findViewById(R.id.profile_image);

        followerCount = findViewById(R.id.follower_count);
        followingCount = findViewById(R.id.following_count);
        reviewCount = findViewById(R.id.review_count);

        // 🔥 리사이클러뷰 설정
        feedRecyclerView = findViewById(R.id.feedRecyclerView);
        feedRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //피드 갱신용(수정)
        feedAdapter = new FeedAdapter(this, feedList, feedDetailLauncher);
        feedRecyclerView.setAdapter(feedAdapter);

        feedRecyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                // 좋아요/댓글/공유 관련 뷰 숨기기
                view.findViewById(R.id.comment_button).setVisibility(View.GONE);
                view.findViewById(R.id.comment_count).setVisibility(View.INVISIBLE);
                view.findViewById(R.id.like_Button).setVisibility(View.INVISIBLE);
                view.findViewById(R.id.like_count).setVisibility(View.INVISIBLE);
                view.findViewById(R.id.share_Button).setVisibility(View.INVISIBLE);
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                // 필요 없으면 비워둬도 됩니다
            }
        });

        // 프로필 불러오기
        loadUserProfile();

        // 팔로워 클릭 → FollowListActivity (followers)
        followerCount.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FollowListActivity.class);
            Log.d("ProfileActivity", "팔로워 클릭, viewedUserId: " + viewedUserId);
            intent.putExtra("userId", viewedUserId);  // viewedUserId 넘기기
            intent.putExtra("type", "followers");
            startActivity(intent);
        });

        // 팔로잉 클릭 → FollowListActivity (following)
        followingCount.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FollowListActivity.class);
            Log.d("ProfileActivity", "팔로잉 클릭, viewedUserId: " + viewedUserId);
            intent.putExtra("userId", viewedUserId);  // viewedUserId 넘기기
            intent.putExtra("type", "following");
            startActivity(intent);
        });

        //배지 함수 호출
        //loadUserBadges();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile(); // 다시 진입할 때마다 최신 데이터
    }

    private void loadUserProfile() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
        int userId = prefs.getInt("userId", -1);

        if (token == null || userId == -1) {
            Log.e("Profile", "토큰 또는 사용자 ID가 없습니다.");
            return;
        }

        ApiService apiService = ApiClient.getClient(token).create(ApiService.class);
        Call<ApiResponse<UserResponse>> call = apiService.getUserProfile("Bearer " + token, viewedUserId);

        call.enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call,
                                   Response<ApiResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    UserResponse user = response.body().getData();

                    // 유저가 가진 배지 목록 저장
                    if (user.getBadges() != null) {
                        userBadges = user.getBadges(); // 다이얼로그에서 획득일 비교용

                        sortBadges(userBadges); // ✅ 배지 고정 순서 정렬

                        showBadges(userBadges); // ✅ 여기서 바로 배지 렌더링
                    }

                    // UI 업데이트
                    profileName.setText(user.getNickname());
                    profileLevel.setText("Lv." + user.getLevel());
                    profileBio.setText(user.getBio() != null ? user.getBio() : "소개가 없습니다.");
                    profileTagId.setText(
                            (user.getTagId() != null && !"null".equals(user.getTagId()))
                                    ? "@" + user.getTagId()
                                    : "@"  // ✅ null 또는 "null"이면 @만 표시
                    );
                    followerCount.setText(String.valueOf(user.getFollowerCount()));
                    followingCount.setText(String.valueOf(user.getFollowingCount()));
                    reviewCount.setText(String.valueOf(user.getReviewCount()));

                    Glide.with(ProfileActivity.this)
                            .load(user.getProfileImageUrl())
                            .placeholder(R.drawable.sample_profile)
                            .circleCrop()
                            .into(profileImage);

                    // 프로필 이미지 크게 보기
                    profileImage.setOnClickListener(v -> {
                        String img = user.getProfileImageUrl();
                        if (img != null && !img.isEmpty()) {
                            FullscreenImageFragment fragment =
                                    FullscreenImageFragment.newInstance(img);

                            fragment.show(
                                    getSupportFragmentManager(),
                                    "FullscreenProfileImage"
                            );
                        }
                    });

                    // recentBooks 세팅
                    List<BookListHelper.BookItem> books = new ArrayList<>();
                    if (user.getRecentBooks() != null) {
                        for (UserResponse.Book book : user.getRecentBooks()) {
                            books.add(new BookListHelper.BookItem(book.getCover(), false));
                        }
                    }

                    // 레이아웃에 실제로 반영
                    ViewGroup bookListContainer = findViewById(R.id.book_list_container);
                    BookListHelper.setupBooks(ProfileActivity.this, bookListContainer, books, false);

                    //리뷰 불러오기

                    // 최신순 리뷰 리스트
                    List<UserResponse.Review> reviews = user.getRecentReviews();
                    feedList.clear();

                    for (UserResponse.Review review : reviews) {
                        FeedItem feedItem = new FeedItem(
                                user.getNickname(),
                                review.getContent(),
                                review.getFormattedCreatedAt(),
                                review.getImageUrl(),
                                user.getProfileImageUrl(),
                                review.getBookId() // 책 Id
                        );

                        // ✅ 리뷰ID를 postId로 세팅
                        feedItem.setPostId(review.getReviewId());
                        feedItem.setPostType("feed");

                        feedList.add(feedItem);
                    }
                    feedAdapter.notifyDataSetChanged();

                    // ✅ 대표 배지 표시
                    ImageView repBadgeView = findViewById(R.id.representative_badge);

                    UserResponse.Badge repBadge = null;
                    for (UserResponse.Badge b : userBadges) {
                        if (b.isRepresentative()) {
                            repBadge = b;
                            break;
                        }
                    }

                    if (repBadge != null) {
                        repBadgeView.setVisibility(View.VISIBLE);
                        Glide.with(ProfileActivity.this)
                                .load(repBadge.getImageUrl())
                                .into(repBadgeView);
                    } else {
                        repBadgeView.setVisibility(View.GONE);
                    }

                } else {
                    Log.e("Profile", "프로필 응답이 비정상: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                Log.e("Profile", "API 호출 실패: " + t.getMessage());
            }
        });

    }

    private void checkFollowStatus() {
        String token = getSharedPreferences("auth", MODE_PRIVATE).getString("jwt", null);
        FollowApi followApi = ApiClient.getClient(token).create(FollowApi.class);

        // 내 팔로잉 목록 조회해서 상대방 있는지 확인
        followApi.getFollowing("Bearer " + token, myUserId).enqueue(new Callback<ApiResponse<List<FollowUser>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<FollowUser>>> call, Response<ApiResponse<List<FollowUser>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FollowUser> following = response.body().getData();
                    isFollowing = false;
                    for (FollowUser f : following) {
                        if (f.getId() == viewedUserId) {
                            isFollowing = true;
                            break;
                        }
                    }
                    updateFollowButton();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<FollowUser>>> call, Throwable t) {
                Log.e("Profile", "팔로잉 조회 실패: " + t.getMessage());
            }
        });
    }

    private void toggleFollow() {
        String token = getSharedPreferences("auth", MODE_PRIVATE).getString("jwt", null);
        FollowApi followApi = ApiClient.getClient(token).create(FollowApi.class);

        if (isFollowing) {
            // 언팔로우
            followApi.unfollowUser("Bearer " + token, myUserId, viewedUserId)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.code() == 204) {
                                isFollowing = false;
                                runOnUiThread(() -> updateFollowButton());
                            }
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e("Profile", "언팔로우 실패: " + t.getMessage());
                        }
                    });
        } else {
            // 팔로우
            followApi.followUser("Bearer " + token, myUserId, viewedUserId)
                    .enqueue(new Callback<ApiResponse<String>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().isSuccess()) {
                                isFollowing = true;
                                runOnUiThread(() -> updateFollowButton());
                            }
                        }
                        @Override
                        public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                            Log.e("Profile", "팔로우 실패: " + t.getMessage());
                        }
                    });
        }
    }


    private void updateFollowButton() {
        if (isFollowing) {
            actionButton.setText("Unfollow");
            actionButton.setBackgroundResource(R.drawable.bg_unfollow_button);
            actionButton.setTextColor(getColor(android.R.color.white));
        } else {
            actionButton.setText("Follow");
            actionButton.setBackgroundResource(R.drawable.bg_follow_button);
            actionButton.setTextColor(getColor(android.R.color.white));
        }
    }

    private void showBadges(List<UserResponse.Badge> badges) {
        GridLayout badgeContainer = findViewById(R.id.badge_container);
        badgeContainer.removeAllViews();

        int imageSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());

        for (UserResponse.Badge badge : badges) {
            ImageView badgeView = new ImageView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = imageSize;
            params.height = imageSize;
            params.setMargins(40, 25, 40, 25);
            badgeView.setLayoutParams(params);

            badgeView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            badgeView.setAdjustViewBounds(true);  // 이미지뷰 비율 유지

            Glide.with(this)
                    .load(badge.getImageUrl())
                    .into(badgeView);

            badgeView.setOnClickListener(v -> showBadgeDialog(badge));

            badgeContainer.addView(badgeView);
        }
    }

    private void showBadgeDialog(UserResponse.Badge badge) {
        if (badge == null) {
            Log.w("BadgeDialog", "badge 데이터가 null입니다.");
            return;
        }

        // 대표 여부 체크 (내 프로필일 때만 대표 설정 가능)
        boolean isMine = (viewedUserId == myUserId);

        // null-safe 처리
        String badgeType = badge.getBadgeType() != null ? badge.getBadgeType() : "UNKNOWN";
        String tierText = badge.getTier() != null ? badge.getTier() : "미획득";

        String title = getBadgeTitle(badgeType);
        String desc = getBadgeDescription(badgeType, badge.getTier());

        // 유저 획득 날짜 조회
        String acquiredDate = badge.getCreatedAt();

        // 날짜 포맷 변환
        if (acquiredDate != null) {
            try {
                java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(
                        acquiredDate, java.time.format.DateTimeFormatter.ISO_DATE_TIME
                );
                acquiredDate = dateTime.format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                );
            } catch (Exception e) {
                Log.w("BadgeDialog", "날짜 포맷 변환 실패");
            }
        }

        // 메시지 구성
        StringBuilder message = new StringBuilder(desc != null ? desc : "배지 설명이 없습니다.");

        if (acquiredDate != null) {
            message.append("\n\n🏅 획득일: ").append(acquiredDate);
        } else {
            message.append("\n\n아직 획득하지 않았습니다.");
        }

        // ✅ AlertDialog.Builder 를 변수로 선언해야 한다!!
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle(title + " (" + tierText + ")")
                .setMessage(message.toString())
                .setPositiveButton("확인", null);

        // ✅ 대표 배지 설정 버튼 조건
        if (isMine && badge.getId() != null) {

            if (!badge.isRepresentative()) {
                // ✅ 대표 배지 설정 버튼 추가
                dialog.setNegativeButton("대표 배지로 설정", (d, w) -> {
                    setRepresentativeBadge(badge.getId());
                });
            } else {
                // ✅ 이미 대표 배지일 때
                dialog.setNegativeButton("이미 대표 배지입니다", null);
            }
        }

        dialog.show();
    }

    //배지별 이름과 설명 매핑
    private String getBadgeTitle(String type) {
        switch (type) {
            case "FULL_READ": return "완독 배지";
            case "GOAL_MASTER": return "목표 마스터 배지";
            case "REVIEW_MASTER": return "감상문 마스터 배지";
            case "MARATHONER": return "마라토너 배지";
            case "GENRE_MASTER": return "장르 마스터 배지";
            case "REVIEW_SHARE_MASTER": return "감상문 공유 마스터 배지";
            default: return "알 수 없는 배지";
        }
    }

    private String getBadgeDescription(String type, String tier) {
        switch (type) {
            case "FULL_READ":
                return "브론즈: 첫 번째 책 완독\n실버: 5권 완독\n골드: 10권 완독\n플래티넘: 3개월 연속 완독";
            case "GOAL_MASTER":
                return "브론즈: 월간 목표 달성\n실버: 2번 달성\n골드: 3개월 연속 달성\n플래티넘: 그룹 공동 목표 달성";
            case "REVIEW_MASTER":
                return "브론즈: 감상문 3개 작성\n실버: 10개 작성\n골드: 3개월 연속 매달 5개 작성\n플래티넘: 그룹 내 10개 작성";
            case "MARATHONER":
                return "브론즈: 연간 목표 절반 달성\n실버: 연간 목표 달성\n골드: 2년 연속 달성\n플래티넘: 그룹 목표 5회 완료";
            case "GENRE_MASTER":
                return "브론즈: 특정 장르 3권 완독\n실버: 5권\n골드: 10권\n플래티넘: 3장르 5권 이상 완독";
            case "REVIEW_SHARE_MASTER":
                return "브론즈: 댓글 10개 작성\n실버: 20개\n골드: 3개월 연속 매달 5개\n플래티넘: 좋아요 5개 이상 포함";
            default:
                return "배지 정보 없음";
        }
    }
    private void sortBadges(List<UserResponse.Badge> badges) {

        // 배지 순서 정의
        List<String> badgeOrder = List.of(
                "FULL_READ",
                "GOAL_MASTER",
                "REVIEW_MASTER",
                "MARATHONER",
                "GENRE_MASTER",
                "REVIEW_SHARE_MASTER"
        );

        badges.sort((b1, b2) -> {
            int idx1 = badgeOrder.indexOf(b1.getBadgeType());
            int idx2 = badgeOrder.indexOf(b2.getBadgeType());
            return Integer.compare(idx1, idx2);
        });
    }

    private void setRepresentativeBadge(int badgeId) {

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", null);

        ApiService api = ApiClient.getClient(token).create(ApiService.class);

        RepresentativeBadgeRequest request = new RepresentativeBadgeRequest(badgeId);

        api.setRepresentativeBadge("Bearer " + token, myUserId, request)
                .enqueue(new Callback<ApiResponse<UserResponse.Badge>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserResponse.Badge>> call,
                                           Response<ApiResponse<UserResponse.Badge>> response) {

                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {

                            // ✅ 대표 배지 UI 업데이트
                            for (UserResponse.Badge b : userBadges) {
                                b.setRepresentative(b.getId() != null && b.getId() == badgeId);
                            }

                            showBadges(userBadges);  // UI 새로 그림
                            showToast("대표 배지가 설정되었습니다!");
                            loadUserProfile();  // 대표 배지 UI 갱신
                        } else {
                            showToast("대표 배지 설정 실패");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserResponse.Badge>> call, Throwable t) {
                        Log.e("Badge", "대표 배지 설정 실패: " + t.getMessage());
                        showToast("네트워크 오류");
                    }
                });
    }

    private void showToast(String msg) {
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override
    protected int getCurrentNavItemId() {
        return R.id.Profile;
    }
}