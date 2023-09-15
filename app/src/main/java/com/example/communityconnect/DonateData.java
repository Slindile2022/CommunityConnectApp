package com.example.communityconnect;

public class DonateData {
    private String donateTitle;
    private String donateDescription;
    private String timeStamp;
    private  String uid;
    private String userName;
    private String donateImage;

    private String userType;

    private String amount;


    public DonateData() {
    }


    public DonateData(String donateTitle, String donateDescription, String timeStamp, String uid, String userName, String donateImage, String userType, String amount) {
        this.donateTitle = donateTitle;
        this.donateDescription = donateDescription;
        this.timeStamp = timeStamp;
        this.uid = uid;
        this.userName = userName;
        this.donateImage = donateImage;
        this.userType = userType;
        this.amount = amount;
    }

    public String getDonateTitle() {
        return donateTitle;
    }

    public void setDonateTitle(String donateTitle) {
        this.donateTitle = donateTitle;
    }

    public String getDonateDescription() {
        return donateDescription;
    }

    public void setDonateDescription(String donateDescription) {
        this.donateDescription = donateDescription;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDonateImage() {
        return donateImage;
    }

    public void setDonateImage(String donateImage) {
        this.donateImage = donateImage;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
