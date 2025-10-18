package com.example.meltingbooks.feed;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.feed.comment.CommentBottomSheet;
import com.example.meltingbooks.R;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.book.Book;
import com.example.meltingbooks.network.book.BookController;
import com.example.meltingbooks.profile.ProfileActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//피드 갱신용


public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private final List<FeedItem> feedList;
    private final Context context;
    private final ActivityResultLauncher<Intent> detailLauncher;

    //피드 갱신 추가
    public FeedAdapter(Context context, List<FeedItem> feedList, ActivityResultLauncher<Intent> detailLauncher) {
        this.context = context;
        this.feedList = feedList;
        this.detailLauncher = detailLauncher;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item, parent, false);
        return new FeedViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        FeedItem item = feedList.get(position);

        holder.userName.setText(item.getUserName());
        holder.reviewContent.setText(item.getReviewContent());
        holder.reviewDate.setText(item.getReviewDate());

        // 댓글/좋아요 수 연결
        holder.commentCount.setText(String.valueOf(item.getCommentCount()));
        holder.likeCount.setText(String.valueOf(item.getLikeCount()));


        // 댓글 버튼
        holder.commentButton.setOnClickListener(v -> {
            CommentBottomSheet commentBottomSheet =
                    CommentBottomSheet.newInstance(item.getPostId(), "feed");

            commentBottomSheet.setOnCommentAddedListener(commentCount -> {
                holder.commentCount.setText(String.valueOf(commentCount));
            });

            commentBottomSheet.show(
                    ((AppCompatActivity)v.getContext()).getSupportFragmentManager(),
                    "CommentBottomSheet"
            );
        });


        // 초기 상태 (좋아요 여부에 따라 이미지 설정)
        /*holder.likeButton.setImageResource(
                item.isLiked() ? R.drawable.feed_like_full : R.drawable.feed_like_button
        );*/
        holder.likeButton.setImageResource(
                item.isLikedByMe() ? R.drawable.feed_like_full : R.drawable.feed_like_button
        );
        holder.likeCount.setText(String.valueOf(item.getLikeCount()));

        //좋아요 버튼
        // 좋아요 버튼 클릭 이벤트
        /**holder.likeButton.setOnClickListener(v -> {
         boolean newState = !item.isLiked(); // 토글
         item.setLiked(newState);

         // 이미지 변경
         holder.likeButton.setImageResource(
         newState ? R.drawable.feed_like_full : R.drawable.feed_like_button
         );

         // 카운트 갱신
         int newCount = item.getLikeCount() + (newState ? 1 : -1);
         item.setLikeCount(newCount);
         holder.likeCount.setText(String.valueOf(newCount));

         // TODO: 서버에 좋아요 API 호출 필요
         });*/

        holder.likeButton.setOnClickListener(v -> {
            toggleLike(item, holder);
        });

        //공유 버튼 클릭 리스너
        holder.shareButton.setOnClickListener(v -> {

            String shareUrl = String.valueOf(item.getShareUrl()); //⭐수정

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);
            v.getContext().startActivity(Intent.createChooser(shareIntent, "공유하기"));

            /**
             Intent intent = new Intent(context, FeedItemActivity.class);
             intent.putExtra("postId", item.getPostId()); // 게시물 ID 전달
             context.startActivity(intent);
             */
        });

        // 이미지 표시
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            holder.feedImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(item.getImageUrl()).into(holder.feedImage);
        } else {
            holder.feedImage.setVisibility(View.GONE);
        }

        // 프로필 표시
        if (item.getProfileImageUrl() != null && !item.getProfileImageUrl().isEmpty()) {
            holder.profileImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(item.getProfileImageUrl())
                    .placeholder(R.drawable.sample_profile) // 로딩 중 기본 이미지
                    .error(R.drawable.sample_profile)       // 실패 시 기본 이미지
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setVisibility(View.VISIBLE); // GONE 대신 보이게
            holder.profileImage.setImageResource(R.drawable.sample_profile); // 기본 이미지 적용
        }

        //⭐ 사용자 프로필 이동 추가
        View.OnClickListener profileClickListener = v -> {
            Intent intent = new Intent(v.getContext(), ProfileActivity.class);
            intent.putExtra("userId", item.getUserId());
            v.getContext().startActivity(intent);
        };

        //⭐ 프로필 이미지 클릭
        holder.profileImage.setOnClickListener(profileClickListener);
        //⭐ 사용자 이름 클릭
        holder.userName.setOnClickListener(profileClickListener);

        //평점은 피드에서 표시 안함.
        Book book = item.getBook();
        Integer bookId = item.getBookId();
        if  (bookId != null && bookId > 0){
            BookController bookController = new BookController(context);
            bookController.getBookDetail(bookId, new Callback<Book>()  {
                @Override
                public void onResponse(Call<Book> call, Response<Book> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Book book = response.body();
                        item.setBook(book); // 캐싱해두면 다음에 API 안 타고 바로 표시 가능

                        holder.bookInfoLayout.setVisibility(View.VISIBLE);
                        holder.bookTitle.setText(book.getTitle());
                        holder.bookAuthor.setText(book.getAuthor());
                        holder.bookPublisher.setText(book.getPublisher());
                        holder.bookCategory.setText(book.getCategoryName());

                        Glide.with(context).load(book.getCover()).into(holder.bookCover);
                    } else {
                        Log.e("BookDetail", "실패 코드: " + response.code());
                        holder.bookInfoLayout.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<Book> call, Throwable t) {
                    Log.e("BookDetail", "에러: " + t.getMessage());
                    holder.bookInfoLayout.setVisibility(View.GONE);
                }
            });
        } else {
            holder.bookInfoLayout.setVisibility(View.GONE);
        }


        // 투표 기능
        /*if (item.hasVote()) {
            holder.voteLayout.setVisibility(View.VISIBLE);
        } else {
            holder.voteLayout.setVisibility(View.GONE);
        }
        holder.voteOption1.setOnClickListener(v -> {
            Toast.makeText(context, "예 선택!", Toast.LENGTH_SHORT).show();
        });

        holder.voteOption2.setOnClickListener(v -> {
            Toast.makeText(context, "아니오 선택!", Toast.LENGTH_SHORT).show();
        });*/


        // ✅ 해시태그 표시
        List<String> hashtags = item.getHashtags();
        if (hashtags != null && !hashtags.isEmpty()) {
            holder.hashtagContent.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();
            for (String tag : hashtags) {
                // 이미 #로 시작하면 그대로, 아니면 붙이기
                if (!tag.startsWith("#")) {
                    sb.append("#");
                }
                sb.append(tag).append(" ");
            }
            holder.hashtagContent.setText(sb.toString().trim());
        } else {
            holder.hashtagContent.setVisibility(View.GONE);
        }


        //더보기
        holder.readMore.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, FeedDetailActivity.class);
            intent.putExtra("feedItem", item); // FeedItem 전달
            context.startActivity(intent);      // 그냥 startActivity 사용
        });
    }



    @Override
    public int getItemCount() {
        return feedList.size();
    }

    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        TextView userName, reviewContent, reviewDate, commentCount, likeCount;
        ImageView commentButton, shareButton, feedImage, profileImage, likeButton;
        //LinearLayout voteLayout;
        //Button voteOption1, voteOption2;

        //책 관련 뷰
        LinearLayout bookInfoLayout;
        TextView bookTitle, bookAuthor, bookPublisher, bookCategory;
        ImageView bookCover;

        //해시태그
        TextView hashtag, hashtagContent;
        //더보기
        TextView readMore;


        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            reviewContent = itemView.findViewById(R.id.reviewContent);
            reviewDate = itemView.findViewById(R.id.reviewDate);
            commentButton = itemView.findViewById(R.id.comment_button); // 댓글 버튼
            commentCount = itemView.findViewById(R.id.comment_count); // 댓글 수 표시 텍스트뷰
            likeCount = itemView.findViewById(R.id.like_count);//좋아요 수 텍스트뷰
            likeButton = itemView.findViewById(R.id.like_Button); //좋아요 버튼
            shareButton = itemView.findViewById(R.id.share_Button); // 공유 버튼
            feedImage = itemView.findViewById(R.id.feedImage); // 피드에 이미지가 있을경우 사용.
            profileImage = itemView.findViewById(R.id.profileImage);

            //해시태그 추가
            hashtagContent=itemView.findViewById(R.id.hashtagContent);//해시태그 표시

            //더보기
            readMore = itemView.findViewById(R.id.readMore);//더보기


            // 책 정보 뷰 초기화
            bookInfoLayout = itemView.findViewById(R.id.bookInfoLayout);
            bookTitle = bookInfoLayout.findViewById(R.id.bookInfoTitle);
            bookAuthor = bookInfoLayout.findViewById(R.id.bookInfoAuthor);
            bookPublisher = bookInfoLayout.findViewById(R.id.bookInfoPublisher);
            bookCover = bookInfoLayout.findViewById(R.id.bookCover);
            bookCategory = bookInfoLayout.findViewById(R.id.bookInfoCategory);


            /*voteLayout = itemView.findViewById(R.id.voteLayout);
            voteOption1 = itemView.findViewById(R.id.voteOption1);
            voteOption2 = itemView.findViewById(R.id.voteOption2);*/
        }
    }
    private void toggleLike(FeedItem item, FeedViewHolder holder) {
        // SharedPreferences에서 토큰 가져오기
        SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
        if (token == null) return;

        ApiService apiService = ApiClient.getClient(token).create(ApiService.class);

       // boolean newState = !item.isLiked(); // 토글
       // item.setLiked(newState);

        boolean oldState = item.isLikedByMe();   // ✅ 기존 상태
        int oldCount = item.getLikeCount();

        boolean newState = !oldState;
        item.setLikedByMe(newState);             // ✅ likedByMe 갱신

        // UI 즉시 반영 (optimistic update)
        holder.likeButton.setImageResource(
                newState ? R.drawable.feed_like_full : R.drawable.feed_like_button
        );
        int newCount = item.getLikeCount() + (newState ? 1 : -1);
        item.setLikeCount(newCount);
        holder.likeCount.setText(String.valueOf(newCount));

        // 서버 요청
        int reviewId = item.getPostId();

        Call<ApiResponse<Void>> call = newState
                ? apiService.likeReview("Bearer " + token, reviewId)   // 좋아요
                : apiService.unlikeReview("Bearer " + token, reviewId); // 좋아요 취소

        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> result = response.body();
                    if (!result.isSuccess()) {
                        // 서버에서 실패 응답 시 -> 롤백
                        rollbackLike(holder, item, !newState);
                    }
                } else {
                    // 서버 응답 실패 -> 롤백
                    rollbackLike(holder, item, !newState);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                // 네트워크/통신 실패 -> 롤백
                rollbackLike(holder, item, !newState);
            }
        });

    }

    private void rollbackLike(FeedViewHolder holder, FeedItem item,boolean correctState) {
        //item.setLiked(correctState);

        item.setLikedByMe(correctState);

        holder.likeButton.setImageResource(
                correctState ? R.drawable.feed_like_full : R.drawable.feed_like_button
        );

        int correctedCount = item.getLikeCount() + (correctState ? 1 : -1);
        item.setLikeCount(correctedCount);
        holder.likeCount.setText(String.valueOf(correctedCount));
    }


}
