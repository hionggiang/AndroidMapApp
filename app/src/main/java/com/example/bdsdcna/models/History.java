package com.example.bdsdcna.models;

public class History {
    private String historyId;
    private String action;
    private String householdId;
    private String memberId;
    private String field;
    private String oldValue;
    private String newValue;

    // Đã đồng bộ theo bảng users
    private String uid;
    private String email;
    private String name;

    // Đổi từ long/Long sang Object để nhận diện được ServerValue.TIMESTAMP
    private Object timestamp;

    // Constructor rỗng bắt buộc phải có cho Firebase
    public History() {
    }

    // ================= GETTER VÀ SETTER =================

    public String getHistoryId() { return historyId; }
    public void setHistoryId(String historyId) { this.historyId = historyId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getHouseholdId() { return householdId; }
    public void setHouseholdId(String householdId) { this.householdId = householdId; }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    // Các hàm getter/setter mới cho User
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // Hàm timestamp kiểu Object
    public Object getTimestamp() { return timestamp; }
    public void setTimestamp(Object timestamp) { this.timestamp = timestamp; }
}