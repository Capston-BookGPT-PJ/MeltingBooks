package com.example.meltingbooks.network.group;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.group.feed.GroupPostResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupController {

    private static final String TAG = "GroupController";
    private GroupApi groupApi;
    private final Context context;

    public GroupController(Context context) {
        this.context = context;
        SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt", null);

        if (token == null) {
            Log.e(TAG, "JWT 토큰이 없습니다.");
            return;
        }

        groupApi = ApiClient.getClient(token).create(GroupApi.class);
    }

    //그룹 생성
    public void createGroup(Group group, OnGroupActionCallback callback) {
        if (groupApi == null) return;

        groupApi.createGroup(group).enqueue(new Callback<GroupPostResponse>() {
            @Override
            public void onResponse(Call<GroupPostResponse> call, Response<GroupPostResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("생성 실패: 코드 " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GroupPostResponse> call, Throwable t) {
                callback.onFailure("서버 연결 실패");
            }
        });
    }


    //그룹 정보 수정
    public void updateGroup(int groupId, Group updatedGroup, OnGroupActionCallback callback) {
        if (groupApi == null) return;

        groupApi.updateGroup(groupId, updatedGroup).enqueue(new Callback<GroupPostResponse>() {
            @Override
            public void onResponse(Call<GroupPostResponse> call, Response<GroupPostResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("수정 실패: 코드 " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GroupPostResponse> call, Throwable t) {
                callback.onFailure("서버 연결 실패");
            }
        });
    }



    // 그룹 삭제 (그룹장 전용)
    public void deleteGroup(int groupId, OnDeleteGroupCallback callback) {
        if (groupApi == null) return;

        groupApi.deleteGroup(groupId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("코드: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onFailure("서버 연결 실패");
            }
        });
    }

    public interface OnDeleteGroupCallback {
        void onSuccess();
        void onFailure(String message);
    }


    // 그룹 검색
    public void searchGroups(String keyword, String category, Callback<GroupAllList> callback) {
        if (groupApi == null) return;

        Call<GroupAllList> call = groupApi.searchGroups(keyword, category);
        call.enqueue(callback);
    }

    // 단일 그룹 조회->그룹 프로필 조회
    public void getGroupById(int groupId, Callback<GroupPostResponse> callback) {
        if (groupApi == null) return;

        Call<GroupPostResponse> call = groupApi.getGroupById(groupId);
        call.enqueue(callback);
    }

    //그룹 가입
    public void joinGroup(int groupId) {
        groupApi.joinGroup(groupId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "그룹 가입 신청 완료", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "가입 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "서버 연결 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //그룹 탈퇴
    public void leaveGroup(int groupId, OnLeaveGroupCallback callback) {
        if (groupApi == null) return;

        groupApi.leaveGroup(groupId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(); // 탈퇴 성공
                } else {
                    callback.onFailure("코드: " + response.code()); // 실패
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onFailure("서버 연결 실패");
            }
        });
    }
    public interface OnLeaveGroupCallback {
        void onSuccess();
        void onFailure(String message);
    }

    public interface OnGroupActionCallback {
        void onSuccess(Object result); // GroupSingleList 혹은 null
        void onFailure(String message);
    }


}
