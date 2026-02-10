package com.example.meltingbooks.group;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.R;
import com.example.meltingbooks.group.LikedUsersAdapter;
import com.example.meltingbooks.network.group.feed.GroupFeedResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.Serializable;
import java.util.List;

public class LikedUsersBottomSheet extends BottomSheetDialogFragment {

    private List<GroupFeedResponse.Post.LikedUser> likedUsers;

    public static LikedUsersBottomSheet newInstance(List<GroupFeedResponse.Post.LikedUser> likedUsers) {
        LikedUsersBottomSheet fragment = new LikedUsersBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable("likedUsers", (Serializable) likedUsers);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            likedUsers = (List<GroupFeedResponse.Post.LikedUser>) getArguments().getSerializable("likedUsers");
        }
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        // ✅ 댓글창처럼 전체 화면 높이 확장
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(bottomSheet.getLayoutParams());
            }
        });

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        if (getDialog() != null && getDialog().getWindow() != null) {
            // 댓글창처럼 배경 dim 제거 (자연스러운 전환)
            getDialog().getWindow().setDimAmount(0f);
        }

        View view = inflater.inflate(R.layout.bottomsheet_liked_users, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.liked_users_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(new LikedUsersAdapter(getContext(), likedUsers));

        return view;
    }
}
