package com.example.meltingbooks.feed;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;
import com.example.meltingbooks.feed.comment.CommentAdapter;
import com.example.meltingbooks.feed.comment.CommentItem;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.book.Book;
import com.example.meltingbooks.network.book.BookController;
import com.example.meltingbooks.network.feed.CommentRequest;
import com.example.meltingbooks.network.feed.CommentResponse;
import com.example.meltingbooks.network.feed.FeedResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedDetailActivity extends AppCompatActivity {

    private int postId;
    private int currentUserId; // 현재 로그인된 사용자 ID
    private String token;
    private ApiService apiService;
    //현재 피드
    //private FeedResponse currentFeed;
    private FeedItem currentFeed;


    private ImageView profileImage, feedImage, shareButton, likeButton, postCommentButton;
    private TextView userName,reviewDate, reviewContent, hashtagContent, commentCount, likeCount;

    //책 관련 뷰
    private BookController bookController;
    LinearLayout bookInfoLayout;
    TextView bookTitle, bookAuthor, bookPublisher, bookCategory;
    ImageView bookCover;

    //해시태그
    TextView hashtag;

    // 리뷰 관련
    private ImageButton btnEditPost; // 게시글 수정 버튼
    private ImageButton btnDeletePost; // 게시글 삭제 버튼

    //댓글
    private EditText commentEditText;
    private RecyclerView commentRecyclerView;
    private CommentAdapter commentAdapter;
    private List<CommentItem> commentList = new ArrayList<>();

    private int postOwnerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_detail);

        // 상태바 색상 조정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // SharedPreferences
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("jwt", null);
        currentUserId = prefs.getInt("userId", -1);
        if (token == null || currentUserId == -1) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // FeedAdpater.java에서 FeedItem 받아오기
        currentFeed = (FeedItem) getIntent().getSerializableExtra("feedItem");
        postId = currentFeed.getPostId();
        if (currentFeed == null) {
            Toast.makeText(this, "게시글 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        postOwnerId = currentFeed.getUserId(); // 게시글 작성자 ID

        // 초기화
        initializeViews();
        setupRecyclerView();
        initializeApiClients();
        setupListeners();

        // 데이터 바인딩
        bindDataToViews(currentFeed);

        // 댓글 불러오기
        fetchComments();


    }


    // --- 뷰 초기화 ---
    private void initializeViews() {

        // 리뷰 관련
        // 리뷰 수정 버튼
        btnEditPost = findViewById(R.id.btn_edit_post);
        btnDeletePost = findViewById(R.id.btn_delete_post);

        // --- 본인 글일 때만 수정/삭제 버튼 보이기 ---
        if (postOwnerId == currentUserId) {
            Log.d("FeedDetail", "currentUserId=" + currentUserId + ", userId=" + postOwnerId);
            btnEditPost.setVisibility(View.VISIBLE);
            btnDeletePost.setVisibility(View.VISIBLE);
        } else {
            Log.d("FeedDetail", "currentUserId=" + currentUserId + ", userId=" + postOwnerId);
            btnEditPost.setVisibility(View.GONE);
            btnDeletePost.setVisibility(View.GONE);
        }

        // 리뷰 내용
        profileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userName);
        reviewDate = findViewById(R.id.reviewDate);
        shareButton = findViewById(R.id.share_Button);
        feedImage = findViewById(R.id.feedImage);
        reviewContent = findViewById(R.id.reviewContent);
        hashtagContent = findViewById(R.id.hashtagContent);
        commentCount = findViewById(R.id.comment_count);
        likeCount = findViewById(R.id.like_count);
        likeButton = findViewById(R.id.like_Button);

        // 댓글
        commentEditText = findViewById(R.id.commentEditText);
        postCommentButton = findViewById(R.id.postCommentButton);

        // 책 정보 뷰
        bookInfoLayout = findViewById(R.id.bookInfoLayout);
        bookTitle = bookInfoLayout.findViewById(R.id.bookInfoTitle);
        bookAuthor = bookInfoLayout.findViewById(R.id.bookInfoAuthor);
        bookPublisher = bookInfoLayout.findViewById(R.id.bookInfoPublisher);
        bookCategory = bookInfoLayout.findViewById(R.id.bookInfoCategory);
        bookCover = bookInfoLayout.findViewById(R.id.bookCover);

    }

    private void setupRecyclerView() {
        commentRecyclerView = findViewById(R.id.commentRecyclerView);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(this, commentList, currentUserId);
        commentRecyclerView.setAdapter(commentAdapter);

        // 댓글 삭제 리스너 설정
        commentAdapter.setOnDeleteCommentListener((commentId, position) -> {
            Log.d("FeedDetail", "삭제 클릭 - commentId=" + commentId + ", position=" + position);
            deleteComment(commentId, position);
        });
    }


    private void initializeApiClients() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("jwt", null);
        currentUserId = prefs.getInt("userId", -1);

        if (token == null || currentUserId == -1) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        apiService = ApiClient.getClient(token).create(ApiService.class);
        bookController = new BookController(this);
    }

    // --- 이벤트 리스너 설정 ---
    private void setupListeners() {
        // 게시글 수정 / 삭제 버튼 리스너 (자신이 작성한 게시글에만 보임)
        // FeedDetailActivity에서 수정 버튼 클릭
        btnEditPost.setOnClickListener(v -> {
            Intent intent = new Intent(FeedDetailActivity.this, FeedWriteActivity.class);
            intent.putExtra("postId", postId);
            intent.putExtra("isEdit", true);
            //feedEditLauncher.launch(intent);
            startActivity(intent);
            finish(); // FeedActivity로 돌아갈 때 onNewIntent로 refresh 신호 처리
        });

        btnDeletePost.setOnClickListener(v -> {
            if (postId == -1 || currentFeed == null) {
                Toast.makeText(this, "게시글 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            apiService = ApiClient.getClient(token).create(ApiService.class);

            apiService.deleteReview("Bearer " + token, postId, currentUserId)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(FeedDetailActivity.this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();

                                // 삭제 후 FeedActivity 갱신
                                Intent intent = new Intent(FeedDetailActivity.this, FeedActivity.class);
                                intent.putExtra("refreshFeed", true); // 새로고침 신호
                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish(); // FeedActivity로 돌아가기
                            } else {
                                Toast.makeText(FeedDetailActivity.this, "리뷰 삭제 실패", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(FeedDetailActivity.this, "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });


        likeButton.setOnClickListener(v -> toggleLike());

        postCommentButton.setOnClickListener(v -> postComment());
    }



    private void bindDataToViews(FeedItem feed) {

        // 기본 정보
        userName.setText(feed.getUserName());
        reviewContent.setText(feed.getReviewContent());
        reviewDate.setText(feed.getReviewDate());
        commentCount.setText(String.valueOf(feed.getCommentCount()));
        likeCount.setText(String.valueOf(feed.getLikeCount()));
        likeButton.setImageResource(feed.isLikedByMe() ? R.drawable.feed_like_full : R.drawable.feed_like_button);

        // 프로필 이미지
        if (feed.getProfileImageUrl() != null && !feed.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(feed.getProfileImageUrl())
                    .placeholder(R.drawable.sample_profile)
                    .error(R.drawable.sample_profile)
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.sample_profile);
        }

        // 피드 이미지
        if (feed.getImageUrl() != null && !feed.getImageUrl().isEmpty()) {
            feedImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(feed.getImageUrl())
                    .centerCrop()
                    .into(feedImage);
        } else {
            feedImage.setVisibility(View.GONE);
        }

        // 해시태그
        if (feed.getHashtags() != null && !feed.getHashtags().isEmpty()) {
            hashtagContent.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();
            // 중복 제거를 위해 Set 사용
            Set<String> uniqueTags = new LinkedHashSet<>(feed.getHashtags()); // 순서 유지

            for (String tag : uniqueTags) {
                if (!tag.startsWith("#")) {
                    sb.append("#");
                }
                sb.append(tag).append(" ");
            }
            hashtagContent.setText(sb.toString().trim());
        } else {
            hashtagContent.setVisibility(View.GONE);
        }

        // 책 정보 바인딩 (FeedAdapter 방식)
        if (feed.getBookId() != null && feed.getBookId() > 0) {
            bookInfoLayout.setVisibility(View.VISIBLE);
            bookController.getBookDetail(feed.getBookId(), new Callback<Book>() {
                @Override
                public void onResponse(Call<Book> call, Response<Book> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Book book = response.body();
                        feed.setBook(book); // 캐싱

                        bookInfoLayout.setVisibility(View.VISIBLE); // 여기서 보여주기

                        bookTitle.setText(book.getTitle());
                        bookAuthor.setText(book.getAuthor());
                        bookPublisher.setText(book.getPublisher());
                        bookCategory.setText(book.getCategoryName());
                        Glide.with(FeedDetailActivity.this).load(book.getCover()).into(bookCover);
                    } else {
                        bookInfoLayout.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<Book> call, Throwable t) {
                    bookInfoLayout.setVisibility(View.GONE);
                }
            });
        } else {
            bookInfoLayout.setVisibility(View.GONE);
        }
    }


    private void fetchComments() {
        apiService.getComments("Bearer " + token, postId)
                .enqueue(new Callback<ApiResponse<List<CommentResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<CommentResponse>>> call, Response<ApiResponse<List<CommentResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            List<CommentResponse> responseList = response.body().getData();
                            commentList.clear();

                            for (CommentResponse c : responseList) {
                                commentList.add(new CommentItem(
                                        c.getCommentId(),
                                        c.getUserId(),
                                        c.getNickname(),
                                        c.getContent(),
                                        c.getUserProfileImage(),   // ✅ 서버 값 사용
                                        c.getFormattedCreatedAt()
                                ));
                            }

                            commentAdapter.notifyDataSetChanged();

                            // 댓글 수 업데이트
                            if (currentFeed != null) {
                                currentFeed.setCommentCount(responseList.size());
                                commentCount.setText(String.valueOf(currentFeed.getCommentCount()));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<CommentResponse>>> call, Throwable t) {
                        Log.e("Comments", "댓글 로드 실패: " + t.getMessage());
                    }
                });
    }

    private void postComment() {
        String commentContent = commentEditText.getText().toString().trim();
        if (commentContent.isEmpty()) {
            Toast.makeText(this, "댓글 내용을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        CommentRequest request = new CommentRequest(commentContent);

        apiService.postComment("Bearer " + token,  currentUserId, postId, request)
                .enqueue(new Callback<ApiResponse<CommentResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<CommentResponse>> call, Response<ApiResponse<CommentResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(FeedDetailActivity.this, "댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                            Log.d("postComment", "postId=" + postId + ", currentUserId=" + currentUserId + ", content=" + request.getContent());


                            // 입력창 비우기
                            commentEditText.setText("");

                            // 등록 후 전체 댓글 다시 불러오기
                            fetchComments();

                            // 댓글 수 증가
                            if (currentFeed != null) {
                                currentFeed.setCommentCount(currentFeed.getCommentCount() + 1);
                                commentCount.setText(String.valueOf(currentFeed.getCommentCount()));
                            }

                        } else {
                            Toast.makeText(FeedDetailActivity.this, "댓글 등록 실패", Toast.LENGTH_SHORT).show();
                            Log.e("postComment", "응답 코드: " + response.code());
                            Log.e("postComment", "응답 바디: " + new Gson().toJson(response.body()));

                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<CommentResponse>> call, Throwable t) {
                        Toast.makeText(FeedDetailActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    /**
     * 댓글 삭제 처리
     */
    private void deleteComment(int commentId, int position) {
        Log.d("FeedDetail", "deleteComment 호출 - commentId=" + commentId + ", position=" + position);

        if (apiService == null || token == null) return;

        apiService.deleteComment("Bearer " + token, commentId, currentUserId)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        Log.d("FeedDetail", "삭제 응답 - commentId=" + commentId + ", code=" + response.code());

                        if (response.isSuccessful()) {
                            Toast.makeText(FeedDetailActivity.this, "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();

                            commentList.remove(position);
                            commentAdapter.notifyItemRemoved(position);

                            if (currentFeed != null) {
                                currentFeed.setCommentCount(currentFeed.getCommentCount() - 1);
                                commentCount.setText(String.valueOf(currentFeed.getCommentCount()));
                            }
                        } else {
                            Toast.makeText(FeedDetailActivity.this, "댓글 삭제 실패", Toast.LENGTH_SHORT).show();
                            Log.e("deleteComment", "삭제 실패 - commentId=" + commentId + ", body=" + response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Toast.makeText(FeedDetailActivity.this, "댓글 삭제 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("deleteComment", "삭제 에러 - commentId=" + commentId, t);
                    }
                });
    }

    private void toggleLike() {
        if (currentFeed == null) return;

        //boolean newState = !currentFeed.isLiked();
        //currentFeed.setLiked(newState);
        boolean oldState = currentFeed.isLikedByMe(); // ✅ 기존 상태
        int oldCount = currentFeed.getLikeCount();    // ✅ 기존 카운트 저장

        boolean newState = !oldState;
        currentFeed.setLikedByMe(newState); // ✅ likedByMe로 변경

        // UI 즉시 반영
        likeButton.setImageResource(newState ? R.drawable.feed_like_full : R.drawable.feed_like_button);
        int newCount = currentFeed.getLikeCount() + (newState ? 1 : -1);
        currentFeed.setLikeCount(newCount);
        likeCount.setText(String.valueOf(newCount));

        // 서버 요청
        Call<ApiResponse<Void>> call = newState
                ? apiService.likeReview("Bearer " + token, postId)
                : apiService.unlikeReview("Bearer " + token, postId);

        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (!response.isSuccessful() || (response.body() != null && !response.body().isSuccess())) {
                    rollbackLike(!newState);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                rollbackLike(!newState);
            }
        });
    }

    private void rollbackLike(boolean correctState) {
        if (currentFeed == null) return;

        //currentFeed.setLiked(correctState);
        currentFeed.setLikedByMe(correctState); // ✅ likedByMe 복원
        likeButton.setImageResource(correctState ? R.drawable.feed_like_full : R.drawable.feed_like_button);

        // UI 즉시 반영
        int correctedCount = correctState
                ? currentFeed.getLikeCount() + 1
                : currentFeed.getLikeCount() - 1;
        currentFeed.setLikeCount(correctedCount);
        likeCount.setText(String.valueOf(correctedCount));
    }


}