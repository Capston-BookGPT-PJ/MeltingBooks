package com.example.meltingbooks.network;

import com.example.meltingbooks.MyApp;
import com.example.meltingbooks.login.TokenManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://meltingbooks.o-r.kr:8080/";
    private static Retrofit retrofit;
    private static String currentToken;

    public static Retrofit getClient(String token) {
        if (retrofit == null || !token.equals(currentToken)) {
            currentToken = token;

            OkHttpClient client = new OkHttpClient.Builder()
                    // ✅ 1) Authorization 헤더 기존 방식 그대로 유지
                    .addInterceptor(chain -> {
                        Request request = chain.request().newBuilder()
                                .addHeader("Authorization", "Bearer " + token)
                                .build();
                        return chain.proceed(request);
                    })

                    // ✅ 2) 응답 코드 체크 → 401/403 자동 로그아웃 추가
                    .addInterceptor(chain -> {
                        okhttp3.Response response = chain.proceed(chain.request());

                        if (response.code() == 401 || response.code() == 403) {
                            TokenManager.forceLogout(MyApp.getAppContext());
                        }

                        return response;
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
