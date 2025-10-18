package com.example.meltingbooks.calendar;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.meltingbooks.R;
import com.example.meltingbooks.calendar.utils.BookListHelper;
import com.example.meltingbooks.calendar.utils.ProgressBarUtil;
import com.example.meltingbooks.calendar.view.CircularProgressView;
import com.example.meltingbooks.calendar.view.GoalProgressView;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.goal.GoalApi;
import com.example.meltingbooks.network.goal.GoalController;
import com.example.meltingbooks.network.goal.GoalResponse;
import com.example.meltingbooks.network.report.ReportApi;
import com.example.meltingbooks.network.report.ReportController;
import com.example.meltingbooks.network.report.ReportResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class MonthlyReportFragment extends Fragment {
    public MonthlyReportFragment() { }

    private String token;
    private int userId;
    private GoalController goalController;
    private ReportController reportController;
    private TextView textMonth, title;
    private Calendar currentCalendar;
    private String selectedGoalType = "MONTHLY"; // 초기값 월간

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monthly_report, container, false);

        title = view.findViewById(R.id.monthly_report_title);
        textMonth = view.findViewById(R.id.textMonth);
        currentCalendar = Calendar.getInstance();

        // SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("jwt", null);
        userId = prefs.getInt("userId", -1);

        GoalApi apiService = ApiClient.getClient(token).create(GoalApi.class);
        goalController = new GoalController(apiService);

        ReportApi reportService = ApiClient.getClient(token).create(ReportApi.class);
        reportController = new ReportController(reportService);

        TextView btnMonthly = view.findViewById(R.id.set_goal_monthly);
        TextView btnYearly = view.findViewById(R.id.set_goal_yearly);

        // 이전 달
        view.findViewById(R.id.btnPrev).setOnClickListener(v -> {
            if ("MONTHLY".equals(selectedGoalType)) {
                currentCalendar.add(Calendar.MONTH, -1);
            } else {
                currentCalendar.add(Calendar.YEAR, -1);
            }
            updateDateText();
            loadGoal(selectedGoalType);
            loadReport(selectedGoalType);
        });

        // 다음 달
        view.findViewById(R.id.btnNext).setOnClickListener(v -> {
            if ("MONTHLY".equals(selectedGoalType)) {
                currentCalendar.add(Calendar.MONTH, 1);
            } else {
                currentCalendar.add(Calendar.YEAR, 1);
            }
            updateDateText();
            loadGoal(selectedGoalType);
            loadReport(selectedGoalType);
        });

        btnMonthly.setOnClickListener(v -> {
            selectedGoalType = "MONTHLY";
            btnMonthly.setSelected(true);
            btnYearly.setSelected(false);
            updateDateText();
            loadGoal("MONTHLY");
            loadReport("MONTHLY");
        });

        btnYearly.setOnClickListener(v -> {
            selectedGoalType = "YEARLY";
            btnMonthly.setSelected(false);
            btnYearly.setSelected(true);
            updateDateText();
            loadGoal("YEARLY");
            loadReport("YEARLY");
        });

        // 초기값: 월간 불러오기
        btnMonthly.setSelected(true);
        btnYearly.setSelected(false);
        updateDateText();
        loadGoal("MONTHLY");
        loadReport("MONTHLY");

        return view;
    }

    private void loadReport(String type) {
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH) + 1;

        if ("MONTHLY".equals(type)) {
            reportController.getMonthlyReport(token, year, month, new ReportController.ReportCallback<ReportResponse>() {
                @Override
                public void onSuccess(ReportResponse data) {
                    if (data == null) {
                        clearReportUI();
                        Toast.makeText(getContext(), "리포트 기록이 없습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        bindReportData(data);
                    }
                }
                @Override
                public void onError(String message) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    clearReportUI();
                    Toast.makeText(getContext(), "리포트 기록이 없습니다.", Toast.LENGTH_SHORT).show();
                    Log.e("DetailGoalFragment", "loadReport error: " + message);
                }
            });
        } else {
            reportController.getYearlyReport(token, year, new ReportController.ReportCallback<ReportResponse>() {
                @Override
                public void onSuccess(ReportResponse data) {
                    if (data == null) {
                        clearReportUI();
                        Toast.makeText(getContext(), "리포트 기록이 없습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        bindReportData(data);
                    }
                }
                @Override
                public void onError(String message) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    clearReportUI();
                    Toast.makeText(getContext(), "리포트 기록이 없습니다.", Toast.LENGTH_SHORT).show();
                    Log.e("DetailGoalFragment", "loadReport error: " + message);
                }
            });
        }
    }

    private void bindReportData(ReportResponse report) {
        TextView totalPages = getView().findViewById(R.id.totalPagesNumber);
        TextView totalMinutes = getView().findViewById(R.id.totalMinutesNumber);
        TextView completedReviews = getView().findViewById(R.id.completedReviewsNumber);
        TextView avgDaily = getView().findViewById(R.id.averageDailyMinutesNumber);
        TextView experience = getView().findViewById(R.id.experienceNumber);
        TextView level = getView().findViewById(R.id.levelNumber);

        totalPages.setText(report.getTotalPages() + "P");
        totalMinutes.setText(report.getTotalMinutes() + "분");
        completedReviews.setText(report.getCompletedReviews() + "개");
        avgDaily.setText(report.getAverageDailyMinutes() + "분");
        experience.setText(report.getExperience() + "Point");
        level.setText(report.getLevel() + ".Lv");

        // 배지 표시
        LinearLayout badgeContainer = getView().findViewById(R.id.badge_container);
        badgeContainer.removeAllViews();

        if (report.getBadges() != null && !report.getBadges().isEmpty()) {
            for (ReportResponse.Badge badge : report.getBadges()) {
                View badgeView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_badge, badgeContainer, false);

                ImageView badgeImg = badgeView.findViewById(R.id.badge_image);
                TextView badgeText = badgeView.findViewById(R.id.badge_text);

                Glide.with(this).load(badge.getImageUrl()).into(badgeImg);

                String badgeName = getBadgeDisplayName(badge.getBadgeType(), badge.getTier());
                badgeText.setText(badgeName + " 획득!");

                badgeContainer.addView(badgeView);
            }
        } else {
            TextView noBadge = new TextView(getContext());
            noBadge.setText("아직 획득한 배지가 없어요 🥹");
            badgeContainer.addView(noBadge);
        }
    }

    private String getBadgeDisplayName(String type, String tier) {
        switch (type) {
            case "FULL_READ": return tier + " 완독 배지";
            case "GOAL_MASTER": return tier + " 목표 마스터 배지";
            case "REVIEW_MASTER": return tier + " 감상문 마스터 배지";
            case "MARATHONER": return tier + " 마라토너 배지";
            case "GENRE_MASTER": return tier + " 장르 마스터 배지";
            case "REVIEW_SHARE_MASTER": return tier + " 감상문 공유 마스터 배지";
            default: return "미확인 배지";
        }
    }

    private void clearReportUI() {
        TextView totalPages = getView().findViewById(R.id.totalPagesNumber);
        TextView totalMinutes = getView().findViewById(R.id.totalMinutesNumber);
        TextView completedReviews = getView().findViewById(R.id.completedReviewsNumber);
        TextView avgDaily = getView().findViewById(R.id.averageDailyMinutesNumber);
        TextView experience = getView().findViewById(R.id.experienceNumber);
        TextView level = getView().findViewById(R.id.levelNumber);
        LinearLayout badgeContainer = getView().findViewById(R.id.badge_container);

        totalPages.setText("-");
        totalMinutes.setText("-");
        completedReviews.setText("-");
        avgDaily.setText("-");
        experience.setText("-");
        level.setText("-");
        badgeContainer.removeAllViews();
    }

    private void loadGoal(String goalType) {
        goalController.getGoals(token, new GoalController.GoalCallback<List<GoalResponse>>() {
            @Override
            public void onSuccess(List<GoalResponse> goals) {
                if (goals != null && !goals.isEmpty()) {
                    int year = currentCalendar.get(Calendar.YEAR);
                    int month = currentCalendar.get(Calendar.MONTH) + 1;

                    GoalResponse selectedGoal = null;

                    for (GoalResponse goal : goals) {
                        if ("MONTHLY".equalsIgnoreCase(goalType)) {
                            if ("MONTHLY".equalsIgnoreCase(goal.getGoalType())
                                    && goal.getYear() == year
                                    && goal.getMonth() != null
                                    && goal.getMonth() == month) {
                                selectedGoal = goal;
                                break;
                            }
                        } else if ("YEARLY".equalsIgnoreCase(goalType)) {
                            if ("YEARLY".equalsIgnoreCase(goal.getGoalType())
                                    && goal.getYear() == year) {
                                selectedGoal = goal;
                                break;
                            }
                        }
                    }

                    if (selectedGoal != null) {
                        bindGoalData(selectedGoal);
                    } else {
                        Toast.makeText(getContext(), "독서 기록이 없습니다.", Toast.LENGTH_SHORT).show();
                        clearGoalUI();
                    }
                } else {
                    Toast.makeText(getContext(), "독서 기록이 없습니다.", Toast.LENGTH_SHORT).show();
                    clearGoalUI();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("DetailGoalFragment", "loadGoal error: " + errorMessage);
                Toast.makeText(getContext(), "독서 기록이 없습니다.", Toast.LENGTH_SHORT).show();
                clearGoalUI();
            }
        });
    }

    private void clearGoalUI() {
        // 목표가 없을 때 UI 초기화 (진행률 0으로 세팅)
        CircularProgressView circleProgress = getView().findViewById(R.id.circle_progress);
        ProgressBarUtil.setCircularProgress(circleProgress, 0);

        GoalProgressView goal1 = getView().findViewById(R.id.goal1_view);
        goal1.setProgressWithGoal(0f, 1f);

        GoalProgressView goal2 = getView().findViewById(R.id.goal2_view);
        goal2.setProgressWithGoal(0f, 1f);

        GoalProgressView goal3 = getView().findViewById(R.id.goal3_view);
        goal3.setProgressWithGoal(0f, 1f);

        // 책 리스트 초기화
        LinearLayout bookListContainer = getView().findViewById(R.id.book_list_container);
        bookListContainer.removeAllViews();

        TextView emptyMessage = new TextView(requireContext());
        emptyMessage.setText("아직 완독한 책이 없어요!");
        emptyMessage.setTextColor(Color.DKGRAY);
        emptyMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        emptyMessage.setPadding(16, 16, 16, 16);
        bookListContainer.addView(emptyMessage);
    }

    private void bindGoalData(GoalResponse goal) {
        float totalProgress = (float) ((goal.getBookProgress() + goal.getReviewProgress() + goal.getTimeProgress()) / 3.0);

        // 원형 전체 진행률
        CircularProgressView circleProgress = getView().findViewById(R.id.circle_progress);
        ProgressBarUtil.setCircularProgress(circleProgress, totalProgress);

        // 목표 1 (책 권수)
        GoalProgressView goal1 = getView().findViewById(R.id.goal1_view);
        goal1.setUnit("권");
        goal1.setProgressWithGoal(goal.getCompletedBooks(), goal.getTargetBooks(),goal.getBookProgress());

        // 목표 2 (리뷰 개수)
        GoalProgressView goal2 = getView().findViewById(R.id.goal2_view);
        goal2.setUnit("개");
        goal2.setProgressWithGoal(goal.getCompletedReviews(), goal.getTargetReviews(),goal.getReviewProgress());

        // 목표 3 (독서 시간)
        GoalProgressView goal3 = getView().findViewById(R.id.goal3_view);
        goal3.setUnit("시간");
        float completedHours = goal.getCompletedMinutes() / 60f;
        float targetHours = goal.getTargetMinutes() / 60f;
        goal3.setProgressWithGoal(completedHours, targetHours, goal.getTimeProgress());


        //응원 메시지
        TextView cheerMessage = getView().findViewById(R.id.cheer_message);
        String message;
        if (totalProgress >= 80) {
            // 80~100%
            String[] options = {
                    "🎉 이번 달 목표를 멋지게 해냈어요!",
                    "🏆 " + goal.getMonth() + "월의 독서 여정을 완주했네요, 대단해요!"
            };
            message = options[new Random().nextInt(options.length)];
        } else if (totalProgress >= 50) {
            // 50~79%
            String[] options = {
                    "📚 목표의 절반 이상을 달성했어요, 조금만 더 힘내봐요!",
                    "🌱 꾸준한 독서가 멋져요, 다음 달엔 더 큰 성취를 기대해요."
            };
            message = options[new Random().nextInt(options.length)];
        } else if (totalProgress >= 1) {
            // 1~49%
            String[] options = {
                    "🚶 시작이 반이에요, 다음 달엔 조금 더 도전해볼까요?",
                    "💡 조금 아쉽지만, 꾸준함이 가장 큰 힘이 돼요."
            };
            message = options[new Random().nextInt(options.length)];
        } else {
            // 0% or 목표 없음
            String[] options = {
                    "😌 이번 달은 쉬어가는 달이었네요. 다음 달엔 함께 해봐요!",
                    "📖 책과의 새로운 만남을 준비해볼까요?"
            };
            message = options[new Random().nextInt(options.length)];
        }

        cheerMessage.setText(message);

        // BookListHelper용 리스트 생성
        LinearLayout bookListContainer = getView().findViewById(R.id.book_list_container);
        bookListContainer.removeAllViews();

        List<GoalResponse.BookInfo> books = goal.getBooks();
        if (books != null && !books.isEmpty()) {
            List<BookListHelper.BookItem> bookItems = new ArrayList<>();
            for (GoalResponse.BookInfo book : books) {
                // URL 기반 BookItem 생성
                bookItems.add(new BookListHelper.BookItem(book.getCoverUrl(),true, false));
            }

            // BookListHelper로 세팅
            BookListHelper.setupBooks(requireContext(), bookListContainer, bookItems, false);

        } else {
            // 책이 없는 경우 안내 문구
            TextView emptyMessage = new TextView(requireContext());
            emptyMessage.setText("아직 완독한 책이 없어요!");
            emptyMessage.setTextColor(Color.DKGRAY);
            emptyMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            emptyMessage.setPadding(16, 16, 16, 16);
            bookListContainer.addView(emptyMessage);
        }

    }

    private void updateDateText() {
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH) + 1;
        if ("MONTHLY".equals(selectedGoalType)) {
            textMonth.setText(String.format(Locale.getDefault(), "%d년 %d월", year, month));
            title.setText(month + "월의 리포트 \uD83D\uDCCA");
        } else {
            textMonth.setText(String.format(Locale.getDefault(), "%d년", year));
            title.setText(year + "년의 리포트 \uD83D\uDCCA");
        }


    }
}

