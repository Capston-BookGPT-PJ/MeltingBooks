package com.example.meltingbooks.group;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;
import com.example.meltingbooks.network.group.feed.GroupFeedResponse;
import com.example.meltingbooks.profile.ProfileActivity;

import java.util.List;

public class LikedUsersAdapter extends RecyclerView.Adapter<LikedUsersAdapter.ViewHolder> {
    private final Context context;
    private final List<GroupFeedResponse.Post.LikedUser> likedUsers;

    public LikedUsersAdapter(Context context, List<GroupFeedResponse.Post.LikedUser> likedUsers) {
        this.context = context;
        this.likedUsers = likedUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ✅ 기존 profile_block.xml 재사용
        View view = LayoutInflater.from(context).inflate(R.layout.follow_users_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupFeedResponse.Post.LikedUser user = likedUsers.get(position);
        holder.userName.setText(user.getNickname());

        Glide.with(context)
                .load(user.getProfileImageUrl())
                .placeholder(R.drawable.sample_profile2)
                .error(R.drawable.sample_profile2)
                .circleCrop()
                .into(holder.userImage);

        // 클릭 시 프로필 이동
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("userId", user.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return likedUsers != null ? likedUsers.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        TextView userName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.userImage);
            userName = itemView.findViewById(R.id.userName);
        }
    }
}
