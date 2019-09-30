package xyz.jienan.xkcd.model.util

import okhttp3.ResponseBody
import org.junit.Test

class WhatIfArticleUtilTest {

    @Test(expected = NullPointerException::class)
    fun `get articles from null response body`() {
        WhatIfArticleUtil.getArticlesFromArchive(ResponseBody.create(null, ""))
    }
}