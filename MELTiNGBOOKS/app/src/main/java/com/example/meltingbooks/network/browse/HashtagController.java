package com.example.meltingbooks.network.browse;

import android.util.Log;

import com.example.meltingbooks.network.ApiClient;
import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.ApiService;
import com.example.meltingbooks.network.feed.FeedPageResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class HashtagController {
    private ApiService apiService;

    // 토큰을 외부에서 주입받도록 생성자 수정
    public HashtagController(String token) {

        if (token == null) {
            Log.e("HashtagController", "JWT 토큰이 없습니다.");
            return;
        }
        apiService = ApiClient.getClient(token).create(ApiService.class);

    }

    //인기 해시태그
    public void fetchPopularHashtags(Callback<ApiResponse<List<HashtagResponse>>> callback) {
        Call<ApiResponse<List<HashtagResponse>>> call = apiService.getPopularHashtags();
        call.enqueue(callback);
    }

    //해시태그 해당하는 리뷰
    public void fetchReviewsByHashtag(String hashtag,
                                      Callback<ApiResponse<FeedPageResponse>> callback) {

        /*String encodedHashtag = Uri.encode(hashtag); // 여기서 한 번만 인코딩
        Call<ApiResponse<FeedPageResponse>> call =
                apiService.getReviewsByHashtag(encodedHashtag);
                call.enqueue(callback);*/

        // hashtag 값이 "#커피" 형태라면
        String serverParam = "%23" + hashtag.replace("#", "");
        Call<ApiResponse<FeedPageResponse>> call = apiService.getReviewsByHashtag(serverParam);
        call.enqueue(callback);

    }
}
