package com.magic.chatai;

import java.util.List;
import java.util.Map;

public class x_FirestoreData {
    private int point;
    private String createDate;
    private List<Map<String, Object>> post;


    public x_FirestoreData(int point, String createDate, List<Map<String, Object>> post) {
        this.point = point;
        this.createDate = createDate;
        this.post = post;
    }

    public int getPoint() {
        return point;
    }

    public String getCreateDate() {
        return createDate;
    }

    public List<Map<String, Object>> getPost() {
        return post;
    }
}
