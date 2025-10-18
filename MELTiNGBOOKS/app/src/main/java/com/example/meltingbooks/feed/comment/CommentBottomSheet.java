package com.example.meltingbooks.feed.comment;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.R;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.feed.CommentRequest;
import com.example.meltingbooks.network.feed.CommentResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentBottomSheet extends BottomSheetDialogFragment {

    private CommentAdapter commentAdapter;
    private List<CommentItem> commentList;
    private OnCommentAddedListener onCommentAddedListener;

    //feed와 group 구분 코드

    private int postId;
    private String postType;

    private int currentUserId;

    public interface OnCommentAddedListener {
        void onCommentAdded(int commentCount);
    }

    public void setOnCommentAddedListener(OnCommentAddedListener listener) {
        this.onCommentAddedListener = listener;
    }


    @Override // 댓글 화면 초기 설정을 위함
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        // 다이얼로그가 풀스크린으로 보이도록 설정
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                // 높이를 부모 뷰의 전체 높이로 설정하여 상단 고정
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(bottomSheet.getLayoutParams());
            }
        });

        return dialog;
    }

    //GroupAdapter 부분
    public static CommentBottomSheet newInstance(int postId, String postType) {
        CommentBottomSheet fragment = new CommentBottomSheet();
        Bundle args = new Bundle();
        args.putInt("postId", postId);
        args.putString("postType", postType);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getInt("postId");
            postType = getArguments().getString("postType");
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("auth", MODE_PRIVATE);
        currentUserId = prefs.getInt("userId", -1);

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            // 배경을 투명하게 설정
            getDialog().getWindow().setDimAmount(0f);
        }

        View view = inflater.inflate(R.layout.comment_bottom_sheet, container, false);

        RecyclerView commentRecyclerView = view.findViewById(R.id.commentRecyclerView);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(getContext(), commentList, currentUserId);
        commentRecyclerView.setAdapter(commentAdapter);

        //댓글 삭제 리스너
        commentAdapter.setOnDeleteCommentListener((commentId, position) -> {
            SharedPreferences prefs = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE);
            String token = prefs.getString("jwt", null);
            int userId = prefs.getInt("userId", -1);
            if (token == null) return;

            ApiService apiService = ApiClient.getClient(token).create(ApiService.class);
            apiService.deleteComment("Bearer " + token, commentId, userId)
                    .enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                            if (response.isSuccessful()) {
                                commentList.remove(position);
                                commentAdapter.notifyItemRemoved(position);

                                //댓글 수 업데이트
                                if (onCommentAddedListener != null) {
                                    onCommentAddedListener.onCommentAdded(commentList.size());
                                }

                            } else {
                                Log.e("Comment", "삭제 실패: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            Log.e("Comment", "삭제 에러: " + t.getMessage());
                        }
                    });
        });

        if ("feed".equals(postType)) {
            loadCommentsFromServer();
        } else {
            // 기존 group 테스트용 코드 그대로 둠
            commentList.add(new CommentItem("User1", "멋진 리뷰네요!", R.drawable.sample_profile));
            commentList.add(new CommentItem("User2", "저도 이 책 좋아해요!", R.drawable.sample_profile));
        }

        commentAdapter.notifyDataSetChanged();

        // 댓글 입력 부분 설정
        EditText commentEditText = view.findViewById(R.id.commentEditText);
        ImageView postCommentButton = view.findViewById(R.id.postCommentButton);

        /**postCommentButton.setOnClickListener(v -> {
         String comment = commentEditText.getText().toString().trim();
         if (!comment.isEmpty()) {
         // 새 댓글을 리스트에 추가
         commentList.add(new CommentItem("CurrentUser", comment, R.drawable.sample_profile));
         commentAdapter.notifyDataSetChanged();
         commentEditText.setText("");

         // 콜백 호출하여 댓글 수 업데이트 나중에 서버와 통신하도록 수정
         if (onCommentAddedListener != null) {
         onCommentAddedListener.onCommentAdded(commentList.size());
         }
         }
         });**/

        postCommentButton.setOnClickListener(v -> {
            String commentText = commentEditText.getText().toString().trim();
            if (!commentText.isEmpty()) {
                SharedPreferences prefs = requireContext().getSharedPreferences("auth", MODE_PRIVATE);
                String token = prefs.getString("jwt", null);
                int userId = prefs.getInt("userId", -1); // 기본값 -1
                if (token == null) return;

                ApiService apiService = ApiClient.getClient(token).create(ApiService.class);

                CommentRequest request = new CommentRequest(commentText);

                apiService.postComment("Bearer " + token, userId, postId, request)
                        .enqueue(new Callback<ApiResponse<CommentResponse>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<CommentResponse>> call, Response<ApiResponse<CommentResponse>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    CommentResponse newComment = response.body().getData();

                                    // 리스트에 추가
                                    commentList.add(new CommentItem(
                                            newComment.getCommentId(),
                                            newComment.getUserId(),
                                            newComment.getNickname(),
                                            newComment.getContent(),
                                            newComment.getUserProfileImage(),
                                            newComment.getFormattedCreatedAt()
                                    ));
                                    commentAdapter.notifyDataSetChanged();
                                    commentEditText.setText("");

                                    if (onCommentAddedListener != null) {
                                        onCommentAddedListener.onCommentAdded(commentList.size());
                                    }
                                } else {
                                    Log.e("Comment", "댓글 등록 실패: " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<CommentResponse>> call, Throwable t) {
                                Log.e("Comment", "댓글 등록 에러: " + t.getMessage());
                            }
                        });
            }
        });


        return view;
    }

    private void loadCommentsFromServer() {
        SharedPreferences prefs = requireContext().getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
        if (token == null) return;

        ApiService apiService = ApiClient.getClient(token).create(ApiService.class);


        apiService.getComments("Bearer " + token, postId)
                .enqueue(new Callback<ApiResponse<List<CommentResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<CommentResponse>>> call,
                                           Response<ApiResponse<List<CommentResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<CommentResponse> data = response.body().getData();
                            commentList.clear();
                            for (CommentResponse c : data) {
                                commentList.add(new CommentItem(
                                        c.getCommentId(),                 // commentId
                                        c.getUserId(),             // userId
                                        c.getNickname(),
                                        c.getContent(),
                                        c.getUserProfileImage(),
                                        c.getFormattedCreatedAt()
                                ));
                            }
                            commentAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<CommentResponse>>> call, Throwable t) {
                        Log.e("Comment", "댓글 불러오기 실패: " + t.getMessage());
                    }
                });
    }

}