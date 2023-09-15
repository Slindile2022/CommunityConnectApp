package com.example.communityconnect;

public class ListOfDonatedPeopleData {

    private String userNames;

    private String donatedAmount;

    private String donationID;

    private  String userId;

    private  String timeStamp;



    public ListOfDonatedPeopleData() {
    }

    public ListOfDonatedPeopleData(String userNames, String donatedAmount, String donationID, String userId, String timeStamp) {
        this.userNames = userNames;
        this.donatedAmount = donatedAmount;
        this.donationID = donationID;
        this.userId = userId;
        this.timeStamp = timeStamp;
    }

    public String getUserNames() {
        return userNames;
    }

    public void setUserNames(String userNames) {
        this.userNames = userNames;
    }

    public String getDonatedAmount() {
        return donatedAmount;
    }

    public void setDonatedAmount(String donatedAmount) {
        this.donatedAmount = donatedAmount;
    }

    public String getDonationID() {
        return donationID;
    }

    public void setDonationID(String donationID) {
        this.donationID = donationID;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
