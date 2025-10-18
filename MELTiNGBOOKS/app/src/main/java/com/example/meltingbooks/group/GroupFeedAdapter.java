package com.example.meltingbooks.group;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;
import com.example.meltingbooks.group.comment.GroupCommentBottomSheet;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.group.GroupApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupFeedAdapter extends RecyclerView.Adapter<GroupFeedAdapter.GroupFeedViewHolder> {

    private final List<GroupFeedItem> feedList;
    private final Context context;
    private final int groupId;
    private final ActivityResultLauncher<Intent> detailLauncher;


    public GroupFeedAdapter(Context context, List<GroupFeedItem> feedList, int groupId, ActivityResultLauncher<Intent> detailLauncher) {
        this.context = context;
        this.feedList = feedList;
        this.groupId = groupId;
        this.detailLauncher = detailLauncher;
    }

    @NonNull
    @Override
    public GroupFeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_feed_item, parent, false);
        return new GroupFeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupFeedViewHolder holder, int position) {
        GroupFeedItem item = feedList.get(position);

        // 작성자 이름, 작성일, 본문 표시
        holder.userName.setText(item.getUserName());
        holder.writeDate.setText(item.getCreatedAt());
        holder.title.setText(item.getTitle());
        holder.content.setText(item.getContent());


        // 댓글 수 표시
        holder.commentCount.setText(String.valueOf(item.getCommentCount()));
        holder.likeCount.setText(String.valueOf(item.getLikeCount()));

        // 댓글 버튼(현재 feed용 나중에 group용으로 수정 필요)
        holder.commentButton.setOnClickListener(v -> {

            GroupCommentBottomSheet groupCommentBottomSheet =
                    GroupCommentBottomSheet.newInstance(groupId, item.getPostId(), "group");

            groupCommentBottomSheet.setOnCommentAddedListener(commentCount -> {
                holder.commentCount.setText(String.valueOf(commentCount));
            });

            groupCommentBottomSheet.show(
                    ((AppCompatActivity)v.getContext()).getSupportFragmentManager(),
                    "GroupCommentBottomSheet"
            );
        });

        // 초기 상태 (좋아요 여부에 따라 이미지 설정)
        holder.likeButton.setImageResource(
                item.isLikedByMe() ? R.drawable.feed_like_full : R.drawable.feed_like_button
        );
        holder.likeCount.setText(String.valueOf(item.getLikeCount()));


        holder.likeButton.setOnClickListener(v -> {
            toggleLike(item, holder);
        });


        // 이미지 표시
        if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
            holder.groupImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(item.getImageUrls().get(0)) // 첫 번째 이미지 사용
                    .centerCrop()
                    .into(holder.groupImage);
        } else {
            holder.groupImage.setVisibility(View.GONE);
        }


        // 프로필 표시
        if (item.getUserProfileImage() != null && !item.getUserProfileImage().isEmpty()) {
            holder.profileImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(item.getUserProfileImage())
                    .placeholder(R.drawable.sample_profile) // 로딩 중 기본 이미지
                    .error(R.drawable.sample_profile)       // 실패 시 기본 이미지
                    .into(holder.profileImage);
        } else {
            holder.profileImage.setVisibility(View.VISIBLE); // GONE 대신 보이게
            holder.profileImage.setImageResource(R.drawable.sample_profile2); // 기본 이미지 적용
        }

        // 더보기 클릭
        holder.readMore.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, GroupDetailActivity.class);
            intent.putExtra("groupFeedItem", item); // groupFeedItem 전달
            intent.putExtra("groupId", item.getGroupId());  // groupId 전달
            intent.putExtra("postId", item.getPostId());    // ✅ postId 추가
            context.startActivity(intent);
            // 로그로 확인
            Log.d("ReadMoreClick", "groupFeedItem postId=" + item.getPostId());
            // 로그 확인
            Log.d("ReadMoreClick", "groupFeedItem: " + item.toString());
            Log.d("ReadMoreClick", "groupId: " + item.getGroupId());
            Log.d("ReadMoreClick", "postId: " + item.getPostId()); // ✅ 로그 추가
        });


    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }

    static class GroupFeedViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage, groupImage, commentButton, likeButton;
        TextView userName, writeDate, title, content, commentCount, likeCount;
        TextView readMore;

        public GroupFeedViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            groupImage = itemView.findViewById(R.id.groupImage);
            userName = itemView.findViewById(R.id.userName);
            writeDate = itemView.findViewById(R.id.groupWriteDate);
            title = itemView.findViewById(R.id.groupWriteTitle);
            content = itemView.findViewById(R.id.groupWriteContent);
            commentButton = itemView.findViewById(R.id.chat_button);
            commentCount = itemView.findViewById(R.id.chat_count);

            likeButton = itemView.findViewById(R.id.like_button);
            likeCount = itemView.findViewById(R.id.like_count);

            //더보기
            readMore = itemView.findViewById(R.id.readMore);//더보기

        }
    }

    private void toggleLike(GroupFeedItem item, GroupFeedAdapter.GroupFeedViewHolder holder) {
        // SharedPreferences에서 토큰 가져오기
        SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt", null);
        if (token == null) return;

        ApiService apiService = ApiClient.getClient(token).create(ApiService.class);
        GroupApi groupApi = ApiClient.getClient(token).create(GroupApi.class);

        // 서버 요청
        int groupId = item.getGroupId();   // ✅ 그룹 ID 가져오기
        int postId = item.getPostId();

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


        Call<ApiResponse<Void>> call = newState
                ? groupApi.likePost("Bearer " + token, groupId, postId)   // 좋아요
                : groupApi.unlikePost("Bearer " + token, groupId, postId); // 좋아요 취소

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

    private void rollbackLike(GroupFeedAdapter.GroupFeedViewHolder holder, GroupFeedItem item, boolean correctState) {
        item.setLikedByMe(correctState);

        holder.likeButton.setImageResource(
                correctState ? R.drawable.feed_like_full : R.drawable.feed_like_button
        );

        int correctedCount = item.getLikeCount() + (correctState ? 1 : -1);
        item.setLikeCount(correctedCount);
        holder.likeCount.setText(String.valueOf(correctedCount));
    }

}
