package com.example.meltingbooks.group.goal;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class GroupProgressBarUtil {

    /** dp 기반 가로형 프로그레스바 */
    public static void setProgressBar(Context context, View progressFillView, float progressPercent, int totalDp) {
        int totalWidthInPx = dpToPx(context, totalDp);
        int fillWidthInPx = Math.round(totalWidthInPx * progressPercent / 100f);

        ViewGroup.LayoutParams params = progressFillView.getLayoutParams();
        params.width = fillWidthInPx;
        progressFillView.setLayoutParams(params);
    }

    /** 픽셀 기반으로 바로 계산하는 함수 */
    public static void setProgressBarWithPx(View progressFillView, float progressPercent, int totalWidthInPx) {
        int fillWidthInPx = Math.round(totalWidthInPx * progressPercent / 100f);

        ViewGroup.LayoutParams params = progressFillView.getLayoutParams();
        params.width = fillWidthInPx;
        progressFillView.setLayoutParams(params);
    }

    public static void setProgressBarWithPx(View progressFillView, float progressPercent, float totalWidthInPx) {
        int fillWidthInPx = Math.round(totalWidthInPx * progressPercent / 100f);

        ViewGroup.LayoutParams params = progressFillView.getLayoutParams();
        params.width = fillWidthInPx;
        progressFillView.setLayoutParams(params);
    }

    /** dp → px 변환 */
    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
