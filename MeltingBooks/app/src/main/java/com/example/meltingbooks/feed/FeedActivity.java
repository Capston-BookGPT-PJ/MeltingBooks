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
import com.example.meltingbooks.network.recommend.RecommendBookApi;
import com.example.meltingbooks.network.recommend.RecommendBookPageResponse;
import com.example.meltingbooks.network.recommend.RecommendBookResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedActivity extends BaseActivity {
    private RecyclerView feedRecyclerView;
    private FeedAdapter feedAdapter;
    private List<FeedItem> feedList = new ArrayList<>(); //Null 방지 초기화


    private List<String> cachedRecommendedCovers = new ArrayList<>();

    //⭐새로 고침 및 무한 스크롤 관련 변수
    private SwipeRefreshLayout swipeRefreshLayout; //⭐
    private int currentPage = 0; //⭐ 페이징 현재 페이지
    private final int PAGE_SIZE = 10; // ⭐한 페이지에 불러올 항목 수
    private boolean isLoading = false; //⭐
    private boolean isLastPage = false; //⭐

    private int nextRecommendPage = 0; // ✅ 추천 전용 페이지
    private long nextRecoStableId = -1; // ✅ 추천아이템 고정 ID 생성 (음수로)

    private Integer pendingPostId = null; // 알림 클릭으로 이동할 postId 저장

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
                                //currentPage++;
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

            int targetPostId = getIntent().getIntExtra("postId", -1);
            if (targetPostId != -1) {
                pendingPostId = targetPostId; // 나중에 loadFeeds 완료 후 처리
            }

            // ✅ 첫 페이지부터 로드
            loadFeeds(false);
        }

    }

    private void scrollToTargetFeed(int targetPostId) {
        for (int i = 0; i < feedList.size(); i++) {
            if (feedList.get(i).getPostId() == targetPostId) {
                feedRecyclerView.scrollToPosition(i);

                // 자동으로 상세 열기
                FeedItem targetItem = feedList.get(i);
                Intent intent = new Intent(this, FeedDetailActivity.class);
                intent.putExtra("feedItem", targetItem);
                feedDetailLauncher.launch(intent);

                return;
            }
        }

        if (!isLastPage && !isLoading) {
            currentPage++;
            loadFeeds(false);
            feedRecyclerView.postDelayed(() -> scrollToTargetFeed(targetPostId), 300);
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
        if (token == null || userId == -1) return;
        if (isLoading) return;
        isLoading = true;

        if (isRefresh) {
            currentPage = 0;
            nextRecommendPage = 0;         // ✅ 추천 페이지도 리셋
            feedList.clear();
            isLastPage = false;
        }

        final int pageToLoad = currentPage;
        ApiService api = ApiClient.getClient(token).create(ApiService.class);

        api.getUserFeeds("Bearer " + token, userId, pageToLoad, PAGE_SIZE)
                .enqueue(new Callback<ApiResponse<FeedPageResponse>>() {
                    @Override public void onResponse(Call<ApiResponse<FeedPageResponse>> call,
                                                     Response<ApiResponse<FeedPageResponse>> res) {
                        if (!res.isSuccessful() || res.body()==null || res.body().getData()==null) {
                            finishLoading(false);
                            return;
                        }

                        FeedPageResponse pg = res.body().getData();
                        List<FeedResponse> feeds = pg.getContent();

                        // 1) 이번 페이지 피드들을 임시로 변환
                        List<FeedItem> pageFeedItems = new ArrayList<>();
                        for (FeedResponse f : feeds) {
                            List<String> images = f.getReviewImageUrls(); // 서버에서 받은 전체 이미지 리스트
                            String latestImage = (images != null && !images.isEmpty()) ? images.get(images.size() - 1) : null;

                            FeedItem it = new FeedItem(
                                    f.getNickname(),
                                    f.getContent(),
                                    f.getFormattedCreatedAt(),
                                    latestImage, // 최신 이미지
                                    f.getUserProfileImage(),
                                    f.getBookId(),
                                    f.getCommentCount(),
                                    f.getLikeCount(),
                                    f.getTagId(),
                                    f.getHashtags(),
                                    f.getShareUrl(),
                                    f.getUserId()
                            );

                            it.setPostId(f.getReviewId());
                            it.setImageUrls(images != null ? new ArrayList<>(images) : new ArrayList<>()); // ✅ 전체 이미지 리스트 추가
                            it.setLikedByMe(f.isLikedByMe());
                            it.setLikedUsers(f.getLikedUsers());
                            it.setViewType(FeedItem.TYPE_FEED);
                            it.setStableId(f.getReviewId());
                            pageFeedItems.add(it);
                        }


                        // 2) 메인 리스트에 "피드 5개 + 추천 1개" 패턴으로 병합
                        //    (추천 아이템은 covers 비어있는 상태로 자리만 먼저 넣고, 나중에 채움)
                        int feedsSinceLastReco = countTailFeeds(feedList); // 현재 리스트 뒤쪽 연속 feed 개수


                        for (FeedItem it : pageFeedItems) {
                            feedList.add(it);
                            feedsSinceLastReco++;

                            if (feedsSinceLastReco == 5) {

                                // 추천 자리(placeholder) 생성
                                FeedItem reco = new FeedItem();
                                reco.setPostType("recommend");
                                reco.setViewType(FeedItem.TYPE_RECOMMEND);
                                //long recoId = System.currentTimeMillis();   // 고유 ID
                                //reco.setStableId(recoId);
                                long recoId = -1000L - nextRecommendPage; // 음수로, 페이지마다 고유
                                reco.setStableId(recoId);
                                reco.setRecommendCovers(
                                        cachedRecommendedCovers != null ? new ArrayList<>(cachedRecommendedCovers) : new ArrayList<>()
                                );
                                feedList.add(reco);

                                // 비동기로 이 블록만의 추천 커버 요청
                                loadRecommendBlock(token, userId, nextRecommendPage, recoId, covers -> {
                                    int posNow = findItemPositionById(recoId);
                                    if (posNow != -1 && posNow < feedList.size()) {
                                        FeedItem block = feedList.get(posNow);
                                        if (block.getViewType() == FeedItem.TYPE_RECOMMEND) {
                                            block.setRecommendCovers(new ArrayList<>(covers)); // 커버 세팅
                                            feedAdapter.notifyItemChanged(posNow);             // UI 갱신
                                        }
                                    }
                                });


                                nextRecommendPage++;       // ✅ 추천 페이지 증가
                                feedsSinceLastReco = 0;    // 리셋
                            }
                        }

                        Log.d("FeedPaging", "요청 page=" + pageToLoad + ", size=" + PAGE_SIZE);

                        feedAdapter.notifyDataSetChanged(); // 또는 범위 삽입으로 최적화
                        //int start = feedList.size();
                        //feedList.addAll(pageFeedItems);
                        //feedAdapter.notifyItemRangeInserted(start, pageFeedItems.size());

                        //isLastPage = pg.isLast();
                        // 수정
                        isLastPage = feeds.isEmpty(); // 받아온 데이터가 완전히 비었을 때만 멈춤
                        if (!isLastPage) currentPage++;
                        finishLoading(true);

                        // ✅ 알림에서 온 postId 처리
                        if (pendingPostId != null) {
                            scrollToTargetFeed(pendingPostId);
                            pendingPostId = null;
                        }


                    }

                    @Override public void onFailure(Call<ApiResponse<FeedPageResponse>> call, Throwable t) {
                        finishLoading(false);
                    }
                });
    }

    private void finishLoading(boolean ok){
        isLoading = false;
        swipeRefreshLayout.setRefreshing(false);
    }

    // 현재 리스트 끝에서부터 연속된 FEED 개수 계산
    private int countTailFeeds(List<FeedItem> list){
        int c=0;
        for (int i=list.size()-1; i>=0; --i){
            if (list.get(i).getViewType()==FeedItem.TYPE_FEED) c++;
            else break;
        }
        return c;
    }

    private int findItemPositionById(long id){
        for (int i=0;i<feedList.size();i++){
            if (feedList.get(i).getStableId()==id) return i;
        }
        return -1;
    }


    // 로딩 종료 공통처리
    private void onFeedLoadComplete(boolean success) {
        isLoading = false;
        swipeRefreshLayout.setRefreshing(false);
        Log.d("Feed", success ? "✅ 로드 완료" : "❌ 로드 실패");
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
    //피드 수정 갱신 (최신 이미지 반영)
    private void updateFeedInList(FeedResponse updatedFeed) {
        Log.d("FeedRefresh", "updateFeedInList 호출, reviewId=" + updatedFeed.getReviewId());
        for (int i = 0; i < feedList.size(); i++) {
            FeedItem item = feedList.get(i);
            if (item.getPostId() == updatedFeed.getReviewId()) { // postId와 reviewId 비교
                Log.d("FeedRefresh", "수정 대상 피드 발견, position=" + i);

                // FeedItem 필드 업데이트
                item.setReviewContent(updatedFeed.getContent());
                item.setBookId(updatedFeed.getBookId());
                item.setHashtags(updatedFeed.getHashtags());

                List<String> images = updatedFeed.getReviewImageUrls();
                if (images != null && !images.isEmpty()) {
                    // 최신 이미지를 FeedItem에 반영
                    item.setImageUrl(images.get(images.size() - 1));  // 마지막 이미지 사용
                    item.setImageUrls(new ArrayList<>(images));       // 전체 이미지 리스트도 저장
                } else {
                    item.setImageUrl(null);
                    item.setImageUrls(new ArrayList<>());
                }

                Log.d("FeedRefresh", "feedAdapter.notifyItemChanged 호출 완료, position=" + i);
                feedAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private interface CoversCallback { void onResult(List<String> covers); }

    private void loadRecommendBlock(String token, int userId, int recoPage, long targetId, CoversCallback cb) {
        ApiClient.getClient(token).create(RecommendBookApi.class)
                .getRecommendedBooks("Bearer " + token, userId, recoPage, 6)
                .enqueue(new Callback<ApiResponse<RecommendBookPageResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<RecommendBookPageResponse>> call,
                                           Response<ApiResponse<RecommendBookPageResponse>> res) {

                        List<String> covers = new ArrayList<>();

                        if (res.isSuccessful() && res.body() != null && res.body().getData() != null) {
                            List<RecommendBookResponse> books = res.body().getData().getContent();

                            for (RecommendBookResponse b : books) {
                                if (b.getBookCoverUrl() != null)
                                    covers.add(b.getBookCoverUrl());
                            }

                            // ✅ ID로 정확히 해당 추천 블록을 찾아서 데이터 업데이트
                            int posNow = findItemPositionById(targetId);
                            if (posNow != -1) {
                                FeedItem block = feedList.get(posNow);
                                block.setRecommendBooks(new ArrayList<>(books));
                                feedAdapter.notifyItemChanged(posNow); // 갱신
                            }
                        }

                        cb.onResult(covers);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<RecommendBookPageResponse>> call, Throwable t) {
                        cb.onResult(new ArrayList<>());
                    }
                });
    }

    // 콜백 인터페이스
    interface RecommendCallback {
        void onResult(List<String> covers);
    }


    // ✅ 추천 커버 리스트 반환
    private List<String> getCachedRecommendedCovers() {
        // 아직 추천 커버를 불러오지 않았다면 기본 더미 리스트를 반환
        if (cachedRecommendedCovers == null || cachedRecommendedCovers.isEmpty()) {
            List<String> dummy = new ArrayList<>();
            dummy.add("https://image.aladin.co.kr/product/37/1/cover/893746067x_3.jpg");
            dummy.add("https://image.aladin.co.kr/product/17915/57/cover/k462534038_1.jpg");
            dummy.add("https://image.aladin.co.kr/product/2611/13/cover/8996991349_1.jpg");
            dummy.add("https://image.aladin.co.kr/product/31081/19/cover/8959897316_1.jpg");
            return dummy;
        }
        return cachedRecommendedCovers;
    }

}
