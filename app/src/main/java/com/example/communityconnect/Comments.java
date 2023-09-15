package com.example.communityconnect;

public class Comments {

    private String cId;

    private String comment;

    private String timestamp;

    private  String userName;

    private String userId;

    private String profileImage;

    private String userType;

    public Comments() {
    }

    public Comments(String cId, String comment, String timestamp, String userName, String userId, String profileImage, String userType) {
        this.cId = cId;
        this.comment = comment;
        this.timestamp = timestamp;
        this.userName = userName;
        this.userId = userId;
        this.profileImage = profileImage;
        this.userType = userType;
    }

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
