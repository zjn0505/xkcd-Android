package com.neuandroid.xkcd.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.neuandroid.xkcd.NetworkUtils;
import com.neuandroid.xkcd.NumberPickerDialogFragment;
import com.neuandroid.xkcd.R;
import com.neuandroid.xkcd.SimpleInfoDialogFragment;
import com.neuandroid.xkcd.XkcdPic;
import com.neuandroid.xkcd.XkcdQueryTask;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static android.view.HapticFeedbackConstants.LONG_PRESS;

public class MainActivity extends AppCompatActivity {

    private TextView tvTitle;
    private ImageView ivXkcdPic;
    private TextView tvCreateDate;
    private TextView tvDescription;
    private ProgressBar pbLoading;

    private final static String TAG = "MainActivity";

    // Use this field to record the latest xkcd pic id
    private int latestIndex = 0;

    private XkcdPic currentPic;
    private GestureDetectorCompat mDetector;
    private boolean isFling;
    private static final String LOADED_XKCD_ID = "xkcd_id";
    private static final String LATEST_XKCD_ID = "xkcd_latest_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        ivXkcdPic = (ImageView) findViewById(R.id.iv_xkcd_pic);
        tvCreateDate = (TextView) findViewById(R.id.tv_create_date);
        tvDescription = (TextView) findViewById(R.id.tv_description);
        pbLoading = (ProgressBar) findViewById(R.id.pb_loading);
        ivXkcdPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFling)
                    launchDetailPageActivity();
                isFling = false;
            }
        });
        ivXkcdPic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SimpleInfoDialogFragment dialogFragment = new SimpleInfoDialogFragment();
                dialogFragment.setContent(currentPic.alt);
                dialogFragment.setListener(dialogListener);
                dialogFragment.show(getSupportFragmentManager(), "AltInfoDialogFragment");
                v.performHapticFeedback(LONG_PRESS, FLAG_IGNORE_GLOBAL_SETTING);
                return true;
            }
        });
        mDetector = new GestureDetectorCompat(this, new MyGestureListener());
        ivXkcdPic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDetector.onTouchEvent(event);
                return false;
            }
        });
        if (savedInstanceState != null) {
            int savedId = savedInstanceState.getInt(LOADED_XKCD_ID);
            loadXkcdPicById(savedId);
            latestIndex = savedInstanceState.getInt(LATEST_XKCD_ID);
        } else {
            loadXkcdPic();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentPic != null)
            outState.putInt(LOADED_XKCD_ID, currentPic.num);
            outState.putInt(LATEST_XKCD_ID, latestIndex);
    }

    /**
     * Request current xkcd picture
     */
    private void loadXkcdPic() {
        try {
            URL url = new URL(NetworkUtils.XKCD_QUERY_BASE_URL);
            new XkcdQueryTask(queryListener).execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Request a specific xkcd picture
     * @param id the id of xkcd picture
     */
    private void loadXkcdPicById(int id) {
        String queryUrl = String.format(NetworkUtils.XKCD_QUERY_BY_ID_URL, id);

        try {
            URL url = new URL(queryUrl);
            new XkcdQueryTask(queryListener).execute(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void loadPreviousPic() {
        if (currentPic != null) {
            int currentNum = currentPic.num;
            if (currentNum > 1) {
                loadXkcdPicById(currentNum - 1);
            } else {
                Toast.makeText(this, getString(R.string.toast_first), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadNextPic() {
        if (currentPic != null) {
            int currentNum = currentPic.num;
            if (currentNum < latestIndex) {
                loadXkcdPicById(currentNum + 1);
            } else {
                Toast.makeText(this, getString(R.string.toast_last), Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Render img, text on the view
     * @param xPic
     */
    private void renderXkcdPic(final XkcdPic xPic) {
        tvTitle.setText("");
        tvCreateDate.setText("");
        if (this.isFinishing()) {
            return;
        }
        Glide.with(this.getApplicationContext()).load(xPic.img).listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                pbLoading.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                pbLoading.setVisibility(View.GONE);
                tvTitle.setText(xPic.num + ". " + xPic.title);
                tvCreateDate.setText("created on " + xPic.year + "." + xPic.month + "." + xPic.day);
                if (tvDescription != null) {
                    tvDescription.setText(xPic.alt);
                }
                return false;

            }
        }).into(ivXkcdPic);
        currentPic = xPic;
        Log.d(TAG, "Pic to be loaded: " + xPic.img);

    }


    /**
     * Launch a new Activity to show the pic in full screen mode
     */
    private void launchDetailPageActivity() {

        if (currentPic == null || TextUtils.isEmpty(currentPic.img)) {
            return;
        }
        Intent intent = new Intent(MainActivity.this, ImageDetailPageActivity.class);
        intent.putExtra("URL", currentPic.img);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }


    private SimpleInfoDialogFragment.ISimpleInfoDialogListener dialogListener = new SimpleInfoDialogFragment.ISimpleInfoDialogListener() {
        @Override
        public void onPositiveClick() {
            // Do nothing
        }

        @Override
        public void onNegativeClick() {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.explainxkcd.com/wiki/index.php/" + currentPic.num));
            startActivity(browserIntent);
        }
    };


    private XkcdQueryTask.IAsyncTaskListener queryListener = new XkcdQueryTask.IAsyncTaskListener() {
        @Override
        public void onPreExecute() {
            pbLoading.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPostExecute(Serializable result) {
            if (result instanceof XkcdPic) {
                if (0 == latestIndex) {
                    latestIndex = ((XkcdPic) result).num;
                }
                renderXkcdPic((XkcdPic) result);
            } else {
                pbLoading.setVisibility(View.GONE);
            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_left:
                loadPreviousPic();
                break;
            case R.id.action_right:
                loadNextPic();
                break;
            case R.id.action_random:
                if (latestIndex == 0) {
                    loadXkcdPic();
                    break;
                }
                Random random = new Random();
                int randomId = random.nextInt(latestIndex + 1);
                loadXkcdPicById(randomId);
                break;
            case R.id.action_share:
                if (currentPic == null) {
                    break;
                }
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Come and check this funny image I got from xkcd. \n " + currentPic.img);
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_to)));

                break;
            case R.id.action_specific:
                if (currentPic == null) {
                    break;
                }
                NumberPickerDialogFragment pickerDialogFragment = new NumberPickerDialogFragment();
                pickerDialogFragment.setTitle(getString(R.string.dialog_pick_title));
                pickerDialogFragment.setContent(getString(R.string.dialog_pick_content));
                pickerDialogFragment.setNumberRange(1, latestIndex);
                pickerDialogFragment.setListener(new NumberPickerDialogFragment.INumberPickerDialogListener() {
                    @Override
                    public void onPositiveClick(int number) {
                        loadXkcdPicById(number);
                    }

                    @Override
                    public void onNegativeClick() {
                        // Do nothing
                    }
                });
                pickerDialogFragment.show(getSupportFragmentManager(), "IdPickerDialogFragment");

                break;
            case R.id.action_go_xkcd: {
                if (currentPic == null) {
                    break;
                }
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://xkcd.com/" + currentPic.num));
                startActivity(browserIntent);
                break;
            }
            case R.id.action_go_explain: {
                if (currentPic == null) {
                    break;
                }
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.explainxkcd.com/wiki/index.php/" + currentPic.num));
                startActivity(browserIntent);
                break;
            }
        }
        return true;
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_MAX_OFF_PATH = 250;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            Log.d("MyGestureListener", "trigger onFling");

            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                loadPreviousPic();
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                //left to right flip
                loadNextPic();
            }
            Log.d("MyGestureListener", "trigger onFling action");
            isFling = true;

            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.i("MyGestureListener", "single tap");
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.i("MyGestureListener", "single tap up");
            return super.onSingleTapUp(e);
        }
    }
}
