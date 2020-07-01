package com.ahmed.eid.whatsapp;

public class MassageModel {

    String from,  massage, type, to, date, time;

    public MassageModel() {
    }

    public MassageModel(String from, String massage, String type, String to, String date, String time) {
        this.from = from;
        this.massage = massage;
        this.type = type;
        this.to = to;
        this.time = time;
        this.date = date;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getmText() {
        return massage;
    }

    public void setmText(String mText) {
        this.massage = mText;
    }

    public String getmType() {
        return type;
    }

    public void setmType(String mType) {
        this.type = mType;
    }

    public String getmFrom() {
        return from;
    }

    public void setmFrom(String mFrom) {
        this.from = mFrom;
    }
}
