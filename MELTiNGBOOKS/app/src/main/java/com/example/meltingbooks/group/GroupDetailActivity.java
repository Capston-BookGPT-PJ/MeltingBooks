package com.example.meltingbooks.group;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;
import com.example.meltingbooks.feed.FeedDetailActivity;
import com.example.meltingbooks.group.comment.GroupCommentAdapter;
import com.example.meltingbooks.group.comment.GroupCommentItem;
import com.example.meltingbooks.group.write.GroupWriteActivity;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.group.GroupApi;
import com.example.meltingbooks.network.group.comment.GroupCommentPageResponse;
import com.example.meltingbooks.network.group.comment.GroupCommentRequest;
import com.example.meltingbooks.network.group.comment.GroupCommentResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupDetailActivity extends AppCompatActivity {

    private RecyclerView commentRecyclerView;
    private GroupCommentAdapter commentAdapter;
    private List<GroupCommentItem> commentList= new ArrayList<>();;
    private EditText commentEditText;
    private ImageView postCommentButton;

    // 게시글 카드뷰 내부
    private ImageView postUserProfile, groupImage;
    private TextView postUserName;
    private TextView postDate;
    private TextView postTitle;
    private TextView postContent;

    // 하단 아이콘
    private ImageView likeButton;
    private TextView likeCount, commentCount;

    // 리뷰 관련
    private ImageButton btnEditPost; // 게시글 수정 버튼
    private ImageButton btnDeletePost; // 게시글 삭제 버튼


    private int postId;
    private int groupId;
    private int userId;
    private GroupFeedItem currentFeed;
    private int currentUserId; // 현재 로그인된 사용자 ID
    private String token;
    private ApiService apiService;
    private GroupApi groupApi;

    private int postOwnerId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        // 상태바 색상 조정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }



        postUserProfile = findViewById(R.id.postUserProfile);
        postUserName = findViewById(R.id.postUserName);
        postDate = findViewById(R.id.postDate);

        postTitle = findViewById(R.id.postTitle);
        postContent = findViewById(R.id.postContent);
        groupImage = findViewById(R.id.groupImage);


        //댓글

        commentCount = findViewById(R.id.comment_count);
        likeCount = findViewById(R.id.like_count);
        likeButton = findViewById(R.id.like_button);



        //chatButton = findViewById(R.id.chat_button);
        commentEditText = findViewById(R.id.commentEditText);
        postCommentButton = findViewById(R.id.postCommentButton);

        // 리뷰 수정 버튼
        btnEditPost = findViewById(R.id.btn_edit_post);
        btnDeletePost = findViewById(R.id.btn_delete_post);


        // SharedPreferences
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("jwt", null);
        currentUserId = prefs.getInt("userId", -1);
        if (token == null || currentUserId == -1) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = ApiClient.getClient(token).create(ApiService.class);
        groupApi = ApiClient.getClient(token).create(GroupApi.class);

        // FeedAdpater.java에서 FeedItem 받아오기
        currentFeed = (GroupFeedItem) getIntent().getSerializableExtra("groupFeedItem");


        // Intent에서 groupId / postId 가져오기
        groupId = currentFeed.getGroupId();
        postId = currentFeed.getPostId();

        postOwnerId = currentFeed.getUserId(); // 게시글 작성자 ID

        Log.d("GroupDetail", "groupId=" + groupId + ", postId=" + postId);

        if (currentFeed == null) {
            Toast.makeText(this, "게시글 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- 본인 글일 때만 수정/삭제 버튼 보이기 ---
        if (postOwnerId == currentUserId) {
            Log.d("GroupDetail", "currentUserId=" + currentUserId + ", userId=" + postOwnerId);
            btnEditPost.setVisibility(View.VISIBLE);
            btnDeletePost.setVisibility(View.VISIBLE);
        } else {
            Log.d("GroupDetail", "currentUserId=" + currentUserId + ", userId=" + postOwnerId);
            btnEditPost.setVisibility(View.GONE);
            btnDeletePost.setVisibility(View.GONE);
        }



        // 초기화
        setupRecyclerView();
        setupListeners();

        // 데이터 바인딩
        bindDataToViews(currentFeed);

        // 댓글 불러오기
        fetchComments();
    }


    //댓글 리사이클러뷰
    private void setupRecyclerView() {
        commentRecyclerView = findViewById(R.id.commentRecyclerView);

        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new GroupCommentAdapter(this, commentList, currentUserId);
        commentRecyclerView.setAdapter(commentAdapter);

        // 댓글 삭제 리스너 설정
        commentAdapter.setOnDeleteCommentListener((commentId, position) -> {
            Log.d("FeedDetail", "삭제 클릭 - commentId=" + commentId + ", position=" + position);
            deleteComment(commentId, position);
        });
    }


    // --- 이벤트 리스너 설정 ---
    private void setupListeners() {
        // 게시글 수정 버튼 리스너
        btnEditPost.setOnClickListener(v -> {
            Log.d("EditPostClick", "groupId=" + groupId + ", postId=" + postId + ", isEdit=true");

            //int groupId = currentFeed.getGroupId();
            Intent intent = new Intent(GroupDetailActivity.this, GroupWriteActivity.class);
            intent.putExtra("groupId", groupId);
            intent.putExtra("postId", postId);
            intent.putExtra("isEdit", true);
            Log.d("EditPostClick", "Intent 생성 후 putExtra 완료");

            // feedEditLauncher는 ActivityResultLauncher<Intent> 로 선언해둬야 함
            //feedEditLauncher.launch(intent);
            startActivity(intent);
            finish();
        });

        // 게시글 삭제 버튼 리스너
        btnDeletePost.setOnClickListener(v -> {
            //int groupId = currentFeed.getGroupId();
            if (postId == -1 || currentFeed == null) {
                Toast.makeText(this, "게시글 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            groupApi =  ApiClient.getClient(token).create(GroupApi.class);

            groupApi.deletePost("Bearer " + token, groupId, postId)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(GroupDetailActivity.this, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                // 삭제 후 FeedActivity 갱신
                                Intent intent = new Intent(GroupDetailActivity.this, GroupFeedActivity.class);
                                intent.putExtra("refreshPost", true); // 새로고침 신호
                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish(); // GroupFeedActivity로 돌아가기
                            } else {
                                Toast.makeText(GroupDetailActivity.this, "게시글 삭제 실패", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(GroupDetailActivity.this, "서버 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // 좋아요 버튼
        likeButton.setOnClickListener(v -> toggleLike());

        // 댓글 작성 버튼
        postCommentButton.setOnClickListener(v -> postComment());
    }


    private void bindDataToViews(GroupFeedItem feed) {
        // 게시글 작성자와 본문
        postUserName.setText(feed.getUserName());
        postTitle.setText(feed.getTitle());
        postContent.setText(feed.getContent());
        postDate.setText(feed.getCreatedAt());


        // 댓글 / 좋아요
        commentCount.setText(String.valueOf(feed.getCommentCount()));
        likeCount.setText(String.valueOf(feed.getLikeCount()));
        likeButton.setImageResource(feed.isLikedByMe() ? R.drawable.feed_like_full : R.drawable.feed_like_button);

        // 프로필 이미지
        if (feed.getUserProfileImage() != null && !feed.getUserProfileImage().isEmpty()) {
            Glide.with(this)
                    .load(feed.getUserProfileImage())
                    .placeholder(R.drawable.sample_profile2)
                    .error(R.drawable.sample_profile2)
                    .into(postUserProfile);
        } else {
            postUserProfile.setImageResource(R.drawable.sample_profile2);
        }

        // 게시글 이미지 (리뷰 게시글만)
        if ("REVIEW".equals(feed.getPostType()) && feed.getImageUrls() != null && !feed.getImageUrls().isEmpty()) {
            groupImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(feed.getFirstImageUrl()) // ✅ 첫 번째 이미지 사용
                    .centerCrop()
                    .into(groupImage);
        } else {
            groupImage.setVisibility(View.GONE);
        }
    }


    //현재 피드용 사용중. 그룹용 수정 필요
    private void fetchComments() {
        groupApi.getGroupComments("Bearer " + token, groupId, postId, 0, 20)
                .enqueue(new Callback<ApiResponse<GroupCommentPageResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<GroupCommentPageResponse>> call, Response<ApiResponse<GroupCommentPageResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            List<GroupCommentResponse> responseList = response.body().getData().getContent();
                            commentList.clear();

                            for (GroupCommentResponse c : responseList) {
                                commentList.add(new GroupCommentItem(
                                        c.getId(),
                                        c.getUserId(),
                                        c.getNickname(),
                                        c.getContent(),
                                        c.getProfileImageUrl(),   // ✅ 서버 값 사용
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
                    public void onFailure(Call<ApiResponse<GroupCommentPageResponse>> call, Throwable t) {
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

        GroupCommentRequest request = new GroupCommentRequest(commentContent);

        groupApi.createGroupComment("Bearer " + token, groupId, postId, request)
                .enqueue(new Callback<ApiResponse<GroupCommentResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<GroupCommentResponse>> call, Response<ApiResponse<GroupCommentResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(GroupDetailActivity.this, "댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(GroupDetailActivity.this, "댓글 등록 실패", Toast.LENGTH_SHORT).show();
                            Log.e("postComment", "응답 코드: " + response.code());
                            Log.e("postComment", "응답 바디: " + new Gson().toJson(response.body()));

                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<GroupCommentResponse>> call, Throwable t) {
                        Toast.makeText(GroupDetailActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 댓글 삭제 처리
     */
    private void deleteComment(int commentId, int position) {
        Log.d("FeedDetail", "deleteComment 호출 - commentId=" + commentId + ", position=" + position);

        if (groupApi == null || token == null) return;

        groupApi.deleteGroupComment("Bearer " + token, groupId, postId, commentId)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        Log.d("GroupDetail", "삭제 응답 - commentId=" + commentId + ", code=" + response.code());

                        if (response.isSuccessful()) {
                            Toast.makeText(GroupDetailActivity.this, "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();

                            commentList.remove(position);
                            commentAdapter.notifyItemRemoved(position);

                            if (currentFeed != null) {
                                currentFeed.setCommentCount(currentFeed.getCommentCount() - 1);
                                commentCount.setText(String.valueOf(currentFeed.getCommentCount()));
                            }
                        } else {
                            Toast.makeText(GroupDetailActivity.this, "댓글 삭제 실패", Toast.LENGTH_SHORT).show();
                            Log.e("deleteComment", "삭제 실패 - commentId=" + commentId + ", body=" + response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Toast.makeText(GroupDetailActivity.this, "댓글 삭제 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("deleteComment", "삭제 에러 - commentId=" + commentId, t);
                    }
                });
    }


    private void toggleLike() {
        if (currentFeed == null) return;

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

        currentFeed.setLikedByMe(correctState);
        likeButton.setImageResource(correctState ? R.drawable.feed_like_full : R.drawable.feed_like_button);

        // UI 즉시 반영
        int correctedCount = correctState
                ? currentFeed.getLikeCount() + 1
                : currentFeed.getLikeCount() - 1;
        currentFeed.setLikeCount(correctedCount);
        likeCount.setText(String.valueOf(correctedCount));
    }


}
