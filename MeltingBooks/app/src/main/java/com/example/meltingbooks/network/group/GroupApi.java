package com.example.meltingbooks.network.group;

import com.example.meltingbooks.network.ApiResponse;
import com.example.meltingbooks.network.group.comment.GroupCommentPageResponse;
import com.example.meltingbooks.network.group.comment.GroupCommentRequest;
import com.example.meltingbooks.network.group.comment.GroupCommentResponse;
import com.example.meltingbooks.network.group.comment.GroupCommonResponse;
import com.example.meltingbooks.network.group.feed.CreateGroupNotice;
import com.example.meltingbooks.network.group.feed.CreateGroupRecommend;
import com.example.meltingbooks.network.group.feed.CreatePostRequest;
import com.example.meltingbooks.network.group.feed.GroupFeedPageResponse;
import com.example.meltingbooks.network.group.feed.GroupPostResponse;
import com.example.meltingbooks.network.group.feed.GroupReviewResponse;
import com.example.meltingbooks.network.group.feed.UpdatePostRequest;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GroupApi {

    //그룹 생성
    @POST("/api/groups")
    Call<GroupPostResponse> createGroup(@Body Group group);

    // 그룹 수정
    @PUT("/api/groups/{groupId}") // 서버가 PUT을 지원하면 PUT으로 바꾸세요
    Call<GroupPostResponse> updateGroup(
            @Path("groupId") int groupId,
            @Body Group group
    );

    // 그룹 삭제
    @DELETE("/api/groups/{groupId}")
    Call<Void> deleteGroup(@Path("groupId") int groupId);

    //그룹 검색
    @GET("/api/groups/search")
    Call<GroupAllList> searchGroups(
            @Query("keyword") String keyword,
            @Query("category") String category
    );

    //그룹 프로필 조회
    @GET("/api/groups/{id}")
    Call<GroupPostResponse> getGroupById(@Path("id") int groupId);


    //그룹 가입
    @POST("/api/groups/{groupId}/join")
    Call<Void> joinGroup(@Path("groupId") int groupId);

    //그룹 탈퇴
    @DELETE("/api/groups/{groupId}/leave")
    Call<Void> leaveGroup(@Path("groupId") int groupId);

    //내 그룹 조회
    @GET("/api/groups/me")
    Call<ApiResponse<List<MyGroup>>> getMyGroups();

    // 그룹 피드 페이징 가져오기
    @GET("/api/groups/{groupId}/feed")
    Call<ApiResponse<GroupFeedPageResponse>> getGroupFeed(
            @Header("Authorization") String token,
            @Path("groupId") int groupId,
            @Query("page") int page,
            @Query("size") int size
    );

    // 피드 생성
    @POST("/api/groups/{groupId}/posts")
    Call<ApiResponse<GroupReviewResponse>> createPost(
            @Header("Authorization") String token,
            @Path("groupId") int groupId,
            @Body CreatePostRequest request
    );

    // 피드 수정
    @PUT("/api/groups/{groupId}/posts/{postId}")
    Call<ApiResponse<GroupReviewResponse>> updatePost(
            @Header("Authorization") String token,
            @Path("groupId") int groupId,
            @Path("postId") int postId,
            @Query("userId") int userId,
            @Body UpdatePostRequest request
    );

    // 피드 삭제
    @DELETE("/api/groups/{groupId}/posts/{postId}")
    Call<Void> deletePost(
            @Header("Authorization") String token,
            @Path("groupId") int groupId,
            @Path("postId") int postId
    );

    // 피드 하나 조회
    @GET("/api/groups/{groupId}/posts/{postId}")
    Call<ApiResponse<GroupReviewResponse>> getPost(
            @Header("Authorization") String token,
            @Path("groupId") int groupId,
            @Path("postId") int postId,
            @Query("userId") int userId

    );

    // 피드 이미지 업로드
    @Multipart
    @POST("/api/groups/{groupId}/posts/{postId}/images")
    Call<ApiResponse<List<String>>> uploadPostImages(
            @Header("Authorization") String token,
            @Path("groupId") int groupId,
            @Path("postId") int postId,
            @Part MultipartBody.Part file
    );

    // 피드 좋아요 등록
    @POST("/api/groups/{groupId}/posts/{postId}/likes")
    Call<ApiResponse<Void>> likePost(
            @Header("Authorization") String token,
            @Path("groupId") int groupId,
            @Path("postId") int postId
    );

    // 피드 좋아요 취소
    @DELETE("/api/groups/{groupId}/posts/{postId}/likes")
    Call<ApiResponse<Void>> unlikePost(
            @Header("Authorization") String token,
            @Path("groupId") int groupId,
            @Path("postId") int postId
    );

    // 그룹 댓글 작성
    @POST("/api/groups/{groupId}/posts/{postId}/comments")
    Call<ApiResponse<GroupCommentResponse>> createGroupComment(
            @Header("Authorization") String token,
            @Path("groupId") int groupId,
            @Path("postId") int postId,
            @Body GroupCommentRequest groupCommentRequest
    );

    // 그룹 댓글 수정
    @PUT("/api/groups/{groupId}/posts/{postId}/comments/{commentId}")
    Call<ApiResponse<GroupCommentResponse>> updateGroupComment(
            @Header("Authorization") String token,
            @Path("groupId") int groupId,
            @Path("postId") int postId,
            @Path("commentId") int commentId,
            @Body GroupCommentRequest groupCommentRequest
    );

    // 그룹 댓글 삭제
    @DELETE("/api/groups/{groupId}/posts/{postId}/comments/{commentId}")
    Call<ApiResponse<Void>> deleteGroupComment(
            @Header("Authorization") String token,
            @Path("groupId") int groupId,
            @Path("postId") int postId,
            @Path("commentId") int commentId
    );

    // 그룹 댓글 목록 조회 (페이징)
    @GET("/api/groups/{groupId}/posts/{postId}/comments")
    Call<ApiResponse<GroupCommentPageResponse>> getGroupComments(
            @Header("Authorization") String token,
            @Path("groupId") int groupId,
            @Path("postId") int postId,
            @Query("page") int page,
            @Query("size") int size
    );

    // 🔹 1. 그룹 가입 요청 리스트 조회
    @GET("/api/groups/{groupId}/join-requests")
    Call<GroupJoinRequestResponse> getJoinRequests(
            @Header("Authorization") String token,
            @Path("groupId") int groupId,
            @Query("page") int page,
            @Query("size") int size
    );

    // 🔹 2. 그룹장 - 가입 요청 승인
    @POST("/api/groups/{groupId}/accept")
    Call<GroupCommonResponse> acceptJoinRequest(
            @Header("Authorization") String token,
            @Path("groupId") int groupId,
            @Query("memberUserId") int memberUserId
    );

    // 🔹 3. 그룹장 - 가입 요청 거절
    @POST("/api/groups/{groupId}/reject")
    Call<GroupCommonResponse> rejectJoinRequest(
            @Header("Authorization") String token,
            @Path("groupId") int groupId,
            @Query("memberUserId") int memberUserId
    );

    // 🔹 4. 그룹장 권한 위임
    @PUT("/api/groups/{groupId}/delegate-owner")
    Call<GroupCommonResponse> delegateGroupOwner(
            @Header("Authorization") String token,
            @Path("groupId") int groupId,
            @Query("newOwnerId") int newOwnerId
    );

    @POST("/api/groups/{groupId}/posts")
    Call<ApiResponse<GroupReviewResponse>>  createNotice(
            @Header("Authorization") String token,
            @Path("groupId") int groupId,
            @Body CreateGroupNotice body
    );

    @POST("/api/groups/{groupId}/posts")
    Call<ApiResponse<GroupReviewResponse>> createRecommend(
            @Header("Authorization") String token,
            @Path("groupId") int groupId,
            @Body CreateGroupRecommend body
    );

    // 그룹 대표 이미지 업로드
    @Multipart
    @POST("/api/groups/{groupId}/image")
    Call<ApiResponse<String>> uploadGroupImage(
            @Header("Authorization") String token,
            @Path("groupId") int groupId,
            @Part MultipartBody.Part file
    );

}
