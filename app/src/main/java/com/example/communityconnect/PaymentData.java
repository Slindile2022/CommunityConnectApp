package com.example.communityconnect;

public class PaymentData {

    private String paymentReference;
    private String amount;
    private String userNames;
    private  String Uid;
    private String timeStamp;

    private String fileLink;

    private  String status;

    public PaymentData() {
    }


    public PaymentData(String paymentReference, String amount, String userNames, String uid, String timeStamp, String fileLink, String status) {
        this.paymentReference = paymentReference;
        this.amount = amount;
        this.userNames = userNames;
        Uid = uid;
        this.timeStamp = timeStamp;
        this.fileLink = fileLink;
        this.status = status;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getUserNames() {
        return userNames;
    }

    public void setUserNames(String userNames) {
        this.userNames = userNames;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getFileLink() {
        return fileLink;
    }

    public void setFileLink(String fileLink) {
        this.fileLink = fileLink;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
