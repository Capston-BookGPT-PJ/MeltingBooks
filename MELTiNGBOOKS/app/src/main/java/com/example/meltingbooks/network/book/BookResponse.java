package com.example.meltingbooks.network.book;

import java.util.List;

public class BookResponse {
    private boolean success;
    private List<Book> data;

    // Getter & Setter
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public List<Book> getData() { return data; }
    public void setData(List<Book> data) { this.data = data; }
}
