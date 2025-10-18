package com.example.meltingbooks.group.comment;

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
import com.example.meltingbooks.feed.comment.CommentAdapter;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.List;

public class GroupCommentAdapter extends RecyclerView.Adapter<GroupCommentAdapter.CommentViewHolder> {

    private final Context context;
    private final List<GroupCommentItem> commentList;
    private final int currentUserId; // 로그인 유저 ID
    private CommentAdapter.OnDeleteCommentListener deleteListener; // ← 이걸 추가



    // ✅ 하나의 생성자만 사용, null이면 빈 리스트로 초기화
    public GroupCommentAdapter(Context context, List<GroupCommentItem> commentList, int currentUserId) {
        this.context = context;
        this.commentList = commentList != null ? commentList : new ArrayList<>();
        this.currentUserId = currentUserId;
    }


    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        GroupCommentItem comment = commentList.get(position);

        holder.commentUserName.setText(comment.getUserName());  // ✅ 변수명 정리 (userName → authorName 권장)
        holder.commentContent.setText(comment.getContent());
        holder.commentDate.setText(comment.getCommentDate());

        // ✅ 프로필 이미지 Glide 처리
        if (comment.getProfileImageUrl() != null && !comment.getProfileImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(comment.getProfileImageUrl())
                    .placeholder(R.drawable.sample_profile)
                    .error(R.drawable.sample_profile)
                    .into(holder.commentProfileImage);
        } else {
            holder.commentProfileImage.setImageResource(R.drawable.sample_profile);
        }

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
        return commentList != null ? commentList.size() : 0;
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

    public void setOnDeleteCommentListener(CommentAdapter.OnDeleteCommentListener listener) {
        this.deleteListener = listener;
    }
}
