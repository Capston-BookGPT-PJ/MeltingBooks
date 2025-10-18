package com.example.meltingbooks.group.menu;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;
import com.example.meltingbooks.network.group.GroupApi;
import com.example.meltingbooks.network.group.comment.GroupCommonResponse;
import com.example.meltingbooks.network.group.GroupJoinRequestResponse;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupJoinRequestAdapter extends RecyclerView.Adapter<GroupJoinRequestAdapter.ViewHolder> {

    private List<GroupJoinRequestResponse.JoinRequest> requests;
    private GroupApi groupApi;
    private String token;
    private int groupId;
    private Context context;

    public GroupJoinRequestAdapter(Context context,
                                   List<GroupJoinRequestResponse.JoinRequest> requests,
                                   GroupApi groupApi,
                                   String token,
                                   int groupId) {
        this.context = context;
        this.requests = requests;
        this.groupApi = groupApi;
        this.token = token;
        this.groupId = groupId;
    }

    @NonNull
    @Override
    public GroupJoinRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_join_request_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupJoinRequestAdapter.ViewHolder holder, int position) {
        GroupJoinRequestResponse.JoinRequest request = requests.get(position);

        holder.memberName.setText(request.getNickname());

        Glide.with(context)
                .load(request.getProfileImageUrl())
                .circleCrop()
                .placeholder(R.drawable.sample_profile2)
                .into(holder.memberImage);

        // ✅ 승인 버튼 클릭
        holder.approvalBtn.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            GroupJoinRequestResponse.JoinRequest currentRequest = requests.get(currentPos);

            groupApi.acceptJoinRequest("Bearer " + token, groupId, currentRequest.getUserId())
                    .enqueue(new Callback<GroupCommonResponse>() {
                        @Override
                        public void onResponse(Call<GroupCommonResponse> call, Response<GroupCommonResponse> response) {
                            Log.d("JoinRequest", "승인 Response code: " + response.code());
                            if(response.errorBody() != null){
                                try {
                                    Log.d("JoinRequest", "승인 Error body: " + response.errorBody().string());
                                } catch (Exception e) { e.printStackTrace(); }
                            }

                            // ✅ body null이어도 성공으로 처리
                            if (response.isSuccessful()) {
                                Toast.makeText(context, currentRequest.getNickname() + " 승인 완료", Toast.LENGTH_SHORT).show();
                                requests.remove(holder.getAdapterPosition());
                                notifyItemRemoved(holder.getAdapterPosition());
                            } else {
                                Toast.makeText(context, "승인 실패", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<GroupCommonResponse> call, Throwable t) {
                            Log.e("JoinRequest", "승인 요청 실패", t);
                            Toast.makeText(context, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // ✅ 거절 버튼 클릭
        holder.rejectionBtn.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            GroupJoinRequestResponse.JoinRequest currentRequest = requests.get(currentPos);

            groupApi.rejectJoinRequest("Bearer " + token, groupId, currentRequest.getUserId())
                    .enqueue(new Callback<GroupCommonResponse>() {
                        @Override
                        public void onResponse(Call<GroupCommonResponse> call, Response<GroupCommonResponse> response) {
                            Log.d("JoinRequest", "거절 Response code: " + response.code());
                            if(response.errorBody() != null){
                                try { Log.d("JoinRequest", "거절 Error body: " + response.errorBody().string()); }
                                catch (Exception e) { e.printStackTrace(); }
                            }

                            // ✅ body null이어도 성공으로 처리
                            if (response.isSuccessful()) {
                                Toast.makeText(context, currentRequest.getNickname() + " 거절 완료", Toast.LENGTH_SHORT).show();
                                requests.remove(holder.getAdapterPosition());
                                notifyItemRemoved(holder.getAdapterPosition());
                            } else {
                                Toast.makeText(context, "거절 실패", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<GroupCommonResponse> call, Throwable t) {
                            Log.e("JoinRequest", "거절 요청 실패", t);
                            Toast.makeText(context, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView memberImage;
        TextView memberName;
        MaterialButton approvalBtn, rejectionBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            memberImage = itemView.findViewById(R.id.memberImage);
            memberName = itemView.findViewById(R.id.memberName);
            approvalBtn = itemView.findViewById(R.id.approval_btn);
            rejectionBtn = itemView.findViewById(R.id.rejection_btn);
        }
    }
}
