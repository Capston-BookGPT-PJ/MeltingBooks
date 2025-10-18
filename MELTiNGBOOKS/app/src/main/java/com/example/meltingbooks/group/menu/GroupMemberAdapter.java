package com.example.meltingbooks.group.menu;

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
import com.example.meltingbooks.profile.ProfileActivity;

import java.util.List;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {

    private List<GroupMemberItem> members;
    private int currentUserId;   // 로그인한 유저 ID
    private int groupOwnerId;    // 그룹장 ID

    public GroupMemberAdapter(List<GroupMemberItem> members, int currentUserId, int groupOwnerId) {
        this.members = members;
        this.currentUserId = currentUserId;
        this.groupOwnerId = groupOwnerId;
    }
    public interface OnDelegateClickListener {
        void onDelegateClick(GroupMemberItem member);
    }

    private OnDelegateClickListener delegateListener;

    public void setOnDelegateClickListener(OnDelegateClickListener listener) {
        this.delegateListener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_member_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupMemberItem member = members.get(position);
        holder.memberName.setText(member.getNickname());

        Glide.with(holder.itemView.getContext())
                .load(member.getProfileImageUrl())
                .circleCrop()
                .placeholder(R.drawable.sample_profile2)
                .into(holder.memberImage);

        // ✅ 표시 로직
        if (member.getUserId() == groupOwnerId) {
            holder.myself.setVisibility(View.VISIBLE);
            holder.groupCheckBoxImage.setVisibility(View.GONE);
        } else if (member.getUserId() == currentUserId) {
            holder.myself.setVisibility(View.GONE);
            holder.groupCheckBoxImage.setVisibility(View.GONE);
        } else {
            holder.myself.setVisibility(View.GONE);
            holder.groupCheckBoxImage.setVisibility(View.VISIBLE);

            // 🔹 그룹장만 체크박스 클릭 가능
            if (currentUserId == groupOwnerId) {
                holder.groupCheckBoxImage.setOnClickListener(v -> {
                    if (delegateListener != null) delegateListener.onDelegateClick(member);
                });
            } else {
                holder.groupCheckBoxImage.setOnClickListener(null);
            }
        }

        // 프로필 클릭
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProfileActivity.class);
            intent.putExtra("userId", member.getUserId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    // 서버에서 ownerId 받아온 후 호출
    public void setOwnerId(int ownerId) {
        this.groupOwnerId = ownerId;
        notifyDataSetChanged(); // 갱신
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView memberImage;
        ImageView myself;
        TextView memberName;
        ImageView groupCheckBoxImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            memberImage = itemView.findViewById(R.id.memberImage);
            myself = itemView.findViewById(R.id.myself);
            memberName = itemView.findViewById(R.id.memberName);
            groupCheckBoxImage = itemView.findViewById(R.id.groupCheckBoxImage);
        }
    }
}
