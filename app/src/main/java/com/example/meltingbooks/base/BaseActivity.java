package com.example.meltingbooks.base;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.meltingbooks.R;
import com.example.meltingbooks.browse.BrowseActivity;
import com.example.meltingbooks.calendar.CalendarActivity;
import com.example.meltingbooks.feed.FeedActivity;
import com.example.meltingbooks.group.GroupActivity;
import com.example.meltingbooks.group.GroupFeedActivity;
import com.example.meltingbooks.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;
    protected ImageView gradientCircle;
    protected int[] iconPositions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureTransparentStatusBar();
    }

    protected void setupBottomNavigation() {
        //하단 메뉴 애니메이션-------
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        gradientCircle = findViewById(R.id.gradientCircle);

        // 기본 애니메이션 제거 (Shift Mode 제거)
        bottomNavigationView.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_UNLABELED);

        // 클릭 이벤트 설정
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int position = getSelectedPosition(item.getItemId());
            if (position >= 0 && iconPositions != null) {
                gradientCircle.setX(iconPositions[position]);
            }

            int itemId = item.getItemId();
            if (itemId == R.id.Feed && !(this instanceof FeedActivity)) {
                startActivity(new Intent(this, FeedActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.Browser && !(this instanceof BrowseActivity)) {
                startActivity(new Intent(this, BrowseActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.Calendar && !(this instanceof CalendarActivity)) {
                startActivity(new Intent(this, CalendarActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.Group) {
                // 그룹 관련 화면이면 아무 이동도 하지 않음
                if (!(this instanceof GroupActivity || this instanceof GroupFeedActivity)) {
                    startActivity(new Intent(this, GroupActivity.class));
                    overridePendingTransition(0, 0);
                }
                return true; // 클릭은 처리 완료


            } else if (itemId == R.id.Profile && !(this instanceof ProfileActivity)) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        bottomNavigationView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int count = bottomNavigationView.getMenu().size();
            iconPositions = new int[count];

            for (int i = 0; i < count; i++) {
                View itemView = bottomNavigationView.findViewById(
                        bottomNavigationView.getMenu().getItem(i).getItemId()
                );

                // 아이콘 중심 X좌표
                float iconCenterX = itemView.getX() + (itemView.getWidth() / 2f);
                // gradientCircle의 중심을 아이콘 중심에 맞춤 (X)
                float circleX = iconCenterX - (gradientCircle.getWidth() / 2f);

                iconPositions[i] = Math.round(circleX);

                // ---- Y 좌표 계산 ----
                // 아이콘 뷰 자체의 높이 안에서 아이콘이 실제 어디에 있는지를 찾아야 함
                ImageView iconView = itemView.findViewById(com.google.android.material.R.id.icon);

                if (iconView != null) {
                    float iconCenterY = itemView.getY() + iconView.getY() + (iconView.getHeight() / 2f);
                    float circleY = iconCenterY - (gradientCircle.getHeight() / 2f);
                    gradientCircle.setY(circleY);
                }
            }

            // 현재 선택된 메뉴 위치 이동
            int navId = getCurrentNavItemId();
            if (navId != -1) {
                int pos = getSelectedPosition(navId);
                if (pos >= 0 && pos < iconPositions.length) {
                    gradientCircle.setX(iconPositions[pos]);
                    bottomNavigationView.setSelectedItemId(navId);
                }
            }
        });
    }

    // 메뉴 아이디 → gradientCircle 위치 인덱스
    private int getSelectedPosition(int itemId) {
        if (itemId == R.id.Feed) return 0;
        else if (itemId == R.id.Browser) return 1;
        else if (itemId == R.id.Calendar) return 2;
        else if (itemId == R.id.Group) return 3;
        else if (itemId == R.id.Profile) return 4;
        else return -1;
    }

    // 상태바 디자인 설정
    protected void configureTransparentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();

            // 상태바를 완전히 투명하게 만들어서 배경이 보이도록 설정
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.TRANSPARENT);

            // 레이아웃이 상태바 영역까지 확장되도록 설정
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

            // 상태바 아이콘 & 글자를 검정색으로 변경
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            window.getDecorView().setSystemUiVisibility(flags);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView != null && gradientCircle != null && iconPositions != null && iconPositions.length > 0) {
            int navId = getCurrentNavItemId();
            if (navId != -1) {
                int pos = getSelectedPosition(navId);
                if (pos >= 0 && pos < iconPositions.length) {
                    gradientCircle.setX(iconPositions[pos]);
                    bottomNavigationView.setSelectedItemId(navId);
                }
            }
        }
    }

    //뒤로가기 시 화면 전환 모션 삭제
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    // 하위 액티비티에서 오버라이드
    protected abstract int getCurrentNavItemId();
}