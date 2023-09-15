package com.example.communityconnect;

public class Events {

    private String timeStamp;
    private String userName;
    private String userId;

    private  String userType;

    private String eventTitle;
    private String eventDescription;
    private String eventTime;
    private String eventDate;

    private  String eventStatus;

    private String pLikes;

    public Events() {
    }

    public Events(String timeStamp, String userName, String userId, String userType, String eventTitle, String eventDescription, String eventTime, String eventDate, String eventStatus, String pLikes) {
        this.timeStamp = timeStamp;
        this.userName = userName;
        this.userId = userId;
        this.userType = userType;
        this.eventTitle = eventTitle;
        this.eventDescription = eventDescription;
        this.eventTime = eventTime;
        this.eventDate = eventDate;
        this.eventStatus = eventStatus;
        this.pLikes = pLikes;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
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

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(String eventStatus) {
        this.eventStatus = eventStatus;
    }

    public String getpLikes() {
        return pLikes;
    }

    public void setpLikes(String pLikes) {
        this.pLikes = pLikes;
    }
}
