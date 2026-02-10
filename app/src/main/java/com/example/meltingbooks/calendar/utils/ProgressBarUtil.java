/**package com.example.meltingbooks.calendar.utils;

 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 public class ProgressBarUtil {

 // 기존 dp 기반 함수
 public static void setProgressBar(Context context, View progressFillView, int progressPercent, int totalDp) {
 int totalWidthInPx = dpToPx(context, totalDp);
 int fillWidthInPx = totalWidthInPx * progressPercent / 100;

 ViewGroup.LayoutParams params = progressFillView.getLayoutParams();
 params.width = fillWidthInPx;
 progressFillView.setLayoutParams(params);
 }

 // 픽셀 기반으로 바로 계산하는 함수
 public static void setProgressBarWithPx(View progressFillView, int progressPercent, int totalWidthInPx) {
 int fillWidthInPx = totalWidthInPx * progressPercent / 100;

 ViewGroup.LayoutParams params = progressFillView.getLayoutParams();
 params.width = fillWidthInPx;
 progressFillView.setLayoutParams(params);
 }

 private static int dpToPx(Context context, int dp) {
 float density = context.getResources().getDisplayMetrics().density;
 return Math.round(dp * density);
 }
 }*/

package com.example.meltingbooks.calendar.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.example.meltingbooks.calendar.view.CircularProgressView;

public class ProgressBarUtil {

    // 기존 dp 기반 함수, 가로형 프로그레스바
    public static void setProgressBar(Context context, View progressFillView, float progressPercent, int totalDp) {
        int totalWidthInPx = dpToPx(context, totalDp);
        int fillWidthInPx = Math.round(totalWidthInPx * progressPercent / 100f);

        ViewGroup.LayoutParams params = progressFillView.getLayoutParams();
        params.width = fillWidthInPx;
        progressFillView.setLayoutParams(params);
    }

    // 픽셀 기반으로 바로 계산하는 함수
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

    // 원형 그래프용 (Custom View)
    public static void setCircularProgress(CircularProgressView progressView, float progressPercent) {
        if (progressView != null) {
            progressView.setProgress(Math.round(progressPercent));
        }
    }

    public  static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
