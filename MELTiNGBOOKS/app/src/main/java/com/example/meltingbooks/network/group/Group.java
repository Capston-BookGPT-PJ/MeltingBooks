package com.example.meltingbooks.network.group;

public class Group {
    private String name;
    private String description;
    private String groupImageUrl;
    private String category;

    public Group(String name, String description, String groupImageUrl, String category) {
        this.name = name;
        this.description = description;
        this.groupImageUrl = groupImageUrl;
        this.category = category;
    }

    // Getter (필요하면 추가)
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getGroupImageUrl() { return groupImageUrl; }
    public String getCategory() { return category; }
}

