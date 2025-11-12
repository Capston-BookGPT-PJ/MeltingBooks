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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.feed.comment.CommentBottomSheet;
import com.example.meltingbooks.R;
import com.example.meltingbooks.feed.like.LikedUsersBottomSheet;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.book.Book;
import com.example.meltingbooks.network.book.BookController;
import com.example.meltingbooks.network.feed.FeedResponse;
import com.example.meltingbooks.network.recommend.RecommendBookResponse;
import com.example.meltingbooks.profile.ProfileActivity;

import java.util.List;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//피드 갱신용


public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private static final int VT_FEED = FeedItem.TYPE_FEED;
    private static final int VT_RECO = FeedItem.TYPE_RECOMMEND;
    private final List<FeedItem> feedList;
    private final Context context;
    private final ActivityResultLauncher<Intent> detailLauncher;


    //피드 갱신 추가
    public FeedAdapter(Context context, List<FeedItem> feedList, ActivityResultLauncher<Intent> detailLauncher) {
        this.context = context;
        this.feedList = feedList;
        setHasStableIds(true); // ✅ 안정적 재활용
        this.detailLauncher = detailLauncher;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VT_RECO) {
            View v = inflater.inflate(R.layout.item_recommend_books, parent, false);
            return new RecommendViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.feed_item, parent, false);
            return new FeedViewHolder(v);
        }
    }


    @Override
    public int getItemViewType(int position) {
        FeedItem item = feedList.get(position);
        return (item != null && item.getViewType() == FeedItem.TYPE_RECOMMEND)
                ? VT_RECO : VT_FEED;
    }


    @Override public long getItemId(int position) {
        return feedList.get(position).getStableId(); // ✅ 진짜 고정되는 값
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FeedItem item = feedList.get(position);


        if (holder instanceof RecommendViewHolder) {
            RecommendViewHolder vh = (RecommendViewHolder) holder;
            vh.title.setText("📚 당신을 위한 책 추천");

            vh.bookListContainer.removeAllViews();
            List<String> covers = item.getRecommendCovers();
            if (covers != null && !covers.isEmpty()) {

                // BookListHelper로 커버 리스트 세팅
                List<com.example.meltingbooks.calendar.utils.BookListHelper.BookItem> books = new ArrayList<>();
                for (String url : covers) books.add(new com.example.meltingbooks.calendar.utils.BookListHelper.BookItem(url, false));
                com.example.meltingbooks.calendar.utils.BookListHelper.setupBooks(context, vh.bookListContainer, books, false);

                // 📌 각 커버에 클릭 리스너 추가 (AlertDialog)
                for (int i = 0; i < vh.bookListContainer.getChildCount(); i++) {
                    View bookView = vh.bookListContainer.getChildAt(i);

                    // 클릭 이벤트 등록
                    int index = i;
                    bookView.setOnClickListener(v -> {
                        // 커버 이미지 URL에 대응하는 책 정보 찾기
                        if (item.getRecommendBooks() != null && index < item.getRecommendBooks().size()) {
                            RecommendBookResponse book = item.getRecommendBooks().get(index);

                            new androidx.appcompat.app.AlertDialog.Builder(context)
                                    .setTitle(book.getBookTitle())
                                    .setMessage("저자: " + book.getAuthor())
                                    .setPositiveButton("닫기", (dialog, which) -> dialog.dismiss())
                                    .show();
                        }
                    });
                }
            }
            return;
        }



        FeedViewHolder h = (FeedViewHolder) holder;

        h.userName.setText(item.getUserName());
        h.reviewContent.setText(item.getReviewContent());
        h.reviewDate.setText(item.getReviewDate());

        // 댓글/좋아요 수 연결
        h.commentCount.setText(String.valueOf(item.getCommentCount()));
        h.likeCount.setText(String.valueOf(item.getLikeCount()));

        // 좋아요 수 클릭 시
        h.likeCount.setOnClickListener(v -> {
            List<FeedResponse.LikedUser> likedUsers = item.getLikedUsers();
            if (likedUsers == null || likedUsers.isEmpty()) {
                Toast.makeText(context, "아직 좋아요한 사람이 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            LikedUsersBottomSheet likedSheet = LikedUsersBottomSheet.newInstance(likedUsers);
            likedSheet.show(
                    ((AppCompatActivity) v.getContext()).getSupportFragmentManager(),
                    "LikedUsersBottomSheet"
            );
        });


        // 댓글 버튼
        h.commentButton.setOnClickListener(v -> {
            CommentBottomSheet commentBottomSheet =
                    CommentBottomSheet.newInstance(item.getPostId(), "feed");

            commentBottomSheet.setOnCommentAddedListener(commentCount -> {
                h.commentCount.setText(String.valueOf(commentCount));
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
        h.likeButton.setImageResource(
                item.isLikedByMe() ? R.drawable.feed_like_full : R.drawable.feed_like_button
        );
        h.likeCount.setText(String.valueOf(item.getLikeCount()));


        h.likeButton.setOnClickListener(v -> {
            toggleLike(item, h);
        });

        //공유 버튼 클릭 리스너
        h.shareButton.setOnClickListener(v -> {

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
        List<String> images = item.getImageUrls();
        if (images != null && !images.isEmpty()) {
            h.feedImage.setVisibility(View.VISIBLE);
            final String latestImage = images.get(images.size() - 1);  // ✅ final 지정
            Glide.with(context).load(latestImage).into(h.feedImage);

            // ✅ 이미지 전체화면 프래그먼트로 표시
            h.feedImage.setOnClickListener(v -> {
                FullscreenImageFragment fragment =
                        FullscreenImageFragment.newInstance(latestImage);

                fragment.show(
                        ((AppCompatActivity) context).getSupportFragmentManager(),
                        "FullscreenImageFragment"
                );
            });

        } else {
            h.feedImage.setVisibility(View.GONE);
        }



        // 프로필 표시
        if (item.getProfileImageUrl() != null && !item.getProfileImageUrl().isEmpty()) {
            h.profileImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(item.getProfileImageUrl())
                    .placeholder(R.drawable.sample_profile) // 로딩 중 기본 이미지
                    .error(R.drawable.sample_profile)       // 실패 시 기본 이미지
                    .into(h.profileImage);
        } else {
            h.profileImage.setVisibility(View.VISIBLE); // GONE 대신 보이게
            h.profileImage.setImageResource(R.drawable.sample_profile); // 기본 이미지 적용
        }

        //⭐ 사용자 프로필 이동 추가
        View.OnClickListener profileClickListener = v -> {
            Intent intent = new Intent(v.getContext(), ProfileActivity.class);
            intent.putExtra("userId", item.getUserId());
            v.getContext().startActivity(intent);
        };

        //⭐ 프로필 이미지 클릭
        h.profileImage.setOnClickListener(profileClickListener);
        //⭐ 사용자 이름 클릭
        h.userName.setOnClickListener(profileClickListener);

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

                        h.bookInfoLayout.setVisibility(View.VISIBLE);
                        h.bookTitle.setText(book.getTitle());
                        h.bookAuthor.setText(book.getAuthor());
                        h.bookPublisher.setText(book.getPublisher());
                        h.bookCategory.setText(book.getCategoryName());

                        Glide.with(context).load(book.getCover()).into(h.bookCover);

                        // ✅✅✅ 알라딘 링크 이동 기능 추가
                        String aladinUrl = book.getLink();   // ⭐ book.link 사용

                        View.OnClickListener openAladin = v -> {
                            if (aladinUrl != null && !aladinUrl.isEmpty()) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(aladinUrl));
                                context.startActivity(intent);
                            } else {
                                Toast.makeText(context, "이 책의 링크가 제공되지 않았습니다.", Toast.LENGTH_SHORT).show();
                            }
                        };

                        // 책 정보 전체 클릭 가능
                        h.bookInfoLayout.setOnClickListener(openAladin);

                        // 책 표지 클릭해도 이동
                        h.bookCover.setOnClickListener(openAladin);

                    } else {
                        Log.e("BookDetail", "실패 코드: " + response.code());
                        h.bookInfoLayout.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<Book> call, Throwable t) {
                    Log.e("BookDetail", "에러: " + t.getMessage());
                    h.bookInfoLayout.setVisibility(View.GONE);
                }
            });
        } else {
            h.bookInfoLayout.setVisibility(View.GONE);
        }



        // ✅ 해시태그 표시
        List<String> hashtags = item.getHashtags();
        if (hashtags != null && !hashtags.isEmpty()) {
            h.hashtagContent.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();
            for (String tag : hashtags) {
                // 이미 #로 시작하면 그대로, 아니면 붙이기
                if (!tag.startsWith("#")) {
                    sb.append("#");
                }
                sb.append(tag).append(" ");
            }
            h.hashtagContent.setText(sb.toString().trim());
        } else {
            h.hashtagContent.setVisibility(View.GONE);
        }


        // ✅ 전체 카드 클릭 → 상세 이동
        h.root.setOnClickListener(v -> {
            Intent intent = new Intent(context, FeedDetailActivity.class);
            intent.putExtra("feedItem", item);
            detailLauncher.launch(intent);
        });

        // ✅ readMore 클릭 → 전체 클릭처럼 동작
        h.readMore.setOnClickListener(v -> h.root.performClick());

    }

    static class RecommendViewHolder extends RecyclerView.ViewHolder {
        LinearLayout bookListContainer;
        TextView title;
        RecommendViewHolder(View v) {
            super(v);
            bookListContainer = v.findViewById(R.id.book_list_container);
            title = v.findViewById(R.id.recommend_title);
        }
    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }

    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        View root; // ✅ 추가
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
            root = itemView.findViewById(R.id.feedItemRoot); // ✅ 루트 가져오기
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
