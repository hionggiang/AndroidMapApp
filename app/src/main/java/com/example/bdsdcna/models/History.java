package com.example.bdsdcna.models;

public class History {

    private String historyId;

    // Loại thao tác
    // ADD_HOUSEHOLD
    // UPDATE_HOUSEHOLD
    // DELETE_HOUSEHOLD
    // ADD_MEMBER
    // UPDATE_MEMBER
    // DELETE_MEMBER
    // ADD_USER
    // UPDATE_USER
    // DELETE_USER
    // LOGIN
    // CHANGE_PASSWORD
    // IMPORT_EXCEL
    private String action;

    // ID hộ dân liên quan
    private String householdId;

    // ID thành viên liên quan
    private String memberId;

    // ID user bị tác động
    private String targetUserId;

    // Tên đối tượng bị tác động
    private String targetName;

    // Tên trường thay đổi
    private String field;

    // Giá trị cũ
    private String oldValue;

    // Giá trị mới
    private String newValue;

    // Người thực hiện
    private String userId;

    private String userName;

    private String userEmail;

    // Thời gian
    private long timestamp;

    public History() {
        // Firebase cần constructor rỗng
    }
    // Loại đối tượng bị tác động
// household | member | user | notification | system
    private String targetType;

    // Địa chỉ/IP hoặc thiết bị (nếu muốn lưu)
    private String device;

    // Ghi chú
    private String description;

    public History(String historyId, String action, String householdId, String memberId, String targetUserId, String targetName, String field, String oldValue, String newValue, String userId, String userName, String userEmail, long timestamp, String targetType, String device, String description) {
        this.historyId = historyId;
        this.action = action;
        this.householdId = householdId;
        this.memberId = memberId;
        this.targetUserId = targetUserId;
        this.targetName = targetName;
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.timestamp = timestamp;
        this.targetType = targetType;
        this.device = device;
        this.description = description;
    }

    public String getHistoryId() {
        return historyId;
    }

    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getHouseholdId() {
        return householdId;
    }

    public void setHouseholdId(String householdId) {
        this.householdId = householdId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}