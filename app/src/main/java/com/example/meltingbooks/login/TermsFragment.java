package com.example.meltingbooks.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.meltingbooks.R;

public class TermsFragment extends Fragment {

    private Button agreeBtn;
    private Button cancelBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terms, container, false);

        agreeBtn = view.findViewById(R.id.agreeBtn);
        cancelBtn = view.findViewById(R.id.cancelBtn);

        // 동의 버튼 → LoginActivity 이동
        agreeBtn.setOnClickListener(v -> {
            // 동의 시 SocialSignupFragment로 전환
            ((SignupActivity)getActivity()).showFragment(new SocialSignupFragment());
        });

        // 취소 버튼 → 현재 액티비티 종료
        cancelBtn.setOnClickListener(v -> getActivity().finish());

        return view;
    }
}