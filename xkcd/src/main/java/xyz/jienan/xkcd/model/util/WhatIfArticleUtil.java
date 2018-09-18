package xyz.jienan.xkcd.model.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import xyz.jienan.xkcd.model.WhatIfArticle;

public class WhatIfArticleUtil {

    private static final String BASE_URI = "https://what-if.xkcd.com/";

    private static final String TEX_JS = "https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.4/latest.js?config=TeX-MML-AM_CHTML";

    public static List<WhatIfArticle> getArticlesFromArchive(ResponseBody responseBody) throws IOException {
        Document doc = Jsoup.parse(responseBody.string(), BASE_URI);
        Elements divArchive = doc.select("div#archive-wrapper");
        List<WhatIfArticle> articles = new ArrayList<>();
        for (Element element : divArchive.first().children()) {
            WhatIfArticle article = new WhatIfArticle();

            Element a = element.selectFirst("a[href]");
            String[] href = a.attr("href").split("/");
            article.num = Long.parseLong(href[href.length - 1]);
            article.featureImg = a.child(0).absUrl("src");
            article.title = element.selectFirst("h1.archive-title").child(0).html();
            article.date = element.selectFirst("h2.archive-date").html();

            articles.add(article);
        }
        return articles;
    }

    public static Document getArticleFromHtml(ResponseBody responseBody) throws IOException {
        final Document doc = Jsoup.parse(responseBody.string(), BASE_URI);
        final Elements elements = doc.select("article.entry");
        elements.remove(elements.get(0).children().first());

        final Elements imageElements = doc.select("img.illustration");
        for (Element element : imageElements) {
            element.attr("src", element.absUrl("src"));
        }

        final Elements pElements = doc.select("p");
        for (Element element : pElements) {
            if (element.html().split("\\[").length > 1) {
                element.attr("class", "latex");
            }
        }

        final Elements aElements = doc.select("a");
        for (Element element : aElements) {
            element.attr("href", element.absUrl("href"));
        }

        doc.head().html("");
        doc.head().appendElement("link")
                .attr("rel", "stylesheet")
                .attr("type", "text/css")
                .attr("href", "style.css");
        doc.head().appendElement("script")
                .attr("src", TEX_JS)
                .attr("async", "");
        doc.head().appendElement("script").attr("src", "LatexInterface.js");
        doc.head().appendElement("script").attr("src", "ImgInterface.js");
        doc.head().appendElement("script").attr("src", "RefInterface.js");
        doc.body().html(elements.html()).appendElement("p");
        return doc;
    }
}
