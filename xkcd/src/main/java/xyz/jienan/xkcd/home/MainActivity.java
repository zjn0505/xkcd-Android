package xyz.jienan.xkcd.home;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.base.BaseActivity;
import xyz.jienan.xkcd.comics.fragment.ComicsMainFragment;
import xyz.jienan.xkcd.model.persist.SharedPrefManager;
import xyz.jienan.xkcd.settings.PreferenceActivity;
import xyz.jienan.xkcd.whatif.fragment.WhatIfMainFragment;

import static xyz.jienan.xkcd.Const.FIRE_SETTING_MENU;
import static xyz.jienan.xkcd.Const.INDEX_ON_NOTI_INTENT;
import static xyz.jienan.xkcd.Const.LANDING_TYPE;
import static xyz.jienan.xkcd.Const.TAG_WHAT_IF;
import static xyz.jienan.xkcd.Const.TAG_XKCD;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String OUTSTATE_FRAGMENT_TYPE = "outstate_fragment_type";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private final static int REQ_SETTINGS = 101;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @BindView(R.id.container)
    FrameLayout container;

    private FragmentManager fragmentManager = getSupportFragmentManager();

    private final SharedPrefManager sharedPrefManager = new SharedPrefManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            navigationView.setItemIconTintList(null);
        }
        if (savedInstanceState == null) {
            String fragmentTag = sharedPrefManager.getLandingType();
            if (getIntent() != null && getIntent().getIntExtra(INDEX_ON_NOTI_INTENT, 0) != 0) {
                fragmentTag = getIntent().getStringExtra(LANDING_TYPE);
            }
            openFragment(fragmentTag);
        }
    }

    //    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        Fragment fragment = getVisibleFragment();
//        if (fragment instanceof ComicsMainFragment) {
//            outState.putString(OUTSTATE_FRAGMENT_TYPE, TAG_XKCD);
//        } else if (fragment instanceof WhatIfMainFragment) {
//            outState.putString(OUTSTATE_FRAGMENT_TYPE, TAG_WHAT_IF);
//        }
//    }

    //    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        updateIndices(intent);
//        isFre = latestIndex == INVALID_ID;
//        if (latestIndex > INVALID_ID) {
//            adapter.setSize(latestIndex);
//            scrollViewPagerToItem(savedId > INVALID_ID ? savedId - 1 : latestIndex - 1, false);
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_SETTINGS) {
            if (resultCode == RESULT_OK) {
                recreate();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_comics:
                openFragment(TAG_XKCD);
                break;
            case R.id.nav_whatif:
                openFragment(TAG_WHAT_IF);
                break;
            case R.id.nav_setting:
                Intent settingsIntent = new Intent(this, PreferenceActivity.class);
                startActivityForResult(settingsIntent, REQ_SETTINGS);
                logUXEvent(FIRE_SETTING_MENU);
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    private void openFragment(String fragmentTag) {
        Fragment targetFragment = fragmentManager.findFragmentByTag(fragmentTag);
        if (targetFragment == null) {
            if (TAG_WHAT_IF.equals(fragmentTag)) {
                targetFragment = new WhatIfMainFragment();
            } else {
                targetFragment = new ComicsMainFragment();
            }
        }
        sharedPrefManager.setLandingType(fragmentTag);
        if (getVisibleFragment() != targetFragment) {
            fragmentManager.beginTransaction().replace(R.id.container, targetFragment, fragmentTag).commit();
        }
    }

    private Fragment getVisibleFragment(){
        List<Fragment> fragments = fragmentManager.getFragments();
        if(fragments != null){
            for(Fragment fragment : fragments){
                if(fragment != null && fragment.isVisible())
                    return fragment;
            }
        }
        return null;
    }
}
