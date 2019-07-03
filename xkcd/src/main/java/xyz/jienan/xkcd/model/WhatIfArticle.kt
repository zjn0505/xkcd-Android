package xyz.jienan.xkcd.model

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class WhatIfArticle constructor(
        @Id(assignable = true)
        var num: Long = 0,
        var title: String,
        var featureImg: String?,
        var content: String? = null,
        var date: Long,
        var isFavorite: Boolean = false,
        var hasThumbed: Boolean = false,
        var thumbCount: Long = 0
)

