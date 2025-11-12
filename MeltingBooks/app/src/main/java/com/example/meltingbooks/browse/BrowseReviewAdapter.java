package com.example.meltingbooks.browse;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;
import com.example.meltingbooks.feed.FeedDetailActivity;
import com.example.meltingbooks.feed.FeedItem;
import com.example.meltingbooks.network.feed.FeedResponse;

import java.util.List;

public class BrowseReviewAdapter extends RecyclerView.Adapter<BrowseReviewAdapter.ReviewViewHolder> {

    private List<FeedItem> reviewList;
    private final Context context;
    private ActivityResultLauncher<Intent> detailLauncher;

    public BrowseReviewAdapter(Context context, List<FeedItem> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }
    public void updateReviews(List<FeedItem> newReviewList) {
        this.reviewList.clear();
        this.reviewList.addAll(newReviewList); // FeedPageResponse.getContent()로 받은 리스트 전달
        notifyDataSetChanged();
    }


    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView userName, reviewDate, reviewContent;
        LinearLayout ratingContainer; // ★ 새로 추가

        public ReviewViewHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            userName = itemView.findViewById(R.id.userName);
            reviewDate = itemView.findViewById(R.id.reviewDate);
            reviewContent = itemView.findViewById(R.id.reviewContent);
            ratingContainer = itemView.findViewById(R.id.ratingContainer); // ★ 참조
        }
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.browse_book_review_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
       // FeedResponse feed = reviewList.get(position);
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

        // ⭐ 별점 표시
        float rating = feed.getRating() != null ? feed.getRating() : 0;
        int numStars = 5;
        holder.ratingContainer.removeAllViews();
        holder.ratingContainer.setBaselineAligned(false);
        holder.ratingContainer.setOrientation(LinearLayout.HORIZONTAL);
        holder.ratingContainer.setDividerPadding(0);
        holder.ratingContainer.setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);


        for (int i = 1; i <= numStars; i++) {
            ImageView star = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(22, context),
                    dpToPx(22, context)
            );
            params.setMargins(0, 0, 0, 0);
            star.setLayoutParams(params);
            star.setScaleType(ImageView.ScaleType.CENTER_CROP); // 이미지 꽉차게 // 이미지 여백 제거

            if (i <= Math.floor(rating)) {
                star.setImageResource(R.drawable.star_filled);
            } else {
                star.setImageResource(R.drawable.star_empty);
            }

            holder.ratingContainer.addView(star);
        }


        // ⭐ 아이템 전체 클릭 시에도 동일하게
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

    // ⭐ dp -> px 변환
    private int dpToPx(int dp, Context context) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
}
