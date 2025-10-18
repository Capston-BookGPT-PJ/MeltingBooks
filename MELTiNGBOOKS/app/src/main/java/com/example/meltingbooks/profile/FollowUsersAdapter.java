package com.example.meltingbooks.profile;

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
import com.example.meltingbooks.network.profile.FollowUser;

import java.util.List;

public class FollowUsersAdapter extends RecyclerView.Adapter<FollowUsersAdapter.ViewHolder> {

    private List<FollowUser> userList;

    public FollowUsersAdapter(List<FollowUser> userList) {
        this.userList = userList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        TextView userName;

        public ViewHolder(View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.userImage);
            userName = itemView.findViewById(R.id.userName);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.follow_users_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FollowUser user = userList.get(position);
        holder.userName.setText(user.getNickname());

        // Glide 사용, null 처리 및 기본 이미지 지정
        Glide.with(holder.itemView.getContext())
                .load(user.getProfileImageUrl()) // null도 처리 가능
                .placeholder(R.drawable.sample_profile2) // 로딩 중 기본 이미지
                .error(R.drawable.sample_profile2)       // 로드 실패 시 기본 이미지
                .circleCrop()                             // 원형 처리
                .into(holder.userImage);

        // 클릭 → 해당 유저 프로필로 이동
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProfileActivity.class);
            intent.putExtra("userId", user.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}