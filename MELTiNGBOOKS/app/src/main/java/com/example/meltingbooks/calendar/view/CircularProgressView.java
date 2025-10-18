package com.example.meltingbooks.calendar.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CircularProgressView extends View {

    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint progressBorderPaint;
    private Paint textPaint;
    private RectF rectF;
    private float progress = 0f; // 현재 표시되는 값
    private int strokeWidth = 23;

    public CircularProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // 배경 원
        //backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //backgroundPaint.setStyle(Paint.Style.STROKE);
        //backgroundPaint.setColor(0xCCCCCC);
        //backgroundPaint.setStrokeWidth(strokeWidth);


        // 진행 원 테두리
        progressBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressBorderPaint.setStyle(Paint.Style.STROKE);
        progressBorderPaint.setStrokeCap(Paint.Cap.ROUND);
        progressBorderPaint.setColor(0xFFCCCCCC); // 회색
        progressBorderPaint.setStrokeWidth(strokeWidth + 4); // 조금 더 두껍게

        // 진행 원
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(0xFFA9D4F5);
        progressPaint.setStrokeWidth(strokeWidth);

        // 중앙 퍼센트 텍스트
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF292D32);
        textPaint.setTextSize(50f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        rectF = new RectF();
    }

    /** 즉시 값 변경 */
    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    /** 애니메이션으로 값 변경 */
    public void setProgressWithAnimation(float targetProgress) {
        ValueAnimator animator = ValueAnimator.ofFloat(progress, targetProgress);
        animator.setDuration(800); // 0.8초
        animator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float padding = (strokeWidth + 4) / 2f; // 테두리 두께 고려
        rectF.set(padding, padding, getWidth() - padding, getHeight() - padding);

        float sweepAngle = (progress / 100f) * 360f;

        //테두리 그림
        canvas.drawArc(rectF, -90, sweepAngle, false, progressBorderPaint);

        //진행 원 그리기
        canvas.drawArc(rectF, -90, sweepAngle, false, progressPaint);

        //중앙 텍스트
        String percentText = String.format("%.0f%%", progress);
        float y = (getHeight() / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2);
        canvas.drawText(percentText, getWidth() / 2f, y, textPaint);
    }
}
