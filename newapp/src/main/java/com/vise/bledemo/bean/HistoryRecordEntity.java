package com.vise.bledemo.bean;

public class HistoryRecordEntity {

    private String time;
    private String des;


    public HistoryRecordEntity(String time, String des) {
        this.time = time;
        this.des = des;
    }


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }
}
