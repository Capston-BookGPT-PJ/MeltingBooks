package com.example.meltingbooks.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

import com.example.meltingbooks.R;

public class SocialSignupFragment extends Fragment {

    private ImageButton kakaoLoginBtn, googleLoginBtn, naverLoginBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_social_signup, container, false);

        kakaoLoginBtn = view.findViewById(R.id.kakaoLoginBtn);
        googleLoginBtn = view.findViewById(R.id.googleLoginBtn);
        naverLoginBtn = view.findViewById(R.id.naverLoginBtn);

        kakaoLoginBtn.setOnClickListener(v -> {
            String loginUrl = "http://meltingbooks.o-r.kr:8080/auth/KAKAO";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(loginUrl));
            startActivity(intent);
            getActivity().finish();
        });

        googleLoginBtn.setOnClickListener(v -> {
            String loginUrl = "http://meltingbooks.o-r.kr:8080/auth/GOOGLE";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(loginUrl));
            startActivity(intent);
            getActivity().finish();
        });

        naverLoginBtn.setOnClickListener(v -> {
            String loginUrl = "http://meltingbooks.o-r.kr:8080/auth/NAVER";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(loginUrl));
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }
}