package com.example.meltingbooks.network.group;

import com.example.meltingbooks.group.menu.GroupMemberItem;

import java.io.Serializable;
import java.util.List;

public class GroupProfileResponse implements Serializable {
    private int id;
    private String name;
    private String description;
    private String groupImageUrl;
    private int ownerId;
    private String category;
    private int memberCount;
    private String createdAt;

    // members 추가
    private List<GroupMemberItem> members;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getGroupImageUrl() { return groupImageUrl; }
    public int getOwnerId() { return ownerId; }
    public String getCategory() { return category; }
    public int getMemberCount() { return memberCount; }
    public String getCreatedAt() { return createdAt; }
    public List<GroupMemberItem> getMembers() { return members; }



    // Setter
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setGroupImageUrl(String groupImageUrl) { this.groupImageUrl = groupImageUrl; }
    public void setCategory(String category) { this.category = category; }
    public void setMembers(List<GroupMemberItem> members) { this.members = members; }
}
