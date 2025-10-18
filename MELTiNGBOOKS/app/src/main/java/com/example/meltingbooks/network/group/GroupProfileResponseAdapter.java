package com.example.meltingbooks.network.group;

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

import java.util.List;

public class GroupProfileResponseAdapter extends RecyclerView.Adapter<GroupProfileResponseAdapter.GroupViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(GroupProfileResponse groupResponse);
    }

    private Context context;
    private List<GroupProfileResponse> groupList;
    private OnItemClickListener listener;

    public GroupProfileResponseAdapter(Context context, List<GroupProfileResponse> groupList, OnItemClickListener listener) {
        this.context = context;
        this.groupList = groupList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.group_card_item, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        GroupProfileResponse group = groupList.get(position);
        holder.bind(group, listener);
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        ImageView groupImage;
        TextView groupName, groupDescription;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupImage = itemView.findViewById(R.id.groupImage);
            groupName = itemView.findViewById(R.id.groupName);
            groupDescription = itemView.findViewById(R.id.groupIntro);
        }

        public void bind(final GroupProfileResponse group, final OnItemClickListener listener) {
            groupName.setText(group.getName());
            groupDescription.setText(group.getDescription());

            String imageUrl = group.getGroupImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(groupImage.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.sample_profile2)
                        .error(R.drawable.sample_profile2)
                        .circleCrop()
                        .into(groupImage);
            } else {
                Glide.with(groupImage.getContext())
                        .load(R.drawable.sample_profile2)
                        .circleCrop()
                        .into(groupImage);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(group));
        }
    }

    // 데이터 갱신
    public void updateData(List<GroupProfileResponse> newGroupList) {
        groupList.clear();
        groupList.addAll(newGroupList);
        notifyDataSetChanged();
    }

    // 새로운 그룹 추가
    public void addItem(GroupProfileResponse group) {
        groupList.add(0, group); // 맨 앞에 추가
        notifyItemInserted(0);
    }
}
