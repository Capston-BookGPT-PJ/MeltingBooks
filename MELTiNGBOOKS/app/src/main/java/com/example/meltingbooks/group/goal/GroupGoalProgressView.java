package com.example.meltingbooks.group.goal;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.meltingbooks.R;
import com.example.meltingbooks.calendar.utils.ProgressBarUtil;
import com.example.meltingbooks.group.goal.GroupProgressBarUtil;

import java.util.Locale;

public class GroupGoalProgressView extends FrameLayout {

    private TextView tvTitle;
    private View vProgressFill, vProgressBackground;
    private TextView tvSubtext;

    private float currentPercent = 0f;
    private String unit = "권"; // 기본 단위 (권 / 개 / 시간)

    public GroupGoalProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_goal_progress, this, true);

        tvTitle = findViewById(R.id.tv_title);
        vProgressFill = findViewById(R.id.v_progress_fill);
        vProgressBackground = findViewById(R.id.fl_progress_frame);
        tvSubtext = findViewById(R.id.tv_subtext);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GroupGoalProgressView);
            String title = ta.getString(R.styleable.GroupGoalProgressView_groupGoalTitle);
            String subText = ta.getString(R.styleable.GroupGoalProgressView_groupGoalSubText);
            int progressDrawableResId = ta.getResourceId(R.styleable.GroupGoalProgressView_groupProgressDrawable, 0);
            String unitFromAttr = ta.getString(R.styleable.GroupGoalProgressView_groupGoalUnit);
            float titleTextSize = ta.getDimension(R.styleable.GroupGoalProgressView_groupGoalTitleTextSize, 15f);

            tvTitle.setText(title);
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);
            tvSubtext.setText(subText);

            if (progressDrawableResId != 0) {
                vProgressFill.setBackgroundResource(progressDrawableResId);
            }

            if (unitFromAttr != null) {
                unit = unitFromAttr;
            }

            ta.recycle();
        }
    }

    /** 단위 변경 (권 / 개 / 시간) **/
    public void setUnit(String unit) {
        if (unit != null && !unit.trim().isEmpty()) {
            this.unit = unit;
        }
    }

    public void setProgressWithGoal(float current, float goal, Float progressOverride, Integer progressHeightDp, Float titleTextSizePx) {
        if (goal <= 0) return;

        // 퍼센트 계산
        float percent = (progressOverride != null) ? progressOverride : (current / goal) * 100f;

        // 0~100 범위로 제한
        currentPercent = Math.max(0f, Math.min(percent, 100f));

        // 단위 표시
        String currentStr = ("권".equals(unit) || "개".equals(unit)) ? String.valueOf((int) current)
                : String.format(Locale.getDefault(), "%.1f", current);
        String goalStr = ("권".equals(unit) || "개".equals(unit)) ? String.valueOf((int) goal)
                : String.format(Locale.getDefault(), "%.1f", goal);
        tvSubtext.setText(currentStr + unit + " / " + goalStr + unit);

        // 제목 글씨 크기 적용
        if (titleTextSizePx != null) tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSizePx);

        post(() -> {
            int totalWidthPx = vProgressBackground.getWidth();
            if (totalWidthPx <= 0) return;

            int heightPx = (progressHeightDp != null) ? ProgressBarUtil.dpToPx(getContext(), progressHeightDp)
                    : ProgressBarUtil.dpToPx(getContext(), 15); // 기본 15dp

            // 배경 높이 설정
            ViewGroup.LayoutParams bgParams = vProgressBackground.getLayoutParams();
            bgParams.height = heightPx;
            vProgressBackground.setLayoutParams(bgParams);

            // 채움 높이 설정
            ViewGroup.LayoutParams fillParams = vProgressFill.getLayoutParams();
            fillParams.height = heightPx;

            // 로그
            android.util.Log.d("GroupGoalProgressView", "setProgressWithGoal: current=" + current
                    + ", goal=" + goal
                    + ", percent=" + currentPercent
                    + ", totalWidthPx=" + totalWidthPx
                    + ", heightPx=" + heightPx);

            // 실제 채움 적용
            ProgressBarUtil.setProgressBarWithPx(vProgressFill, currentPercent, totalWidthPx);
            vProgressFill.setLayoutParams(fillParams);
        });
    }



    public float getPercent() {
        return currentPercent;
    }

}
