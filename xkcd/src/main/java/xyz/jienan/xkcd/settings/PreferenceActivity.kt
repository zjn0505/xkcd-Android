package xyz.jienan.xkcd.settings

import android.os.Bundle
import android.view.MenuItem
import xyz.jienan.xkcd.base.BaseActivity

/**
 * Created by Jienan on 2018/3/9.
 */

class PreferenceActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment()).commit()
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return false
    }
}
