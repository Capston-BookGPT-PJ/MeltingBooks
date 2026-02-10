package com.example.meltingbooks.network;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.meltingbooks.feed.FeedActivity;
import com.example.meltingbooks.feed.FeedDetailActivity;
import com.example.meltingbooks.group.GroupDetailActivity;
import com.example.meltingbooks.group.GroupFeedActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("FCM", "Refreshed token: " + token);

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        prefs.edit().putString("fcmToken", token).apply();

        int userId = prefs.getInt("userId", -1);
        String jwt = prefs.getString("jwt", null);

        if (userId != -1 && jwt != null) {
            sendTokenToServer(userId, jwt, token);
        } else {
            Log.d("FCM", "No logged-in user to update token on server yet");
        }
    }

    private void sendTokenToServer(int userId, String jwt, String fcmToken) {
        ApiService apiService = ApiClient.getClient(jwt).create(ApiService.class);
        TokenRequestBody body = new TokenRequestBody(fcmToken, Build.MODEL);

        Call<Void> call = apiService.registerToken("Bearer " + jwt, userId, body);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d("FCM", response.isSuccessful() ? "Token updated on server" :
                        "Token update failed: " + response.code());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.w("FCM", "Token update failed", t);
            }
        });
    }

   /* @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("FCM", "Message received: " + remoteMessage.getData().toString());

        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        if (title != null && body != null) {
            showNotification(title, body);
        }
    }

    private void showNotification(String title, String body) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "meltingbooks_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "MeltingBooks 알림",
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }*/

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("FCM", "Message received: " + remoteMessage.getData());

        String type = remoteMessage.getData().get("type");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        // 공통 데이터
        String postId = remoteMessage.getData().get("postId");
        String groupId = remoteMessage.getData().get("groupId");
        String groupName = remoteMessage.getData().get("groupName");

        Intent intent = null;

        try {
            if ("feed_like".equals(type) || "feed_comment".equals(type)) {
                // Feed 화면 이동
                intent = new Intent(this, FeedActivity.class);
                if (postId != null) intent.putExtra("postId", Integer.parseInt(postId));

            } else if ("group_new_post".equals(type)) {
                // 그룹 새글
                intent = new Intent(this, GroupFeedActivity.class);
                if (groupId != null) intent.putExtra("groupId", Integer.parseInt(groupId));
                intent.putExtra("groupName", groupName);

            } else if ("group_feed_like".equals(type) || "group_feed_comment".equals(type)) {
                // 그룹 피드 좋아요/댓글
                intent = new Intent(this, GroupFeedActivity.class);
                if (groupId != null) intent.putExtra("groupId", Integer.parseInt(groupId));
                if (postId != null) intent.putExtra("postId", Integer.parseInt(postId));
            }
        } catch (NumberFormatException e) {
            Log.e("FCM", "Failed to parse postId/groupId: " + e.getMessage());
            return;
        }

        if (intent == null) return;

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        showNotification(title, body, pendingIntent, type, postId, groupId);

    }

    private void showNotification(String title, String body, PendingIntent pendingIntent, String type, String postId, String groupId) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "meltingbooks_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "MeltingBooks 알림",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // 알림 ID 고정
        int notificationId;
        if (postId != null) {
            notificationId = Integer.parseInt(postId);
        } else if (groupId != null) {
            notificationId = Integer.parseInt(groupId);
        } else if (type != null) {
            notificationId = type.hashCode();
        } else {
            notificationId = 0;
        }

        manager.notify(notificationId, builder.build());
    }

}
