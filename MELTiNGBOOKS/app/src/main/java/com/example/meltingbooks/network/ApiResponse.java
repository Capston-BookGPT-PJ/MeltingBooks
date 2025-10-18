package com.example.meltingbooks.network;

public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String error;

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public String getError() { return error; }
}

