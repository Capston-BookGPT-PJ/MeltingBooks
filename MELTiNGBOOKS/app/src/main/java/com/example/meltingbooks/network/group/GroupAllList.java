package com.example.meltingbooks.network.group;

import java.util.List;

public class GroupAllList {
    private boolean success;
    private List<GroupProfileResponse> data;
    private String error;

    public boolean isSuccess() { return success; }
    public List<GroupProfileResponse> getData() { return data; }
    public String getError() { return error; }
}

