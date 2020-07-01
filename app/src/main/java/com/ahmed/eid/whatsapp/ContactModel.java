package com.ahmed.eid.whatsapp;

public class ContactModel {

    private String user_image, user_name,user_status ;

    public ContactModel(){}

    public ContactModel(  String user_name, String user_status) {
        this.user_name = user_name;
        this.user_status = user_status;
    }

    public ContactModel( String user_image, String user_name, String user_status) {
        this.user_image = user_image;
        this.user_name = user_name;
        this.user_status = user_status;
    }


    public String getName() {
        return user_name;
    }

    public void setName(String user_name) {
        this.user_name = user_name;
    }

    public String getStatus() {
        return user_status;
    }

    public void setStatus(String user_status) {
        this.user_status = user_status;
    }

    public String getImage() {
        return user_image;
    }

    public void setImage(String user_image) {
        this.user_image = user_image;
    }
}
