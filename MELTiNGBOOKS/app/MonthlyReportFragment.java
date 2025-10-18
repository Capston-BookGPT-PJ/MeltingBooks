package com.example.meltingbooks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.meltingbooks.utils.BookListHelper;
import com.example.meltingbooks.utils.ProgressBarUtil;

import java.util.ArrayList;
import java.util.List;

public class MonthlyReportFragment extends Fragment {
    public MonthlyReportFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_monthly_report, container, false);

        // FrameLayout 참조 (부모 뷰)

        FrameLayout goal1ProgressFrame = view.findViewById(R.id.m_goal_1_progressbar_frame);
        View goal1ProgressFill = view.findViewById(R.id.m_goal1_progressbar_fill);
        int goal1progress = 80;

        goal1ProgressFrame.post(() -> {
            int totalWidthInPx = goal1ProgressFrame.getWidth();
            ProgressBarUtil.setProgressBarWithPx(goal1ProgressFill, goal1progress, totalWidthInPx);
        });


        // goal2에 해당하는 progressbar fill View
        FrameLayout goal2ProgressFrame = view.findViewById(R.id.m_goal_2_progressbar_frame);
        View goal2ProgressFill = view.findViewById(R.id.m_goal2_progressbar_fill);
        int goal2progress = 33;
        goal1ProgressFrame.post(() -> {
            int totalWidthInPx = goal2ProgressFrame.getWidth();
            ProgressBarUtil.setProgressBarWithPx(goal2ProgressFill, goal2progress, totalWidthInPx);
        });

        // goal3에 해당하는 progressbar fill View
        FrameLayout goal3ProgressFrame = view.findViewById(R.id.m_goal_3_progressbar_frame);
        View goal3ProgressFill = view.findViewById(R.id.m_goal3_progressbar_fill);
        int goal3progress = 33;
        goal1ProgressFrame.post(() -> {
            int totalWidthInPx = goal3ProgressFrame.getWidth();
            ProgressBarUtil.setProgressBarWithPx(goal3ProgressFill, goal3progress, totalWidthInPx);
        });

        // 전체 목표 평균 progress 계산
        int totalProgress = (goal1progress + goal2progress + goal3progress) / 3;

        // 전체 progress 적용
        View goalTotalProgressFill = view.findViewById(R.id.goal_total_progressbar_fill);
        ProgressBarUtil.setProgressBar(requireContext(), goalTotalProgressFill, totalProgress, 300);

        // TextView에 표시
        TextView totalPercentageTextView = view.findViewById(R.id.total_percentage);
        totalPercentageTextView.setText(totalProgress + "%");


        //책 이미지 처리 리턴
        List<BookListHelper.BookItem> books = new ArrayList<>();

        books.add(new BookListHelper.BookItem(R.drawable.book_image_1, true));
        books.add(new BookListHelper.BookItem(R.drawable.book_image_2, true));
        books.add(new BookListHelper.BookItem(R.drawable.book_image_3, true));

        LinearLayout bookContainer = view.findViewById(R.id.book_list_container);
        BookListHelper.setupBooks(getContext(), bookContainer, books, false);

        return view;
    }
}
