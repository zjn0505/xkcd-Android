package com.neuandroid.xkcd;

import java.io.Serializable;

/**
 * Created by jienanzhang on 09/07/2017.
 */
public class XkcdPic implements Serializable{
    public String year;
    public String month;
    public String day;
    public int num;
    public String title;
    private String img;
    public String alt;


    public String getImg() {
        return transformXkcdImgUrl();
    }

    public String getRawImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    private String transformXkcdImgUrl() {
        if (num >= 1084) {
            int insert = img.indexOf(".png");
            return img.substring(0, insert) + "_2x" + img.substring(insert, img.length());
        }
        return img;
    }
}
