package com.example.journeyjournal.Model;

public class UserModel {

    String email, password, uid;

    public UserModel(String email, String password, String uid) {
        this.email = email;
        this.password = password;
        this.uid = uid;
    }

    public UserModel(){

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
