package com.example.meltingbooks.browse;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.example.meltingbooks.network.book.Book;
import com.example.meltingbooks.R;

import java.util.List;


public class BrowseBookAdapter extends RecyclerView.Adapter<BrowseBookAdapter.BookViewHolder> {

    private List<Book> bookList;
    private Context context;

    public BrowseBookAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    public static class BookViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCoverImage;

        public BookViewHolder(View itemView) {
            super(itemView);
            bookCoverImage = itemView.findViewById(R.id.bookCoverImage);
        }
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.browse_book_item, parent, false);
        return new BookViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Glide.with(context).clear(holder.bookCoverImage);
        Book book = bookList.get(position);
        int widthDp = 340;
        int heightDp = 470;
        int cornerRadiusDp = 16; // 둥근 모서리 반경

        int widthPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, widthDp, context.getResources().getDisplayMetrics());
        int heightPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, heightDp, context.getResources().getDisplayMetrics());
        int cornerRadiusPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, cornerRadiusDp, context.getResources().getDisplayMetrics());

        // ImageView 크기 강제 적용
        ViewGroup.LayoutParams params = holder.bookCoverImage.getLayoutParams();
        params.width = widthPx;
        params.height = heightPx;
        holder.bookCoverImage.setLayoutParams(params);

        /*// Glide 옵션
        RequestOptions requestOptions = new RequestOptions()
                .override(widthPx, heightPx)
                .transform(new CenterCrop(), new RoundedCorners(cornerRadiusPx));

        Glide.with(context)
                .load(book.getCover() != null && !book.getCover().isEmpty()
                        ? book.getCover() : R.drawable.book_image_1)
                .apply(requestOptions)
                .format(DecodeFormat.PREFER_ARGB_8888)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.bookCoverImage);*/
        RequestOptions requestOptions = new RequestOptions()
                .override(widthPx, heightPx)
                .transform(new FitCenter(), new RoundedCorners(cornerRadiusPx));

        if (book.getCover() == null || book.getCover().isEmpty()) {
            // 로컬 기본 이미지
            Glide.with(context)
                    .load(R.drawable.book_image_1)
                    .apply(requestOptions)
                    .into(holder.bookCoverImage);
        } else {
            // 외부 URL 안전 로딩
            GlideUrl glideUrl = new GlideUrl(book.getCover(),
                    new LazyHeaders.Builder()
                            .addHeader("User-Agent", "Mozilla/5.0")
                            .build());

            Glide.with(context)
                    .load(glideUrl)
                    .apply(requestOptions)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .into(holder.bookCoverImage);
        }
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }
}