package com.example.communityconnect;

public class PostDataEmergency {


    private Double postTitle;
    private String postDescription;
    private String timeStamp;
    private  String uid;
    private Double userName;
    private String profileImage;
    private String postImage;

    private String userType;

    private String pLikes;

    private String pComments;

    public PostDataEmergency() {
    }

    public PostDataEmergency(Double postTitle, String postDescription, String timeStamp, String uid, Double userName, String profileImage, String postImage, String userType, String pLikes, String pComments) {
        this.postTitle = postTitle;
        this.postDescription = postDescription;
        this.timeStamp = timeStamp;
        this.uid = uid;
        this.userName = userName;
        this.profileImage = profileImage;
        this.postImage = postImage;
        this.userType = userType;
        this.pLikes = pLikes;
        this.pComments = pComments;
    }

    public Double getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(Double postTitle) {
        this.postTitle = postTitle;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Double getUserName() {
        return userName;
    }

    public void setUserName(Double userName) {
        this.userName = userName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getpLikes() {
        return pLikes;
    }

    public void setpLikes(String pLikes) {
        this.pLikes = pLikes;
    }

    public String getpComments() {
        return pComments;
    }

    public void setpComments(String pComments) {
        this.pComments = pComments;
    }
}
