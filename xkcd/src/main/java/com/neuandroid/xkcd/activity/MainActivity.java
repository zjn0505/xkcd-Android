package com.neuandroid.xkcd.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.neuandroid.xkcd.NumberPickerDialogFragment;
import com.neuandroid.xkcd.R;
import com.neuandroid.xkcd.SimpleInfoDialogFragment;
import com.neuandroid.xkcd.XkcdPic;
import com.neuandroid.xkcd.network.NetworkService;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

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
    private SimpleInfoDialogFragment dialogFragment;
    private OkHttpClient mOkHttpClient;
    private ProgressListener progressListener;
    interface ProgressListener {
        void update(long bytesRead, long contentLength, boolean done);
    }
    private static class ProgressResponseBody extends ResponseBody {

        private final ResponseBody responseBody;
        private final ProgressListener progressListener;
        private BufferedSource bufferedSource;

        public ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener) {
            this.responseBody = responseBody;
            this.progressListener = progressListener;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            return responseBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                    return bytesRead;
                }
            };
        }
    }

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
                dialogFragment = new SimpleInfoDialogFragment();
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

        progressListener = new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
                int progress = (int) ((100 * bytesRead) / contentLength);

                Log.v(TAG, "Progress: " + progress + "%");

            }
        };
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.addNetworkInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();
            }
        });
        mOkHttpClient = httpClientBuilder.build();
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
        pbLoading.setVisibility(View.VISIBLE);
        Observable<XkcdPic> xkcdPicObservable = NetworkService.getXkcdAPI().getLatest();
        xkcdPicObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(xkcdPicObserver);
    }

    Observer<XkcdPic> xkcdPicObserver = new Observer<XkcdPic>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(XkcdPic xkcdPic) {
            if (0 == latestIndex) {
                latestIndex = xkcdPic.num;
            }
            renderXkcdPic(xkcdPic);
        }

        @Override
        public void onError(Throwable e) {
            pbLoading.setVisibility(View.GONE);
        }

        @Override
        public void onComplete() {

        }
    };

    /**
     * Request a specific xkcd picture
     * @param id the id of xkcd picture
     */
    private void loadXkcdPicById(int id) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setSubtitle(String.valueOf(id));

        pbLoading.setVisibility(View.VISIBLE);
        Observable<XkcdPic> xkcdPicObservable = NetworkService.getXkcdAPI().getComics(String.valueOf(id));
        xkcdPicObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(xkcdPicObserver);
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
        Glide.get(this)
                .register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(mOkHttpClient));
        Glide.with(this.getApplicationContext()).load(xPic.getImg()).diskCacheStrategy(DiskCacheStrategy.SOURCE).listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                pbLoading.setVisibility(View.GONE);
                Log.e(TAG, "image loading failed " + xPic.getImg());
                Log.d(TAG, "image loading fallback " + xPic.getRawImg());
                Glide.with(MainActivity.this).load(xPic.getRawImg()).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(ivXkcdPic);
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                pbLoading.setVisibility(View.GONE);
                tvTitle.setText(xPic.num + ". " + xPic.getTitle());
                tvCreateDate.setText("created on " + xPic.year + "." + xPic.month + "." + xPic.day);
                if (tvDescription != null) {
                    tvDescription.setText(xPic.alt);
                }
                return false;

            }
        }).into(ivXkcdPic);
        currentPic = xPic;
        Log.d(TAG, "Pic to be loaded: " + xPic.getImg());

    }


    /**
     * Launch a new Activity to show the pic in full screen mode
     */
    private void launchDetailPageActivity() {

        if (currentPic == null || TextUtils.isEmpty(currentPic.getImg())) {
            return;
        }
        Intent intent = new Intent(MainActivity.this, ImageDetailPageActivity.class);
        intent.putExtra("URL", currentPic.getImg());
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

        @SuppressLint("StaticFieldLeak")
        @Override
        public void onExplainMoreClick(final SimpleInfoDialogFragment.ExplainingCallback explainingCallback) {
            new AsyncTask<Void, Void, String>() {

                private boolean isH2ByType(Element element, String type) {

                    for (Node child : element.childNodes()) {
                        if (type.equals(child.attr("id"))) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                protected String doInBackground(Void... voids) {
                    Document doc = null;
                    try {
                        doc = Jsoup.connect("http://www.explainxkcd.com/wiki/index.php/" + currentPic.num).get();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (doc == null) {
                        return null;
                    }
                    Elements newsHeadlines = doc.select("h2");
                    for (Element headline : newsHeadlines) {
                        if (isH2ByType(headline, "Explanation")) {
                            Element element = headline.nextElementSibling();
                            StringBuilder htmlResult = new StringBuilder();
                            while (!isH2ByType(element, "Transcript")) {
                                if (element.tagName().equals("p"))
                                    htmlResult.append(element.toString());
                                element = element.nextElementSibling();
                            }
                            return htmlResult.toString();
                        }

                    }
                    return null;
                }

                @Override
                protected void onPostExecute(String result) {
                    if (result != null) {
                        if (dialogFragment != null && dialogFragment.isAdded()) {
                            explainingCallback.explanationLoaded(result);
                            return;
                        }
                    }
                    explainingCallback.explanationFailed();
                }
            }.execute();
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
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Come and check this funny image I got from xkcd. \n " + currentPic.getImg());
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

            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                loadNextPic();
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                //left to right flip
                loadPreviousPic();
            }
            isFling = true;

            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }
    }
}
