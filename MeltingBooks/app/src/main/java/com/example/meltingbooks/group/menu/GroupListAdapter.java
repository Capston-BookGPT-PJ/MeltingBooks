package com.example.meltingbooks.group.menu;


import android.app.Activity;
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
import com.example.meltingbooks.group.GroupFeedActivity;

import java.util.List;
import java.util.Set;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {

    private List<GroupListItem> groups;

    private int feedGroupId; // 현재 피드 그룹 ID

    public GroupListAdapter(List<GroupListItem> groups) {
        this.groups = groups;
    }

    public GroupListAdapter(List<GroupListItem> groups, int feedGroupId) {
        this.groups = groups;
        this.feedGroupId = feedGroupId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupListItem group = groups.get(position);

        holder.groupName.setText(group.getName());

        // 이미지 Glide 로딩 가능
        Glide.with(holder.itemView.getContext())
                .load(group.getImageUrl())
                .circleCrop() // 🔹 원형으로 만들기
                .placeholder(R.drawable.sample_profile2)
                .into(holder.groupImage);

        // ✅ 현재 피드 그룹과 일치하면 체크박스 보임
        if (group.getGroupId() == feedGroupId) {
            holder.groupCheckBoxImage.setVisibility(View.VISIBLE);
        } else {
            holder.groupCheckBoxImage.setVisibility(View.GONE);
        }

        // ✅ 아이템 클릭 시 GroupFeedActivity로 이동
        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, GroupFeedActivity.class);
            intent.putExtra("groupId", group.getGroupId());
            intent.putExtra("groupName", group.getName());
            context.startActivity(intent);

            // 현재 액티비티 종료 (Activity라면)
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        // 최대 3개까지만 표시
        return Math.min(groups.size(), 3);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView groupImage;
        TextView groupName;
        ImageView groupCheckBoxImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            groupImage = itemView.findViewById(R.id.groupImage);
            groupName = itemView.findViewById(R.id.groupName);
            groupCheckBoxImage = itemView.findViewById(R.id.groupCheckBoxImage);
        }
    }
}
