package com.example.meltingbooks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.meltingbooks.utils.BookListHelper;
import com.example.meltingbooks.utils.BookListHelper.BookItem;

import java.util.ArrayList;
import java.util.List;


public class SetGoalFragment extends Fragment {

    public SetGoalFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_set_goal, container, false);

        TextView btnMonthly = view.findViewById(R.id.set_goal_monthly);
        TextView btnYearly = view.findViewById(R.id.set_goal_yearly);

        // 초기 상태 설정
        btnMonthly.setSelected(true);
        btnYearly.setSelected(false);

    // 클릭 리스너
        btnMonthly.setOnClickListener(v -> {
            btnMonthly.setSelected(true);
            btnYearly.setSelected(false);
            // 월간 클릭 시 처리
        });

        btnYearly.setOnClickListener(v -> {
            btnMonthly.setSelected(false);
            btnYearly.setSelected(true);
            // 연간 클릭 시 처리
        });

        // 책 리스트 UI 생성
        setupBooks(view);

        return view;
    }
    private List<BookListHelper.BookItem> bookItems = new ArrayList<>();

    private void setupBooks(View view) {
        LinearLayout container = view.findViewById(R.id.book_list_container);

        // 샘플 데이터
        bookItems.clear();
        bookItems.add(new BookListHelper.BookItem(R.drawable.book_image_1, false));
        bookItems.add(new BookListHelper.BookItem(R.drawable.book_image_2, true));
        bookItems.add(new BookListHelper.BookItem(R.drawable.book_image_3, false));

        BookListHelper.setupBooks(getContext(), container, bookItems, true);
    }
}
