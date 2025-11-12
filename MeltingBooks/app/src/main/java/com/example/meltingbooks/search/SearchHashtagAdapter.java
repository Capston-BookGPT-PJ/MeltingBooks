package com.example.meltingbooks.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.Hashtag;
import com.example.meltingbooks.R;

import java.util.List;

public class SearchHashtagAdapter extends RecyclerView.Adapter<SearchHashtagAdapter.ViewHolder> {

    private List<Hashtag> hashtagList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Hashtag hashtag);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public SearchHashtagAdapter(List<Hashtag> hashtagList) {
        this.hashtagList = hashtagList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView searchItemText;

        public ViewHolder(View itemView) {
            super(itemView);
            searchItemText = itemView.findViewById(R.id.searchItemText);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_hashtag_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hashtag hashtag = hashtagList.get(position);
        holder.searchItemText.setText(hashtag.getTag());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(hashtag);
        });
    }

    @Override
    public int getItemCount() {
        return hashtagList.size();
    }
}

