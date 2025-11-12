package com.example.meltingbooks.network.growth;

import retrofit2.Call;
import retrofit2.Callback;

public class GrowthController {

    private final GrowthApi api;
    private final String token;

    public GrowthController(GrowthApi api, String token) {
        this.api = api;
        this.token = token;
    }

    public void giveExp(int userId, String eventType, Callback<String> callback) {
        api.giveExp("Bearer " + token, userId, eventType).enqueue(callback);
    }
}
