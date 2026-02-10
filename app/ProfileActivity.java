package com.example.meltingbooks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meltingbooks.utils.BookListHelper;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends BaseActivity  {
    //피드 리사이클
    private RecyclerView feedRecyclerView;
    private FeedAdapter feedAdapter;
    private List<FeedItem> feedList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //하단 메뉴
        setupBottomNavigation();

        //글 작성 화면으로 이동
        ImageButton goToUpload = findViewById(R.id.goTo_Profile_setting);

        goToUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, SettingProfile.class);
                startActivity(intent);
            }
        });

        //책 이미지 처리 리턴
        List<BookListHelper.BookItem> books = new ArrayList<>();

        books.add(new BookListHelper.BookItem(R.drawable.book_image_1, false));
        books.add(new BookListHelper.BookItem(R.drawable.book_image_2, false));
        books.add(new BookListHelper.BookItem(R.drawable.book_image_3, false));

        LinearLayout bookContainer = findViewById(R.id.book_list_container);
        BookListHelper.setupBooks(this, bookContainer, books, false);

        //피드 리사이클 뷰---------------------------------------------------
        feedRecyclerView = findViewById(R.id.feedRecyclerView);
        feedRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 더미 데이터 추가
        feedList = new ArrayList<>();

        // 나중에 서버 통신으로 바꿀시 Null 처리 어떻게 할 것인지 생각해보기.
        // 이미지 게시글
        feedList.add(new FeedItem("Alice", "이 고양이좀 봐~! 책읽고 있어", "2025-03-10", "https://i.imgur.com/iWf9Yuh.jpeg"));

        // 투표 게시글
            /*List<String> pollOptions = Arrays.asList("재미있다", "그저 그렇다", "별로다");x
            feedList.add(new FeedItem("Bob", "이번 책 어땠어?", "2025-03-09", null, pollOptions));*/

        //추후 서버 연결해서 바꾸는 식으로 조정.
        feedList.add(new FeedItem("Alice", "오늘 읽은 책 너무 재밌었어!------------------------------------------------------------------------------------------------------------------------------------------------------------------------", "2025-03-10", null));
        feedList.add(new FeedItem("Alice", "독서가 정말 힘이 되네.", "2025-03-09",null));

        feedAdapter = new FeedAdapter(this, feedList);
        feedRecyclerView.setAdapter(feedAdapter);
    }

    //bottom Navigation의 위치 설정
    @Override
    protected int getCurrentNavItemId() {
        return R.id.Profile;
    }
}