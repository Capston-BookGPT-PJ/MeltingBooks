package com.example.meltingbooks.calendar.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.meltingbooks.R;
import com.example.meltingbooks.calendar.utils.ProgressBarUtil;

import java.util.Locale;

public class GoalProgressView extends FrameLayout {

    private TextView tvTitle;
    private View vProgressFill, vProgressBackground;
    private TextView tvSubtext;

    private float currentPercent = 0f;
    private String unit = "권"; // 기본 단위 (권 / 개 / 시간)

    public GoalProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_goal_progress, this, true);

        tvTitle = findViewById(R.id.tv_title);
        vProgressFill = findViewById(R.id.v_progress_fill);
        vProgressBackground = findViewById(R.id.fl_progress_frame); // 배경 추가
        tvSubtext = findViewById(R.id.tv_subtext);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GoalProgressView);
            String title = ta.getString(R.styleable.GoalProgressView_goalTitle);
            String subText = ta.getString(R.styleable.GoalProgressView_goalSubText);
            int progressDrawableResId = ta.getResourceId(R.styleable.GoalProgressView_progressDrawable, 0);
            String unitFromAttr = ta.getString(R.styleable.GoalProgressView_goalUnit);
            float titleTextSize = ta.getDimension(R.styleable.GoalProgressView_goalTitleTextSize, 15f); //제목 글씨 사이즈 설정

            tvTitle.setText(title);
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);
            tvSubtext.setText(subText);

            if (progressDrawableResId != 0) {
                vProgressFill.setBackgroundResource(progressDrawableResId);
            }

            if (unitFromAttr != null) {
                unit = unitFromAttr; // XML에서 지정 가능
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

    public void setProgressWithGoal(float current, int goal) {
        if (goal <= 0) return;

        currentPercent = (current / goal) * 100f;

        // 권, 개 → 소수점 없이 / 시간 → 소수점 1자리
        String currentStr;
        String goalStr;

        if ("권".equals(unit) || "개".equals(unit)) {
            currentStr = String.valueOf((int) current);
            goalStr = String.valueOf((int) goal);
        } else {
            currentStr = String.format(Locale.getDefault(), "%.1f", current);
            goalStr = String.format(Locale.getDefault(), "%.1f", goal);
        }

        String text = currentStr + unit + " / " +  goalStr + unit;
        tvSubtext.setText(text);

        post(() -> {
            int totalWidthPx = getWidth();
            ProgressBarUtil.setProgressBarWithPx(vProgressFill, currentPercent, totalWidthPx);

        });
    }

    public void setProgressWithGoal(float current, float goal) {
        if (goal <= 0) return;

        currentPercent = (current / goal) * 100f;

        // 권, 개 → 소수점 없이 / 시간 → 소수점 1자리
        String currentStr;
        String goalStr;

        if ("권".equals(unit) || "개".equals(unit)) {
            currentStr = String.valueOf((int) current);
            goalStr = String.valueOf((int) goal);
        } else {
            currentStr = String.format(Locale.getDefault(), "%.1f", current);
            goalStr = String.format(Locale.getDefault(), "%.1f", goal);
        }

        String text = currentStr + unit + " / " +  goalStr + unit;
        tvSubtext.setText(text);

        post(() -> {
            int totalWidthPx = getWidth();
            ProgressBarUtil.setProgressBarWithPx(vProgressFill, currentPercent, totalWidthPx);
        });
    }

    public void setProgressWithGoal(float current, int goal, float progress) {
        if (goal <= 0) return;

        currentPercent = progress;

        // 권, 개 → 소수점 없이 / 시간 → 소수점 1자리
        String currentStr;
        String goalStr;

        if ("권".equals(unit) || "개".equals(unit)) {
            currentStr = String.valueOf((int) current);
            goalStr = String.valueOf((int) goal);
        } else {
            currentStr = String.format(Locale.getDefault(), "%.1f", current);
            goalStr = String.format(Locale.getDefault(), "%.1f", goal);
        }

        String text = currentStr + unit + " / " +  goalStr + unit;
        tvSubtext.setText(text);

        post(() -> {
            int totalWidthPx = getWidth();
            ProgressBarUtil.setProgressBarWithPx(vProgressFill, currentPercent, totalWidthPx);
        });
    }

    public void setProgressWithGoal(float current, float goal, float progress) {
        if (goal <= 0) return;

        currentPercent = progress;

        // 권, 개 → 소수점 없이 / 시간 → 소수점 1자리
        String currentStr;
        String goalStr;

        if ("권".equals(unit) || "개".equals(unit)) {
            currentStr = String.valueOf((int) current);
            goalStr = String.valueOf((int) goal);
        } else {
            currentStr = String.format(Locale.getDefault(), "%.1f", current);
            goalStr = String.format(Locale.getDefault(), "%.1f", goal);
        }

        String text = currentStr + unit + " / " +  goalStr + unit;
        tvSubtext.setText(text);

        post(() -> {
            int totalWidthPx = getWidth();
            ProgressBarUtil.setProgressBarWithPx(vProgressFill, currentPercent, totalWidthPx);
        });
    }
    /**
     * 현재 진행률, 목표, 프로그레스바 높이, 제목 텍스트 크기(px) 설정
     */
    public void setProgressWithGoal(float current, float goal, int progressHeightDp, float titleTextSizePx) {
        if (goal <= 0) return;

        currentPercent = (current / goal) * 100f;

        // 권, 개 → 소수점 없이 / 시간 → 소수점 1자리
        String currentStr;
        String goalStr;
        String text;

        if ("권".equals(unit) || "개".equals(unit)) {
            currentStr = String.valueOf((int) current);
            goalStr = String.valueOf((int) goal);
        } else {
            currentStr = String.format(Locale.getDefault(), "%.1f", current);
            goalStr = String.format(Locale.getDefault(), "%.1f", goal);
        }

        text = currentStr + unit + " / " + goalStr + unit;
        tvSubtext.setText(text);

        // 제목 텍스트 크기 적용
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSizePx);

        post(() -> {
            int totalWidthPx = getWidth();
            int heightPx = ProgressBarUtil.dpToPx(getContext(), progressHeightDp); // dp → px 변환

            // 배경 높이 조정
            ViewGroup.LayoutParams bgParams = vProgressBackground.getLayoutParams();
            bgParams.height = heightPx;
            vProgressBackground.setLayoutParams(bgParams);

            // 채워진 부분 높이 조정
            ViewGroup.LayoutParams fillParams = vProgressFill.getLayoutParams();
            fillParams.height = heightPx;
            ProgressBarUtil.setProgressBarWithPx(vProgressFill, currentPercent, totalWidthPx);
            vProgressFill.setLayoutParams(fillParams);
        });
    }

    public void setProgressWithGoal(float current, float goal, int progressHeightDp, float titleTextSizePx, float progress) {
        if (goal <= 0) return;

        currentPercent = progress;

        // 권, 개 → 소수점 없이 / 시간 → 소수점 1자리
        String currentStr;
        String goalStr;
        String text;

        if ("권".equals(unit) || "개".equals(unit)) {
            currentStr = String.valueOf((int) current);
            goalStr = String.valueOf((int) goal);
        } else {
            currentStr = String.format(Locale.getDefault(), "%.1f", current);
            goalStr = String.format(Locale.getDefault(), "%.1f", goal);
        }

        text = currentStr + unit + " / " + goalStr + unit;
        tvSubtext.setText(text);


        // 제목 텍스트 크기 적용
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSizePx);

        post(() -> {
            int totalWidthPx = getWidth();
            int heightPx = ProgressBarUtil.dpToPx(getContext(), progressHeightDp); // dp → px 변환

            // 배경 높이 조정
            ViewGroup.LayoutParams bgParams = vProgressBackground.getLayoutParams();
            bgParams.height = heightPx;
            vProgressBackground.setLayoutParams(bgParams);

            // 채워진 부분 높이 조정
            ViewGroup.LayoutParams fillParams = vProgressFill.getLayoutParams();
            fillParams.height = heightPx;
            ProgressBarUtil.setProgressBarWithPx(vProgressFill, currentPercent, totalWidthPx);
            vProgressFill.setLayoutParams(fillParams);
        });
    }

    public float getPercent() {
        return currentPercent;
    }
}
