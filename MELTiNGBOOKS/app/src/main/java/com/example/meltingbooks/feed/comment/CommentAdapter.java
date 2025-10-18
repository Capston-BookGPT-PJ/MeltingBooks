package com.example.meltingbooks.feed.comment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<CommentItem> commentList;
    private final Context context;
    private final int currentUserId; // 로그인 유저 ID
    private OnDeleteCommentListener deleteListener; // ← 이걸 추가


    public CommentAdapter(Context context, List<CommentItem> commentList, int currentUserId) {
        this.context = context;
        this.commentList = commentList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentItem comment = commentList.get(position);
        holder.commentUserName.setText(comment.getUserName());
        holder.commentContent.setText(comment.getContent());
        holder.commentDate.setText(comment.getCommentDate()); // 작성일 표시

        // ✅ 프로필 이미지 (Glide로 URL 처리)
        Glide.with(holder.itemView.getContext())
                .load(comment.getProfileImageUrl()) // String URL
                .placeholder(R.drawable.sample_profile) // 로딩 중 기본 이미지
                .error(R.drawable.sample_profile)       // 실패 시 기본 이미지
                .into(holder.commentProfileImage);

        // 작성자가 본인일 경우 삭제 버튼 표시
        if (comment.getUserId() == currentUserId) {
            holder.deleteComment.setVisibility(View.VISIBLE);
        } else {
            holder.deleteComment.setVisibility(View.GONE);
        }

        holder.deleteComment.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteComment(comment.getCommentId(), position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        TextView commentUserName, commentContent, commentDate, deleteComment;
        ImageView commentProfileImage;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commentUserName = itemView.findViewById(R.id.commentUserName);
            commentContent = itemView.findViewById(R.id.commentContent);
            commentProfileImage = itemView.findViewById(R.id.commentProfileImage);
            commentDate = itemView.findViewById(R.id.commentDate);
            deleteComment = itemView.findViewById(R.id.deleteComment);
        }
    }

    /** 삭제 인터페이스 */
    public interface OnDeleteCommentListener {
        void onDeleteComment(int commentId, int position);
    }

    public void setOnDeleteCommentListener(OnDeleteCommentListener listener) {
        this.deleteListener = listener;
    }
}