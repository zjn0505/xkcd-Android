package xyz.jienan.xkcd.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;

import xyz.jienan.xkcd.R;
import xyz.jienan.xkcd.XkcdPic;
import xyz.jienan.xkcd.activity.ImageDetailPageActivity;
import xyz.jienan.xkcd.Glide.ProgressTarget;
import xyz.jienan.xkcd.activity.MainActivity;
import xyz.jienan.xkcd.network.NetworkService;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

import static android.view.HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING;
import static android.view.HapticFeedbackConstants.LONG_PRESS;
import static xyz.jienan.xkcd.Const.GLIDE_TAG;

/**
 * Created by jienanzhang on 03/03/2018.
 */

public class SingleComicFragment extends Fragment implements IComicsCallback {

    private TextView tvTitle;
    private ImageView ivXkcdPic;
    private TextView tvCreateDate;
    private TextView tvDescription;
    private ProgressBar pbLoading;
    private SimpleInfoDialogFragment dialogFragment;
    
    private int id;
    private XkcdPic currentPic;

    private ProgressTarget<String, Bitmap> target;
    
    
    public static SingleComicFragment newInstance(int comicId) {
        SingleComicFragment fragment = new SingleComicFragment();
        Bundle args = new Bundle();
        args.putInt("id", comicId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        id = args.getInt("id");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_comic, container, false);
        pbLoading = view.findViewById(R.id.pb_loading);
        tvTitle = view.findViewById(R.id.tv_title);
        ivXkcdPic = view.findViewById(R.id.iv_xkcd_pic);
        tvCreateDate = view.findViewById(R.id.tv_create_date);
        tvDescription = view.findViewById(R.id.tv_description);
        ivXkcdPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchDetailPageActivity();
            }
        });
        ivXkcdPic.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                dialogFragment = new SimpleInfoDialogFragment();
                dialogFragment.setContent(currentPic.alt);
                dialogFragment.setListener(dialogListener);
                dialogFragment.show(getChildFragmentManager(), "AltInfoDialogFragment");
                v.performHapticFeedback(LONG_PRESS, FLAG_IGNORE_GLOBAL_SETTING);
                return true;
            }
        });
        initGlide();
        loadXkcdPic();
        return view;
    }

    @Override
    public XkcdPic getCurrentComic() {
        return currentPic;
    }

    private static class MyProgressTarget<Z> extends ProgressTarget<String, Z> {
        private final ProgressBar progress;
        private final ImageView image;
        public MyProgressTarget(Target<Z> target, ProgressBar progress, ImageView image) {
            super(target);
            this.progress = progress;
            this.image = image;
        }

        @Override public float getGranualityPercentage() {
            return 0.1f; // this matches the format string for #text below
        }

        @Override protected void onConnecting() {
            progress.setIndeterminate(true);
            progress.setVisibility(View.VISIBLE);
            image.setImageLevel(0);
        }
        @Override protected void onDownloading(long bytesRead, long expectedLength) {
            progress.setIndeterminate(false);
            progress.setProgress((int)(100 * bytesRead / expectedLength));
            image.setImageLevel((int)(10000 * bytesRead / expectedLength));
        }
        @Override protected void onDownloaded() {
            progress.setIndeterminate(true);
            image.setImageLevel(10000);
        }
        @Override protected void onDelivered() {
            progress.setVisibility(View.INVISIBLE);
            image.setImageLevel(0); // reset ImageView default
        }
    }

    private void initGlide() {
        target =  new MyProgressTarget<>(new BitmapImageViewTarget(ivXkcdPic), pbLoading, ivXkcdPic);
    }


    /**
     * Launch a new Activity to show the pic in full screen mode
     */
    private void launchDetailPageActivity() {
        if (currentPic == null || TextUtils.isEmpty(currentPic.getImg())) {
            return;
        }
        Intent intent = new Intent(getActivity(), ImageDetailPageActivity.class);
        intent.putExtra("URL", currentPic.getImg());
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
    }

    /**
     * Request current xkcd picture
     */
    private void loadXkcdPic() {
        pbLoading.setVisibility(View.VISIBLE);
        Observable<XkcdPic> xkcdPicObservable = NetworkService.getXkcdAPI().getComics(String.valueOf(id));
        xkcdPicObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(xkcdPicObserver);
    }

    Observer<XkcdPic> xkcdPicObserver = new Observer<XkcdPic>() {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(XkcdPic xkcdPic) {
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
     * Render img, text on the view
     * @param xPic
     */
    private void renderXkcdPic(final XkcdPic xPic) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }

        target.setModel(xPic.getImg());
        Glide.with(getActivity()).load(xPic.getImg()).asBitmap().fitCenter().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(target);

        currentPic = xPic;
        Log.d(GLIDE_TAG, "Pic to be loaded: " + xPic.getImg());
        tvTitle.setText(xPic.num + ". " + xPic.getTitle());
        tvCreateDate.setText("created on " + xPic.year + "." + xPic.month + "." + xPic.day);
        if (tvDescription != null) {
            tvDescription.setText(xPic.alt);
        }

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
            String url = "http://www.explainxkcd.com/wiki/index.php/" + currentPic.num;
            Call<ResponseBody> call = NetworkService.getXkcdAPI().getExplain(url);
            if (((MainActivity)getActivity()).getMaxId() - currentPic.num < 3) {
                call = NetworkService.getXkcdAPI().getExplainWithShortCache(url);
            }
            call.enqueue(new Callback<ResponseBody>() {

                private boolean isH2ByType(Element element, String type) {
                    if (!"h2".equals(element.nodeName())) {
                        return false;
                    }
                    for (Node child : element.childNodes()) {
                        if (type.equals(child.attr("id"))) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                    Document doc = null;
                    try {
                        doc = Jsoup.parse(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (doc == null) {
                        explainingCallback.explanationFailed();
                        return;
                    }
                    Elements newsHeadlines = doc.select("h2");
                    if (newsHeadlines == null || newsHeadlines.size() == 0) {
                        explainingCallback.explanationFailed();
                        return;
                    }
                    for (Element headline : newsHeadlines) {
                        if (isH2ByType(headline, "Explanation")) {
                            Element element = headline.nextElementSibling();
                            StringBuilder htmlResult = new StringBuilder();
                            while (!"h2".equals(element.nodeName()) && !"h3".equals(element.nodeName())) {
                                if (element.tagName().equals("p"))
                                    if (element.toString().contains("<i>citation needed</i>")) {
                                        List<Node> nodes = new ArrayList<>();
                                        for (Node node : element.childNodes()) {
                                            if ("sup".equals(node.nodeName()) && node.toString().contains("<i>citation needed</i>")) {
                                                nodes.add(node);
                                            }
                                        }
                                        for (Node node : nodes) {
                                            node.remove();
                                        }
                                    }
                                htmlResult.append(element.toString());
                                element = element.nextElementSibling();
                            }
                            if (dialogFragment != null && dialogFragment.isAdded()) {
                                if (!htmlResult.toString().endsWith("</p>"))
                                    htmlResult.append("<br>");
                                explainingCallback.explanationLoaded(htmlResult.toString());
                                return;
                            }
                        }
                    }
                    explainingCallback.explanationFailed();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    explainingCallback.explanationFailed();
                }
            });
        }
    };


}
