package xyz.jienan.xkcd

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.jienan.xkcd.home.MainActivity

@RunWith(AndroidJUnit4::class)
class OpenDetailComicTest {

    @get:Rule
    var activityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Test
    fun open_detail_comic() {
        onView(allOf(isDisplayed(), withId(R.id.ivXkcdPic))).perform(click())
    }

}