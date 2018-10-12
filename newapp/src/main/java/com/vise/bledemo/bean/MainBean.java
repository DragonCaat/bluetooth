package com.vise.bledemo.bean;

/**
 * @author : Darcy
 * @Date ${Date}
 * @Description
 */
public class MainBean {

    private String macStr;
    private String name;

    public MainBean(String macStr,String name){
        this.macStr = macStr;
        this.name = name;
    }

    public String getMacStr() {
        return macStr;
    }

    public void setMacStr(String macStr) {
        this.macStr = macStr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
