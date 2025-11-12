package com.example.meltingbooks.feed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;

public class FullscreenImageFragment extends DialogFragment {

    private static final String ARG_IMAGE_URL = "imageUrl";

    public static FullscreenImageFragment newInstance(String imageUrl) {
        FullscreenImageFragment fragment = new FullscreenImageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGE_URL, imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Fragment 를 전체화면 Dialog 로 만든다
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_fullscreen_image, container, false);

        ImageView imageView = view.findViewById(R.id.fullImage);

        String imageUrl = getArguments().getString(ARG_IMAGE_URL);

        // ✅ 절대 잘리지 않고 원본 비율 유지
        Glide.with(requireContext())
                .load(imageUrl)
                .fitCenter()
                .into(imageView);

        // ✅ 아무데나 클릭하면 닫힘
        view.setOnClickListener(v -> dismiss());

        return view;
    }
}
