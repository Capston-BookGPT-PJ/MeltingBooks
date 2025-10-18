package com.example.meltingbooks.browse;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.meltingbooks.base.BaseActivity;
import com.example.meltingbooks.feed.FeedItem;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.browse.HashtagController;
import com.example.meltingbooks.network.browse.HashtagResponse;
import com.example.meltingbooks.network.book.Book;
import com.example.meltingbooks.R;
import com.example.meltingbooks.network.book.BookController;
import com.example.meltingbooks.network.browse.PopularUser;
import com.example.meltingbooks.network.browse.UserController;
import com.example.meltingbooks.network.feed.FeedPageResponse;
import com.example.meltingbooks.network.feed.FeedResponse;

import com.example.meltingbooks.network.profile.UserResponse;
import com.example.meltingbooks.search.SearchActivity;
import com.example.meltingbooks.User;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class BrowseActivity extends BaseActivity {
    private ImageButton search;

    private ViewPager2 bookViewPager;
    private RecyclerView reviewRecyclerView;
    private BrowseBookAdapter bookAdapter;
    private BrowseReviewAdapter reviewAdapter;

    private List<Book> bookList = new ArrayList<>();
    private Map<Integer, List<FeedItem>> reviewMapByBookId = new HashMap<>();


    private View hashtagLayout;
    private FlexboxLayout hashtagFlexbox;
    private RecyclerView hashtagRecyclerView;
    private HashtagReviewsAdapter hashtagAdapter;

    private View usersLayout;
    private RecyclerView usersRecyclerView;
    private BrowseUsersAdapter userAdapter;
    private List<User> popularUserList = new ArrayList<>();


    //private HashMap<Integer, List<FeedResponse>> reviewMapByBookId = new HashMap<>();

    // 현재 선택된 태그 저장
    private TextView selectedTagView = null;

    //private List<List<String>> reviewListByBook = new ArrayList<>();

    private String token;
    private int userId;
    private ApiService apiService; // 클래스 멤버로 선언

    private int currentPage = 0; //⭐ 페이징 현재 페이지
    private final int PAGE_SIZE = 10; // ⭐한 페이지에 불러올 항목 수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);
        setupBottomNavigation();


        // UI 연결
        search = findViewById(R.id.search);

        // 인기 책
        bookViewPager = findViewById(R.id.bookViewPager);
        reviewRecyclerView = findViewById(R.id.reviewRecyclerView);

        // 인기 해시태그
        hashtagLayout = findViewById(R.id.hashtagLayout);
        hashtagFlexbox = hashtagLayout.findViewById(R.id.hashtagFlexbox);
        hashtagRecyclerView = findViewById(R.id.hashtagRecyclerView);

        //⭐ 인기 유저 수정 부분
        usersLayout = findViewById(R.id.usersLayout);
        usersRecyclerView = findViewById(R.id.popularUsersRecyclerView);
        usersRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        //⭐빈 어댑터 먼저 세팅
        userAdapter = new BrowseUsersAdapter(new ArrayList<>());
        usersRecyclerView.setAdapter(userAdapter);


        // 인기 책 ViewPager
        bookAdapter = new BrowseBookAdapter(this, bookList);
        bookViewPager.setAdapter(bookAdapter);


        /// 인기 책 리뷰 RecyclerView
        LinearLayoutManager reviewLayoutManager = new LinearLayoutManager(this);
        reviewRecyclerView.setLayoutManager(reviewLayoutManager);
        reviewAdapter = new BrowseReviewAdapter(this, new ArrayList<>());
        reviewRecyclerView.setAdapter(reviewAdapter);
        reviewRecyclerView.setNestedScrollingEnabled(false);

        // 해시태그 리뷰 RecyclerView
        LinearLayoutManager hashtagLayoutManager = new LinearLayoutManager(this);
        hashtagRecyclerView.setLayoutManager(hashtagLayoutManager);
        hashtagAdapter = new HashtagReviewsAdapter(this, new ArrayList<>());
        hashtagRecyclerView.setAdapter(hashtagAdapter);


        // SharedPreferences에서 토큰과 사용자 ID 가져오기
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("jwt", null);
        userId = prefs.getInt("userId", -1);

        if (token == null || userId == -1) {
            Log.e("BrowseActivity", "토큰 또는 사용자 ID가 없습니다.");
        } else {
            // 서버에서 해시태그 불러오기
            setupHashtags(token);
        }

        apiService = ApiClient.getClient(token).create(ApiService.class);

        //인기 책 목록 가져오기
        loadPopularBooks();
        //책 리뷰 갱신
        setupPageChangeListener();

        //⭐인기 유저 조회
        loadPopularUsers();

        // 탭 클릭 리스너
        TextView popularBooks = findViewById(R.id.popularBooks);
        TextView popularTags = findViewById(R.id.popularTags);
        TextView popularUsers = findViewById(R.id.popularUsers);

        popularBooks.setOnClickListener(v -> {
            bookViewPager.setVisibility(View.VISIBLE);
            reviewRecyclerView.setVisibility(View.VISIBLE);
            hashtagLayout.setVisibility(View.GONE);
            hashtagRecyclerView.setVisibility(View.GONE);
            usersLayout.setVisibility(View.GONE);
        });

        popularTags.setOnClickListener(v -> {
            bookViewPager.setVisibility(View.GONE);
            reviewRecyclerView.setVisibility(View.GONE);
            hashtagLayout.setVisibility(View.VISIBLE);
            hashtagRecyclerView.setVisibility(View.VISIBLE);
            usersLayout.setVisibility(View.GONE);
        });

        popularUsers.setOnClickListener(v -> {
            bookViewPager.setVisibility(View.GONE);
            reviewRecyclerView.setVisibility(View.GONE);
            hashtagLayout.setVisibility(View.GONE);
            hashtagRecyclerView.setVisibility(View.GONE);
            usersLayout.setVisibility(View.VISIBLE);
        });
    }

    private void loadPopularBooks() {
        BookController bookController = new BookController(this);

        // 1. 인기 책 불러오기
        bookController.fetchPopularBooks(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Book> books = response.body();

                    // 2. popularityScore 0인 책 제거
                    List<Book> filteredBooks = new ArrayList<>();
                    for (Book book : books) {
                        if (book.getPopularityScore() > 0) {
                            filteredBooks.add(book);
                        }
                    }

                    // 3. popularityScore 기준 내림차순 정렬
                    Collections.sort(filteredBooks, (b1, b2) ->
                            Integer.compare(b2.getPopularityScore(), b1.getPopularityScore())
                    );

                    // ⭐ bookList에 필터 적용
                    bookList.clear();
                    bookList.addAll(filteredBooks);
                    bookAdapter.notifyDataSetChanged();

                    // 책별 리뷰 가져오기
                    for (Book book : bookList) {
                        bookController.fetchReviewsByBook(book.getBookId(), new Callback<ApiResponse<List<FeedResponse>>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<List<FeedResponse>>> call, Response<ApiResponse<List<FeedResponse>>> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                    List<FeedResponse> bookReviews = response.body().getData();
                                    if (bookReviews == null) return;

                                    // 모든 리뷰 FeedItem으로 변환
                                    mapBookReviewsToFeedItems(book, bookReviews);
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<List<FeedResponse>>> call, Throwable t) {
                                Log.e("BrowseActivity", "책별 리뷰 불러오기 실패", t);
                            }
                        });
                    }

                } else {
                    Log.e("BrowseActivity", "인기 책 불러오기 실패: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                Log.e("BrowseActivity", "인기 책 불러오기 실패", t);
            }
        });
    }

    // ---------------------------
// ⭐ FeedResponse → FeedItem 매핑
    private void mapBookReviewsToFeedItems(Book book, List<FeedResponse> bookReviews) {
        List<FeedItem> feedItems = new ArrayList<>();

        for (FeedResponse review : bookReviews) {
            FeedItem feedItem = new FeedItem(
                    review.getNickname(),
                    review.getContent(),
                    review.getFormattedCreatedAt(),
                    (review.getReviewImageUrls() != null && !review.getReviewImageUrls().isEmpty())
                            ? review.getReviewImageUrls().get(0)
                            : null,
                    review.getUserProfileImage(),
                    review.getBookId(),
                    review.getCommentCount(),
                    review.getLikeCount(),
                    review.getTagId(),
                    review.getHashtags(),
                    review.getRating(),
                    review.getUserId()
            );
            feedItem.setPostId(review.getReviewId());
            feedItem.setPostType("feed");
            feedItems.add(feedItem);
        }

        // 책별 리뷰 저장
        reviewMapByBookId.put(book.getBookId(), feedItems);

        Log.d("BrowseActivity", "Book " + book.getBookId() + " 리뷰 개수: " + feedItems.size());

        // 현재 ViewPager 페이지가 이 책이면 바로 업데이트
        int currentPage = bookViewPager.getCurrentItem();
        if (currentPage >= 0 && currentPage < bookList.size() && bookList.get(currentPage).getBookId() == book.getBookId()) {
            reviewAdapter.updateReviews(feedItems);
        }
    }



    private void setupPageChangeListener() {
        bookViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Book book = bookList.get(position);
                //List<FeedResponse> reviews = reviewMapByBookId.get(book.getBookId());
                List<FeedItem> reviews = reviewMapByBookId.get(book.getBookId());
                reviewAdapter.updateReviews(reviews != null ? reviews : new ArrayList<>());
            }
        });
    }

    private void setupHashtags(String token) {
        HashtagController hashtagController = new HashtagController(token);

        // 인기 해시태그 불러오기
        hashtagController.fetchPopularHashtags(new Callback<ApiResponse<List<HashtagResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<HashtagResponse>>> call, Response<ApiResponse<List<HashtagResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<HashtagResponse> hashtags = response.body().getData();
                    hashtagFlexbox.removeAllViews();

                    for (HashtagResponse hashtag : hashtags) {
                        String tag = hashtag.getTag();

                        TextView tagView = new TextView(BrowseActivity.this);
                        tagView.setText(tag);
                        tagView.setTextSize(15);
                        tagView.setTextColor(ContextCompat.getColor(BrowseActivity.this, R.color.text_blue));
                        tagView.setGravity(Gravity.CENTER);
                        tagView.setBackground(ContextCompat.getDrawable(BrowseActivity.this, R.drawable.hashtag1));
                        tagView.setPadding(8, 8, 8, 8);
                        tagView.setMaxLines(4);
                        tagView.setEllipsize(TextUtils.TruncateAt.END);

                        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(12, 20, 12, 20);
                        tagView.setLayoutParams(params);

                        tagView.setOnClickListener(v -> {
                            boolean isSelected = v.isSelected();

                            // 이전 선택 해제
                            if (selectedTagView != null && selectedTagView != v) {
                                selectedTagView.setSelected(false);
                                selectedTagView.setBackground(ContextCompat.getDrawable(BrowseActivity.this, R.drawable.hashtag1));
                                selectedTagView.setTextColor(ContextCompat.getColor(BrowseActivity.this, R.color.text_blue));
                            }

                            if (!isSelected) {
                                v.setSelected(true);
                                v.setBackground(ContextCompat.getDrawable(BrowseActivity.this, R.drawable.hashtag2));
                                ((TextView) v).setTextColor(ContextCompat.getColor(BrowseActivity.this, R.color.white));
                                selectedTagView = (TextView) v;

                                Log.d("HashtagClick", "Clicked hashtag: " + tag);

                                // ⭐ 해시태그 리뷰 불러오기
                                hashtagController.fetchReviewsByHashtag(tag, new Callback<ApiResponse<FeedPageResponse>>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse<FeedPageResponse>> call, Response<ApiResponse<FeedPageResponse>> response) {
                                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                            FeedPageResponse pageResponse = response.body().getData();
                                            List<FeedResponse> hashtagReviews = (pageResponse != null && pageResponse.getContent() != null)
                                                    ? pageResponse.getContent() : new ArrayList<>();

                                            // FeedResponse → FeedItem 변환
                                            List<FeedItem> feedItems = new ArrayList<>();
                                            for (FeedResponse review : hashtagReviews) {
                                                FeedItem feedItem = new FeedItem(
                                                        review.getNickname(),
                                                        review.getContent(),
                                                        review.getFormattedCreatedAt(),
                                                        (review.getReviewImageUrls() != null && !review.getReviewImageUrls().isEmpty())
                                                                ? review.getReviewImageUrls().get(0)
                                                                : null,
                                                        review.getUserProfileImage(),
                                                        review.getBookId(),
                                                        review.getCommentCount(),
                                                        review.getLikeCount(),
                                                        review.getTagId(),
                                                        review.getHashtags(),
                                                        review.getRating(),
                                                        review.getUserId()
                                                );
                                                feedItem.setPostId(review.getReviewId());
                                                feedItem.setPostType("feed");
                                                feedItems.add(feedItem);
                                            }

                                            // 어댑터 갱신
                                            hashtagAdapter.updateReviews(feedItems);
                                            hashtagRecyclerView.setVisibility(feedItems.isEmpty() ? View.GONE : View.VISIBLE);

                                            Log.d("BrowseActivity", "Hashtag " + tag + " 리뷰 개수: " + feedItems.size());
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponse<FeedPageResponse>> call, Throwable t) {
                                        Log.e("BrowseActivity", "해시태그 리뷰 불러오기 실패", t);
                                    }
                                });

                            } else {
                                // 해시태그 선택 해제
                                v.setSelected(false);
                                v.setBackground(ContextCompat.getDrawable(BrowseActivity.this, R.drawable.hashtag1));
                                ((TextView) v).setTextColor(ContextCompat.getColor(BrowseActivity.this, R.color.text_blue));
                                selectedTagView = null;

                                hashtagRecyclerView.setVisibility(View.GONE);
                                hashtagAdapter.updateReviews(new ArrayList<>());
                            }
                        });

                        hashtagFlexbox.addView(tagView);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<HashtagResponse>>> call, Throwable t) {
                Log.e("Hashtag", "인기 해시태그 불러오기 실패", t);
            }
        });

        // 검색 버튼
        search.setOnClickListener(v -> {
            Intent intent = new Intent(BrowseActivity.this, SearchActivity.class);
            startActivity(intent);
        });
    }


    //⭐인기 유저 조회
    private void loadPopularUsers() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
        if (token == null) return;

        UserController userController = new UserController(token);
        userController.fetchPopularUsers(new UserController.PopularUsersCallback() {
            @Override
            public void onSuccess(List<PopularUser> users) {
                // 응답 로그 출력
                Log.d("BrowseActivity", "Fetched popular users: " + users.size());
                for (PopularUser user : users) {
                    Log.d("BrowseActivity",
                            "User -> id: " + user.getId() +
                                    ", nickname: " + user.getNickname() +
                                    ", bio: " + user.getBio());
                }

                runOnUiThread(() -> {
                    // 어댑터 새로 안만들고 데이터 갱신
                    userAdapter.updateUsers(users);
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("BrowseActivity", "Failed to load popular users: " + errorMessage);
            }
        });
    }


    //bottom Navigation의 위치 설정
    @Override
    protected int getCurrentNavItemId() {
        return R.id.Browser;
    }
}
