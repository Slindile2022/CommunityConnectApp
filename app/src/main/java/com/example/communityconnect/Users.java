package com.example.communityconnect;

public class Users {




    private String email;
    private String phone;
    private String name;

    private  String Uid;

    private String secondName;
    private String userType;
    private String address;
    private String profileImage;

    private String passwordChanged;

    private String amount;




    public Users() {

    }

    public Users(String email, String phone, String name, String uid, String secondName, String userType, String address, String profileImage, String passwordChanged, String amount) {
        this.email = email;
        this.phone = phone;
        this.name = name;
        Uid = uid;
        this.secondName = secondName;
        this.userType = userType;
        this.address = address;
        this.profileImage = profileImage;
        this.passwordChanged = passwordChanged;
        this.amount = amount;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getPasswordChanged() {
        return passwordChanged;
    }

    public void setPasswordChanged(String passwordChanged) {
        this.passwordChanged = passwordChanged;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
