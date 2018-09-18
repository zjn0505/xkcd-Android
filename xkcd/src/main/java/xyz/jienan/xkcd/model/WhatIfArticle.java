package xyz.jienan.xkcd.model;

import com.google.gson.annotations.SerializedName;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Transient;

@Entity
public class WhatIfArticle {

    @Id(assignable = true)
    public long num;

    public String title;

    public String featureImg;

    public String content;

    // transient to make it invisible in Gson
    public transient long date;

    // transient to make it invisible in ObjectBox
    @Transient
    @SerializedName("date")
    public String dateInString;

    public boolean isFavorite = false;

    public boolean hasThumbed = false;

    public long thumbCount;

    public boolean hasRead = false;
}
