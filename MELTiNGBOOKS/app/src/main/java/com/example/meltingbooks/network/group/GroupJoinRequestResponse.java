package com.example.meltingbooks.network.group;

import java.util.List;

public class GroupJoinRequestResponse {
    private boolean success;
    private Data data;

    public boolean isSuccess() { return success; }
    public Data getData() { return data; }

    public static class Data {
        private List<JoinRequest> content;
        public List<JoinRequest> getContent() { return content; }
    }

    public static class JoinRequest {
        private int userId;
        private String nickname;
        private String username;
        private String profileImageUrl;
        private String joinStatus;
        private String joinedAt;

        public int getUserId() { return userId; }
        public String getNickname() { return nickname; }
        public String getUsername() { return username; }
        public String getProfileImageUrl() { return profileImageUrl; }
        public String getJoinStatus() { return joinStatus; }
        public String getJoinedAt() { return joinedAt; }
    }
}

