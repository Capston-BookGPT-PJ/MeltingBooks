package com.example.meltingbooks.browse;

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
import com.example.meltingbooks.network.browse.PopularUser;
import com.example.meltingbooks.profile.ProfileActivity;

import java.util.List;

public class BrowseUsersAdapter extends RecyclerView.Adapter<BrowseUsersAdapter.ViewHolder> {

    private List<PopularUser> userList;

    public BrowseUsersAdapter(List<PopularUser> userList) {
        this.userList = userList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        TextView userName, userIntro;

        public ViewHolder(View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.userImage);
            userName = itemView.findViewById(R.id.userName);
            userIntro = itemView.findViewById(R.id.userIntro);
        }
    }

    @NonNull
    @Override
    public BrowseUsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.browse_users_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BrowseUsersAdapter.ViewHolder holder, int position) {
        PopularUser user = userList.get(position);
        holder.userName.setText(user.getNickname());
        holder.userIntro.setText(user.getBio() != null ? user.getBio() : "");

        if (user.getProfileImageUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.sample_profile2)
                    .into(holder.userImage);
        } else {
            holder.userImage.setImageResource(R.drawable.sample_profile2);
        }

        holder.userImage.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProfileActivity.class);
            intent.putExtra("userId", user.getId());
            v.getContext().startActivity(intent);
        });

    }

    public void updateUsers(List<PopularUser> newUsers) {
        this.userList.clear();
        this.userList.addAll(newUsers);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
