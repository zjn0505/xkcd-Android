package xyz.jienan.xkcd.model

import com.google.gson.annotations.SerializedName
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class WhatIfArticle constructor(
        @Id(assignable = true)
        var num: Long = 0,
        var title: String = "",
        var featureImg: String? = null,
        var content: String? = null,
        @Transient
        var date: Long = 0L,
        @SerializedName("date")
        var dateInString: String = "",
        var isFavorite: Boolean = false,
        var hasThumbed: Boolean = false,
        var thumbCount: Long = 0
)

