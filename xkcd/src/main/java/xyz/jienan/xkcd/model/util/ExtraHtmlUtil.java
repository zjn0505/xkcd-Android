package xyz.jienan.xkcd.model.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import xyz.jienan.xkcd.base.network.NetworkService;

public class ExtraHtmlUtil {

    private static final String XKCD_LINK = "http://www.xkcd.com/";

    public static Observable<String> getContentFromUrl(String url) {
        return NetworkService.INSTANCE.getXkcdAPI().getExplain(url)
                .map(responseBody -> Jsoup.parse(responseBody.string()))
                .map(doc -> {
                    doc.head().appendElement("link")
                            .attr("rel", "stylesheet")
                            .attr("type", "text/css")
                            .attr("href", "style.css");
                    Element tableElement = doc.body().selectFirst("table[width=90%]");
                    if (tableElement != null) {
                        tableElement.removeAttr("width");
                    }
                    Elements imgElements = doc.body().select("img");
                    for (Element imgElement : imgElements) {
                        String src = imgElement.attr("src");
                        if (src != null && src.startsWith("http://imgs.xkcd")) {
                            imgElement.attr("src", src.replaceFirst("http:", "https:"));
                        }
                        if (XKCD_LINK.equals(imgElement.parent().attr("href"))) {
                            imgElement.parent().removeAttr("href").attr("href", imgElement.attr("src"));
                        }
                    }

                    return doc;
                })
                .map(Node::outerHtml)

                .subscribeOn(Schedulers.io());
    }
}