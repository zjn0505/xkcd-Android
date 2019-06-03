package xyz.jienan.xkcd.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.MenuItem
import androidx.core.text.HtmlCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header.view.*
import timber.log.Timber
import xyz.jienan.xkcd.Const.*
import xyz.jienan.xkcd.R
import xyz.jienan.xkcd.base.BaseActivity
import xyz.jienan.xkcd.comics.fragment.ComicsMainFragment
import xyz.jienan.xkcd.extra.fragment.ExtraMainFragment
import xyz.jienan.xkcd.home.base.ContentMainBaseFragment
import xyz.jienan.xkcd.model.QuoteModel
import xyz.jienan.xkcd.model.persist.SharedPrefManager
import xyz.jienan.xkcd.settings.PreferenceActivity
import xyz.jienan.xkcd.whatif.WhatIfFastLoadService
import xyz.jienan.xkcd.whatif.fragment.WhatIfMainFragment

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val tvQuote by lazy { navigationView.getHeaderView(0).tvQuote }

    private val tvSubQuote by lazy { navigationView.getHeaderView(0).tvSubQuote }

    private val fragmentManager = supportFragmentManager

    private val compositeDisposable = CompositeDisposable()

    private val visibleFragment: Fragment?
        get() {
            return fragmentManager.fragments.firstOrNull { it.isVisible }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // init view
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationView.setNavigationItemSelectedListener(this)
        navigationView.itemIconTintList = null
        if (savedInstanceState == null) {

            val fragmentTag = if (intent?.getIntExtra(INDEX_ON_NOTI_INTENT, 0) != 0) {
                intent?.getStringExtra(LANDING_TYPE) ?: TAG_XKCD
            } else {
                SharedPrefManager.landingType
            }

            openFragment(fragmentTag)
        }
        // load data - daily quote & fast load xkcd comics
        getDailyQuote()
        fastLoad()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_SETTINGS && resultCode == Activity.RESULT_OK) {
            recreate()
        }
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent?) {
        setIntent(intent)
        super.onNewIntent(intent)
        if (intent != null && intent.getIntExtra(INDEX_ON_NOTI_INTENT, 0) != 0) {
            val fragmentTag = intent.getStringExtra(LANDING_TYPE)
            if (!openFragment(fragmentTag)) {
                val fragment = visibleFragment as ContentMainBaseFragment?
                if (fragment != null) {
                    val size = intent.getIntExtra(INDEX_ON_NOTI_INTENT, 0)
                    fragment.expand(size)
                    fragment.scrollViewPagerToItem(size - 1, false)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_comics -> {
                openFragment(TAG_XKCD)
                logUXEvent(FIRE_NAVI_XKCD)
            }
            R.id.nav_whatif -> {
                openFragment(TAG_WHAT_IF)
                logUXEvent(FIRE_NAVI_WHAT_IF)
            }
            R.id.nav_extra -> {
                openFragment(TAG_EXTRA)
                logUXEvent(FIRE_NAVI_EXTRA)
            }
            R.id.nav_setting -> {
                val settingsIntent = Intent(this, PreferenceActivity::class.java)
                startActivityForResult(settingsIntent, REQ_SETTINGS)
                logUXEvent(FIRE_SETTING_MENU)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            drawerLayout!!.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }

    fun toggleDrawerAvailability(enable: Boolean) {
        drawerLayout!!.setDrawerLockMode(if (enable) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun openFragment(fragmentTag: String): Boolean {
        var targetFragment = fragmentManager.findFragmentByTag(fragmentTag)
        if (targetFragment == null) {
            targetFragment = when(fragmentTag) {
                TAG_XKCD -> ComicsMainFragment()
                TAG_WHAT_IF -> WhatIfMainFragment()
                TAG_EXTRA ->  ExtraMainFragment()
                else -> ComicsMainFragment()
            }
        }
        SharedPrefManager.landingType = fragmentTag
        if (visibleFragment !== targetFragment) {
            fragmentManager.beginTransaction().replace(R.id.container, targetFragment, fragmentTag).commit()
            return true
        }
        return false
    }

    private fun getDailyQuote() {
        QuoteModel.getInstance()
            .getQuoteOfTheDay(SharedPrefManager.previousQuote)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { SharedPrefManager.saveNewQuote(it) }
            .subscribe({ quote ->
                val header = navigationView.getHeaderView(0)
                if (header != null && tvQuote != null && tvSubQuote != null) {
                    @SuppressLint("SetTextI18n")
                    tvQuote.text = "\"${HtmlCompat.fromHtml(quote.content, HtmlCompat.FROM_HTML_MODE_LEGACY)}\""
                    tvSubQuote.text = getString(R.string.quote_sub_text, quote.author, quote.source[0], quote.num)
                    header.setOnClickListener {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.putExtra(INDEX_ON_NOTI_INTENT, quote.num)
                        intent.putExtra(LANDING_TYPE, quote.source)
                        startActivity(intent)
                        drawerLayout.closeDrawer(GravityCompat.START)
                    }
                }
            }, { e -> Timber.e(e, "failed to get daily quote") })
                .also { compositeDisposable.add(it) }
    }

    private fun fastLoad() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        val isUnlimitedConnected = (activeNetwork != null
                && activeNetwork.isConnected
                && !connectivityManager.isActiveNetworkMetered)
        if (isUnlimitedConnected) {
            val msgIntent = Intent(this, WhatIfFastLoadService::class.java)
            msgIntent.putExtra(WHAT_IF_LATEST_INDEX, SharedPrefManager.latestWhatIf)
            startService(msgIntent)
        }
    }

    companion object {

        private const val REQ_SETTINGS = 101
    }
}
