package com.example.meltingbooks.network.browse;

import com.example.meltingbooks.network.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserController {
    private UserApi userApi;

    public UserController(String token) {
        userApi = ApiClient.getClient(token).create(UserApi.class);
    }

    public void searchUsers(String nickname, Callback<List<PopularUser>> callback) {
        userApi.searchUsers(nickname).enqueue(callback);
    }

    public void fetchPopularUsers(PopularUsersCallback callback) {
        userApi.getPopularUsers().enqueue(new Callback<List<PopularUser>>() {
            @Override
            public void onResponse(Call<List<PopularUser>> call, Response<List<PopularUser>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Response failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<PopularUser>> call, Throwable t) {
                callback.onError("API call failed: " + t.getMessage());
            }
        });
    }

    public interface PopularUsersCallback {
        void onSuccess(List<PopularUser> users);
        void onError(String errorMessage);
    }
}
