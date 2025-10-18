package com.example.meltingbooks.calendar.aichat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<String> chatList;

    public ChatAdapter(List<String> chatList) {
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        String message = chatList.get(position);
        holder.tvMessage.setText(message);

        // 간단한 구분: AI 메시지는 파란색, 사용자 메시지는 회색
        if (message.startsWith("AI:")) {
            holder.tvMessage.setBackgroundResource(R.drawable.chat_ai_bg);
            holder.tvMessage.setTextColor(0xFF000000);
        } else {
            holder.tvMessage.setBackgroundResource(R.drawable.chat_user_bg);
            holder.tvMessage.setTextColor(0xFF000000);
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }
}
