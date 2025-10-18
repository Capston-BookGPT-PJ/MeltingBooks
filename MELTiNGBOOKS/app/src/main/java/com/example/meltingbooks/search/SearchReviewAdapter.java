package com.example.meltingbooks.search;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;
import com.example.meltingbooks.feed.FeedDetailActivity;
import com.example.meltingbooks.feed.FeedItem;

import java.util.List;

public class SearchReviewAdapter extends RecyclerView.Adapter<SearchReviewAdapter.ReviewViewHolder> {

    private List<FeedItem> reviewList;
    private final Context context;

    // ✅ 클래스명과 일치하게 수정
    public SearchReviewAdapter(Context context, List<FeedItem> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }
    public void updateReviews(List<FeedItem> newReviewList) {
        this.reviewList.clear();
        this.reviewList.addAll(newReviewList);
        notifyDataSetChanged();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView userName, reviewDate, reviewContent;
        RatingBar bookRatingBar;

        public ReviewViewHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            userName = itemView.findViewById(R.id.userName);
            reviewDate = itemView.findViewById(R.id.reviewDate);
            reviewContent = itemView.findViewById(R.id.reviewContent);
        }
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.browse_hashtag_review_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        FeedItem feed = reviewList.get(position);

        holder.userName.setText(feed.getUserName());
        holder.reviewContent.setText(feed.getReviewContent());
        holder.reviewDate.setText(feed.getReviewDate());

        // 프로필 표시
        if (feed.getProfileImageUrl() != null && !feed.getProfileImageUrl().isEmpty()) {
            holder.profileImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(feed.getProfileImageUrl())
                    .placeholder(R.drawable.sample_profile) // 로딩 중 기본 이미지
                    .error(R.drawable.sample_profile)       // 실패 시 기본 이미지
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setVisibility(View.VISIBLE); // GONE 대신 보이게
            holder.profileImage.setImageResource(R.drawable.sample_profile); // 기본 이미지 적용
        }

        // 클릭 시 FeedDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FeedDetailActivity.class);
            intent.putExtra("feedItem", feed); // FeedItem 통째로 전달
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }
}
