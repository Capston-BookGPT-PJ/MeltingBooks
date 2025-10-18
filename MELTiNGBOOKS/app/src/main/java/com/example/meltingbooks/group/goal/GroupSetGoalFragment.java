package com.example.meltingbooks.group.goal;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.meltingbooks.R;
import com.example.meltingbooks.group.GroupFeedActivity;
import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.goal.GoalRequest;
import com.example.meltingbooks.network.group.goal.GroupGoalApi;
import com.example.meltingbooks.network.group.goal.GroupGoalController;
import com.example.meltingbooks.network.group.goal.GroupGoalRequest;
import com.example.meltingbooks.network.group.goal.GroupGoalResponse;
import com.google.gson.Gson;

import java.time.LocalDate;
import java.util.List;

public class GroupSetGoalFragment extends Fragment {

    private GroupGoalController goalController;
    private String token;
    private int userId;
    private Integer currentGoalId = null;

    private EditText editPage, editReview, editTime;
    private TextView btnSaveGoal, btnDeleteGoal, tvTitle;

    private int groupId; // 반드시 set 또는 bundle로 전달받아야 함


    public GroupSetGoalFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("GroupSetGoalFragment", "onCreateView called");
        View view = inflater.inflate(R.layout.group_fragment_set_goal, container, false);

        if (getArguments() != null) {
            groupId = getArguments().getInt("groupId");
            // groupId 사용 가능
        }

        tvTitle = view.findViewById(R.id.set_goal_title);
        btnSaveGoal = view.findViewById(R.id.btn_save_goal);
        btnDeleteGoal = view.findViewById(R.id.btn_delete_goal);

        editPage = view.findViewById(R.id.edit_page);
        editReview = view.findViewById(R.id.edit_review);
        editTime = view.findViewById(R.id.edit_time);

        SharedPreferences prefs = requireContext().getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("jwt", null);
        userId = prefs.getInt("userId", -1);

        GroupGoalApi groupGoalApi = ApiClient.getClient(token).create(GroupGoalApi.class);
        goalController = new GroupGoalController(groupGoalApi);

        btnSaveGoal.setOnClickListener(v -> saveGroupGoal(groupId));
        btnDeleteGoal.setOnClickListener(v -> deleteGroupGoal(groupId));

        // 시작 시 이번 달 목표 자동 불러오기
        loadGroupGoal(groupId);

        return view;
    }

    private void loadGroupGoal(int groupId) {
        goalController.getGroupGoals(groupId, new GroupGoalController.GoalCallback<List<GroupGoalResponse>>() {
            @Override
            public void onSuccess(List<GroupGoalResponse> goals) {
                Log.d("GroupGoalFragment", "loadGroupGoal 성공, goals.size=" + (goals != null ? goals.size() : 0));

                if (goals != null && !goals.isEmpty()) {
                    java.util.Calendar calendar = java.util.Calendar.getInstance();
                    int currentYear = calendar.get(java.util.Calendar.YEAR);
                    int currentMonth = calendar.get(java.util.Calendar.MONTH) + 1;

                    GroupGoalResponse selectedGoal = null;

                    for (GroupGoalResponse goal : goals) {
                        if (goal.getStartDate() != null && goal.getEndDate() != null) {
                            LocalDate start = LocalDate.parse(goal.getStartDate());
                            LocalDate end = LocalDate.parse(goal.getEndDate());

                            // 이번 달 목표인지 체크
                            if (start.getYear() == currentYear && start.getMonthValue() == currentMonth) {
                                selectedGoal = goal;
                                break;
                            }
                        }
                    }

                    if (selectedGoal != null) {
                        applyGoalToUI(selectedGoal);
                        currentGoalId = selectedGoal.getId();
                    } else {
                        Log.w("GroupGoalFragment", "이번 달 그룹 목표가 없습니다.");
                        clearUI(); // 목표가 없을 때 UI 초기화
                    }
                } else {
                    Log.w("GroupGoalFragment", "목표 데이터 자체가 없습니다.");
                    clearUI();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("GroupGoalFragment", "loadGroupGoal error: " + errorMessage);
                clearUI();
            }
        });
    }


    /** ✅ 이번 달 그룹 목표 저장 (기존 모든 목표 삭제 후 새로 생성) */
    private void saveGroupGoal(int groupId) {
        if (editPage.getText().toString().isEmpty()
                || editReview.getText().toString().isEmpty()
                || editTime.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        int books = Integer.parseInt(editPage.getText().toString());
        int reviews = Integer.parseInt(editReview.getText().toString());
        int hours = Integer.parseInt(editTime.getText().toString());
        int minutes = hours * 60;

        LocalDate now = LocalDate.now();
        String startDate = String.format("%04d-%02d-01", now.getYear(), now.getMonthValue());
        String endDate = now.withDayOfMonth(now.lengthOfMonth()).toString();

        String title = "그룹 공통 독서 목표";
        String description = "월간 목표";

        GroupGoalRequest request = new GroupGoalRequest(
                title,
                description,
                startDate,
                endDate,
                books,
                reviews,
                minutes
        );

        // ✅ 1. 먼저 모든 기존 목표 불러오기
        goalController.getGroupGoals(groupId, new GroupGoalController.GoalCallback<List<GroupGoalResponse>>() {
            @Override
            public void onSuccess(List<GroupGoalResponse> goals) {
                if (goals != null && !goals.isEmpty()) {
                    Log.d("GroupSetGoalFragment", "기존 목표 개수: " + goals.size());

                    // ✅ 2. 모든 목표 삭제
                    deleteAllGoalsThenCreate(groupId, goals, request);
                } else {
                    // ✅ 기존 목표 없음 → 바로 생성
                    createNewGoal(groupId, request);
                }
            }

            @Override
            public void onError(String errorMsg) {
                Log.e("GroupSetGoalFragment", "목표 불러오기 실패: " + errorMsg);
                // 실패하더라도 새로 생성은 시도
                createNewGoal(groupId, request);
            }
        });
    }

    /** ✅ 여러 목표 전부 삭제 후 새 목표 생성 */
    private void deleteAllGoalsThenCreate(int groupId, List<GroupGoalResponse> goals, GroupGoalRequest request) {
        if (goals.isEmpty()) {
            createNewGoal(groupId, request);
            return;
        }

        // 삭제 완료 카운터
        final int[] deletedCount = {0};
        int total = goals.size();

        for (GroupGoalResponse goal : goals) {
            goalController.deleteGroupGoal(groupId, goal.getId(), new GroupGoalController.GoalCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    deletedCount[0]++;
                    Log.d("GroupSetGoalFragment", "목표 삭제 완료: " + goal.getId());

                    // ✅ 모두 삭제 완료되면 새 목표 생성
                    if (deletedCount[0] == total) {
                        Log.d("GroupSetGoalFragment", "모든 목표 삭제 완료. 새 목표 생성 시작");
                        createNewGoal(groupId, request);
                    }
                }

                @Override
                public void onError(String errorMsg) {
                    deletedCount[0]++;
                    Log.e("GroupSetGoalFragment", "목표 삭제 실패: " + goal.getId() + ", msg=" + errorMsg);

                    // 실패하더라도 모든 시도 후에는 생성 진행
                    if (deletedCount[0] == total) {
                        createNewGoal(groupId, request);
                    }
                }
            });
        }
    }

    /** ✅ 새로운 그룹 목표 생성 후 재계산 + 뒤로가기 */
    private void createNewGoal(int groupId, GroupGoalRequest request) {
        goalController.createGroupGoal(groupId, request, new GroupGoalController.GoalCallback<GroupGoalResponse>() {
            @Override
            public void onSuccess(GroupGoalResponse result) {
                currentGoalId = result.getId();
                applyGoalToUI(result);
                Toast.makeText(getContext(), "새 목표 생성 완료!", Toast.LENGTH_SHORT).show();

                // 달성률 재계산
                goalController.recomputeGroupGoal(groupId, currentGoalId, new GroupGoalController.GoalCallback<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        Toast.makeText(getContext(), "달성률 재계산 완료!", Toast.LENGTH_SHORT).show();
                        requireActivity().runOnUiThread(() -> {
                            requireActivity().getSupportFragmentManager().popBackStack();
                        });
                    }

                    @Override
                    public void onError(String errorMsg) {
                        Log.e("GroupSetGoalFragment", "Recompute 실패: " + errorMsg);
                        // Fragment에서 Activity에 알리고 뒤로가기
                        requireActivity().runOnUiThread(() -> {
                            // 피드 컨테이너 다시 숨기기
                            View feedContainer = requireActivity().findViewById(R.id.feed_fragment_container);
                            if (feedContainer != null) feedContainer.setVisibility(View.GONE);

                            // Activity에 refreshPost 인텐트 전달
                            Intent intent = new Intent(requireActivity(), GroupFeedActivity.class);
                            intent.putExtra("refreshPost", true);
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            requireActivity().startActivity(intent);

                            // Fragment 종료
                            requireActivity().getSupportFragmentManager().popBackStack();
                        });


                    }
                });
            }

            @Override
            public void onError(String errorMsg) {
                Log.e("GroupSetGoalFragment", "목표 생성 실패: " + errorMsg);
                Toast.makeText(getContext(), "목표 생성 실패: " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** ✅ 그룹 목표 삭제 */
    private void deleteGroupGoal(int groupId) {
        if (currentGoalId == null) {
            Toast.makeText(getContext(), "삭제할 목표가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        goalController.deleteGroupGoal(groupId, currentGoalId, new GroupGoalController.GoalCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                clearUI();
                currentGoalId = null;
                Toast.makeText(getContext(), "목표 삭제 완료", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(getContext(), "삭제 실패: " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** ✅ UI 반영 */
    private void applyGoalToUI(GroupGoalResponse goal) {
        editPage.setText(String.valueOf(goal.getTargetBooks()));
        editReview.setText(String.valueOf(goal.getTargetReviews()));

        int totalMinutes = goal.getTargetMinutes();
        int hours = totalMinutes / 60;
        editTime.setText(String.valueOf(hours));
    }

    private void clearUI() {
        editPage.setText("");
        editReview.setText("");
        editTime.setText("");
    }
}
