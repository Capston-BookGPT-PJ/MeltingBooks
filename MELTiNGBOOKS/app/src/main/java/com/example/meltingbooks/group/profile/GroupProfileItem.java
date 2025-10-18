package com.example.meltingbooks.group.profile;

import java.io.Serializable;

public class GroupProfileItem implements Serializable {
    private String groupName;
    private String groupIntroImageUrl; // 이미지 URL 또는 로컬 경로
    private String groupCategory;
    private String groupIntroTitle;
    private String groupIntroDetail;

    public GroupProfileItem(String groupName, String groupIntroImageUrl, String groupCategory, String groupIntroTitle, String groupIntroDetail) {
        this.groupName = groupName;
        this.groupIntroImageUrl = groupIntroImageUrl;
        this.groupCategory = groupCategory;
        this.groupIntroTitle = groupIntroTitle;
        this.groupIntroDetail = groupIntroDetail;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupIntroImageUrl() {
        return groupIntroImageUrl;
    }

    public String getGroupCategory() {
        return groupCategory;
    }

    public String getGroupIntroTitle() {
        return groupIntroTitle;
    }

    public String getGroupIntroDetail() {
        return groupIntroDetail;
    }
}
