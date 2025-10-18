package com.example.meltingbooks.feed;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.meltingbooks.R;
import com.example.meltingbooks.base.BaseActivity;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.feed.FeedPageResponse;
import com.example.meltingbooks.network.feed.FeedResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedActivity extends BaseActivity {
    private RecyclerView feedRecyclerView;
    private FeedAdapter feedAdapter;
    private List<FeedItem> feedList = new ArrayList<>(); //Null 방지 초기화



    //⭐새로 고침 및 무한 스크롤 관련 변수
    private SwipeRefreshLayout swipeRefreshLayout; //⭐
    private int currentPage = 0; //⭐ 페이징 현재 페이지
    private final int PAGE_SIZE = 10; // ⭐한 페이지에 불러올 항목 수
    private boolean isLoading = false; //⭐
    private boolean isLastPage = false; //⭐

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        setupBottomNavigation();



        // 글 작성 화면으로 이동
        ImageButton goToUpload = findViewById(R.id.goToUpload);
        goToUpload.setOnClickListener(v -> {
            Intent intent = new Intent(FeedActivity.this, FeedWriteActivity.class);
            startActivity(intent);
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();

            // 상태바 디자인 설정
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.TRANSPARENT);

            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }

            //리사이클러뷰 설정
            feedRecyclerView = findViewById(R.id.feedRecyclerView);
            feedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            // 피드 갱신용 수정
            feedAdapter = new FeedAdapter(this, feedList, feedDetailLauncher);
            feedRecyclerView.setAdapter(feedAdapter);


            //⭐ 무한 스크롤 리스너 추가
            feedRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                                loadFeeds(false); // 다음 페이지 로드
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
                loadFeeds(true); // true: 새로고침
            });

            //서버에서 피드 불러오기
            loadFeeds(false);
        }
    }

    // 피드 갱신
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("FeedRefresh", "onNewIntent 호출됨"); // ✅ 이 로그 추가

        if (intent != null) {
            FeedResponse updatedFeed = (FeedResponse) intent.getSerializableExtra("updatedFeed");
            if (updatedFeed != null) {
                Log.d("FeedRefresh", "updatedFeed 존재: " + updatedFeed.getReviewId()); // ✅ 확인용 로그
                updateFeedInList(updatedFeed);
                return;
            }

            boolean refresh = intent.getBooleanExtra("refreshFeed", false);
            Log.d("FeedRefresh", "refreshFeed: " + refresh); // ✅ 확인용 로그
            if (refresh) {
                currentPage = 0;
                feedList.clear();
                loadFeeds(false);
            }
        }
    }




    //서버에서 피드 목록 불러옴
    private void loadFeeds(boolean isRefresh) {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
        int userId = prefs.getInt("userId", -1);

        if (token == null || userId == -1) {
            Log.e("Feed", "토큰 또는 사용자 ID가 없습니다.");
            return;
        }

        ApiService apiService = ApiClient.getClient(token).create(ApiService.class);

        isLoading = true; // ⭐ 로딩 시작

        // ✅ FeedPageResponse로 수정
        Call<ApiResponse<FeedPageResponse>> call =
                apiService.getUserFeeds("Bearer " + token, userId,  currentPage, PAGE_SIZE);

        call.enqueue(new Callback<ApiResponse<FeedPageResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<FeedPageResponse>> call,
                                   Response<ApiResponse<FeedPageResponse>> response) {
                isLoading = false; // ⭐ 로딩 끝
                swipeRefreshLayout.setRefreshing(false); //⭐ 새로고침 종료

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    FeedPageResponse pageResponse = response.body().getData();
                    List<FeedResponse> feeds = pageResponse.getContent();

                    //feedList.clear();⭐ 삭제 필요

                    //⭐ 새로고침이면 기존 리스트 초기화
                    if (isRefresh) {
                        currentPage = 0;
                        feedList.clear(); // 새로고침이면 기존 리스트 초기화
                    }

                    for (FeedResponse feed : feeds) {
                        String firstImage = (feed.getReviewImageUrls() != null && !feed.getReviewImageUrls().isEmpty())
                                ? feed.getReviewImageUrls().get(0)
                                : null;

                        FeedItem feedItem = new FeedItem(
                                //feed.getUsername(),
                                feed.getNickname(),
                                feed.getContent(),
                                feed.getFormattedCreatedAt(),
                                firstImage,
                                feed.getUserProfileImage(),
                                feed.getBookId(),
                                feed.getCommentCount(),
                                feed.getLikeCount(),
                                feed.getTagId(),
                                feed.getHashtags(),
                                feed.getShareUrl(), //⭐추가
                                feed.getUserId() //⭐추가
                        );

                        // ✅ 리뷰ID를 postId로 세팅
                        feedItem.setPostId(feed.getReviewId());
                        feedItem.setPostType("feed");

                        feedList.add(feedItem);
                    }

                    feedAdapter.notifyDataSetChanged();
                    isLastPage = pageResponse.isLast(); // ⭐ 마지막 페이지 여부 업데이트

                    // ✅ 페이징 정보도 로그 찍기
                    Log.d("Feed", "불러온 리뷰 개수: " + feeds.size());
                    Log.d("Feed", "전체 페이지 수: " + pageResponse.getTotalPages()
                            + ", 마지막 페이지 여부: " + pageResponse.isLast());

                } else {
                    Log.e("Feed", "Feed 응답이 비정상: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<FeedPageResponse>> call, Throwable t) {
                isLoading = false; // ⭐ 실패해도 로딩 끝
                Log.e("Feed", "Feed API 실패: " + t.getMessage());
            }
        });
    }

    @Override
    protected int getCurrentNavItemId() {
        return R.id.Feed;
    }


    // 피드 갱신 런처
    private final ActivityResultLauncher<Intent> feedDetailLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Log.d("FeedRefresh", "feedDetailLauncher 호출, resultCode=" + result.getResultCode());
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Intent data = result.getData();

                            int deletedPostId = data.getIntExtra("deletedPostId", -1);
                            if (deletedPostId != -1) {
                                removeFeedFromList(deletedPostId);
                                return; // 삭제면 바로 반환
                            }

                            FeedResponse updatedFeed = (FeedResponse) data.getSerializableExtra("updatedFeed");
                            if (updatedFeed != null) {
                                Log.d("FeedRefresh", "수정된 feed 수신, reviewId=" + updatedFeed.getReviewId());
                                updateFeedInList(updatedFeed);
                                return;
                            }

                            boolean refresh = data.getBooleanExtra("refreshFeed", false);
                            if (refresh) {
                                Log.d("FeedRefresh", "전체 새로고침 요청");
                                loadFeeds(true);
                            }
                        }
                    }
            );

    // 피드 삭제 갱신
    private void removeFeedFromList(int postId) {
        for (int i = 0; i < feedList.size(); i++) {
            if (feedList.get(i).getPostId() == postId) { // FeedResponse에 reviewId가 있다고 가정
                feedList.remove(i);
                feedAdapter.notifyItemRemoved(i);
                break;
            }
        }
    }


    //피드 수정 갱신
    private void updateFeedInList(FeedResponse updatedFeed) {
        Log.d("FeedRefresh", "updateFeedInList 호출, reviewId=" + updatedFeed.getReviewId());
        for (int i = 0; i < feedList.size(); i++) {
            FeedItem item = feedList.get(i);
            if (item.getPostId() == updatedFeed.getReviewId()) { // postId와 reviewId 비교
                Log.d("FeedRefresh", "수정 대상 피드 발견, position=" + i);
                // FeedItem 필드 업데이트
                item.setReviewContent(updatedFeed.getContent());
                
                List<String> images = updatedFeed.getReviewImageUrls();
                if (images != null && !images.isEmpty()) {
                    item.setImageUrl(images.get(0)); // 첫 번째 이미지 사용
                } else {
                    item.setImageUrl(null); // 이미지 없으면 null
                }
                // 필요한 필드가 있으면 추가(별점은 제외)
                // bookId, hashtags 업데이트
                item.setBookId(updatedFeed.getBookId());   // Integer, null 허용
                item.setHashtags(updatedFeed.getHashtags()); // List<String>, null 가능
                Log.d("FeedRefresh", "feedAdapter.notifyItemChanged 호출 완료, position=" + i);
                feedAdapter.notifyItemChanged(i);
                break;
            }
        }
    }
}
