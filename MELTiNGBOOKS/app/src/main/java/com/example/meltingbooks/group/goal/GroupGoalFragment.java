package com.example.meltingbooks.group.goal;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.meltingbooks.R;
import com.example.meltingbooks.group.goal.GroupProgressBarUtil;
import com.example.meltingbooks.group.goal.GroupGoalProgressView;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.group.goal.GroupGoalApi;
import com.example.meltingbooks.network.group.goal.GroupGoalController;
import com.example.meltingbooks.network.group.goal.GroupGoalResponse;

import java.util.List;

public class GroupGoalFragment extends Fragment {

    private int groupId; // Activity나 arguments로 받은 그룹 ID
    private int ownerId;
    private String token;
    private int currentUserId;

    private GroupGoalController goalController;
    private GroupGoalProgressView goal1, goal2, goal3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("GroupSetGoalFragment", "onCreateView called");
        View view = inflater.inflate(R.layout.group_goal_fragment, container, false);

        SharedPreferences prefs = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE);
        token = prefs.getString("jwt", null);
        currentUserId = prefs.getInt("userId", -1);

        if (token == null || currentUserId == -1) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return null; // ← 반드시 null 반환
        }


        // arguments로 groupId 받기
        if (getArguments() != null) {
            groupId = getArguments().getInt("groupId", -1);
            ownerId = getArguments().getInt("ownerId", -1);
        }

        // ProgressView 초기화
        goal1 = view.findViewById(R.id.goal1_view);
        goal2 = view.findViewById(R.id.goal2_view);
        goal3 = view.findViewById(R.id.goal3_view);

        // arrow 클릭 처리
        ImageView groupArrow = view.findViewById(R.id.group_arrow);
        // 그룹장만 버튼 보이게
        if (currentUserId == ownerId) {
            groupArrow.setVisibility(View.VISIBLE);
            groupArrow.setOnClickListener(v -> openGroupSetGoalFragment());
        } else {
            groupArrow.setVisibility(View.GONE);
        }


        // GoalController 초기화 (token은 SharedPreferences 등에서 가져오기)
        String token = requireContext().getSharedPreferences("auth", getContext().MODE_PRIVATE)
                .getString("jwt", null);
        goalController = new GroupGoalController(ApiClient.getClient(token).create(GroupGoalApi.class));

        // 이번 달 목표 불러오기
        loadGroupGoal(groupId);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadGroupGoal(groupId); // 항상 최신 목표 불러오기
    }


    /** 이번 달 그룹 목표 조회 */
    private void loadGroupGoal(int groupId) {
        goalController.getGroupGoals(groupId, new GroupGoalController.GoalCallback<List<GroupGoalResponse>>() {
            @Override
            public void onSuccess(List<GroupGoalResponse> goals) {
                Log.d("GroupFeed", "loadGroupGoal 성공, goals.size=" + (goals != null ? goals.size() : 0));
                if (goals != null && !goals.isEmpty()) {

                    // 현재 연도/월 구하기
                    java.util.Calendar calendar = java.util.Calendar.getInstance();
                    int currentYear = calendar.get(java.util.Calendar.YEAR);
                    int currentMonth = calendar.get(java.util.Calendar.MONTH) + 1; // 0부터 시작

                    GroupGoalResponse selectedGoal = null;

                    for (GroupGoalResponse goal : goals) {
                        if (goal.getStartDate() != null && goal.getEndDate() != null) {
                            java.time.LocalDate start = java.time.LocalDate.parse(goal.getStartDate());
                            java.time.LocalDate end = java.time.LocalDate.parse(goal.getEndDate());

                            if (start.getYear() == currentYear && start.getMonthValue() == currentMonth) {
                                selectedGoal = goal;
                                break;
                            }
                        }
                    }

                    if (selectedGoal != null) {
                        bindGroupGoalData(selectedGoal);
                    } else {
                        Log.w("GroupGoalFragment", "이번 달 그룹 목표가 없습니다.");
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("GroupGoalFragment", "loadGroupGoal error: " + errorMessage);
            }
        });
    }

    /** ProgressView에 목표 데이터 바인딩 */
    private void bindGroupGoalData(GroupGoalResponse goal) {
        if (goal == null) return;

        // 1️⃣ 목표 퍼센트 계산 (0~100)
        float bookProgress = goal.getTargetBooks() > 0 ?
                ((float) goal.getCompletedBooks() / goal.getTargetBooks()) * 100f : 0f;
        float reviewProgress = goal.getTargetReviews() > 0 ?
                ((float) goal.getCompletedReviews() / goal.getTargetReviews()) * 100f : 0f;
        float timeProgress = goal.getTargetMinutes() > 0 ?
                ((float) goal.getCompletedMinutes() / goal.getTargetMinutes()) * 100f : 0f;

        // 1️⃣5️⃣ 로그 출력
        Log.d("GroupGoalFragment", "CompletedBooks=" + goal.getCompletedBooks() +
                ", TargetBooks=" + goal.getTargetBooks() + ", bookProgress=" + bookProgress);
        Log.d("GroupGoalFragment", "CompletedReviews=" + goal.getCompletedReviews() +
                ", TargetReviews=" + goal.getTargetReviews() + ", reviewProgress=" + reviewProgress);
        Log.d("GroupGoalFragment", "CompletedMinutes=" + goal.getCompletedMinutes() +
                ", TargetMinutes=" + goal.getTargetMinutes() + ", timeProgress=" + timeProgress);

        // 2️⃣ GoalProgressView 단위 설정
        goal1.setUnit("권");
        goal2.setUnit("개");
        goal3.setUnit("시간");

        // 3️⃣ GoalProgressView에 값 적용
        goal1.setProgressWithGoal(goal.getCompletedBooks(), goal.getTargetBooks(), bookProgress, null, null);
        goal2.setProgressWithGoal(goal.getCompletedReviews(), goal.getTargetReviews(), reviewProgress, null, null);
        goal3.setProgressWithGoal(goal.getCompletedMinutes() / 60f, goal.getTargetMinutes() / 60f, timeProgress, null, null);

        // 3️⃣5️⃣ ProgressView width 확인용 로그
        goal1.post(() -> Log.d("GroupGoalFragment", "goal1 width=" + goal1.getWidth()));
        goal2.post(() -> Log.d("GroupGoalFragment", "goal2 width=" + goal2.getWidth()));
        goal3.post(() -> Log.d("GroupGoalFragment", "goal3 width=" + goal3.getWidth()));
    }


    /** 목표 설정 Fragment 열기 */
    private void openGroupSetGoalFragment() {
        GroupSetGoalFragment fragment = new GroupSetGoalFragment();

        // groupId 전달
        Bundle args = new Bundle();
        args.putInt("groupId", groupId);
        fragment.setArguments(args);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.feed_fragment_container, fragment) // ✅ ScrollView 영역이 아닌 Activity 루트 container로 교체
                .addToBackStack(null)
                .commit();

        // container가 숨겨져 있다면 표시
        requireActivity().findViewById(R.id.feed_fragment_container).setVisibility(View.VISIBLE);
    }



}
