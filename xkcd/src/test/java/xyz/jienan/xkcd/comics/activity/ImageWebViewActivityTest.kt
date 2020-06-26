package xyz.jienan.xkcd.comics.activity

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageWebViewActivityTest {

    private val url1663 = "https://zjn0505.github.io/xkcd-undressed/1663/#d23aed20-d830-11e9-8008-42010a8e0003"

    @Test
    fun check1663UrlParse() {
        val uuid = ImageWebViewActivity().extractUuidFrom1663url(url1663)
        assertEquals("d23aed20-d830-11e9-8008-42010a8e0003", uuid)
    }
}