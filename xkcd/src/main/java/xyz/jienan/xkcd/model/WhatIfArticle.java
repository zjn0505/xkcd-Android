package xyz.jienan.xkcd.model;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class WhatIfArticle {

    @Id(assignable = true)
    public long num;

    public String title;

    public String featureImg;

    public String content;

    public String date;

    public boolean isFavorite = false;

    public boolean hasThumbed = false;

    public long thumbCount;

    public boolean hasRead = false;
}
