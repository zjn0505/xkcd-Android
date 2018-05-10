package xyz.jienan.xkcd;

import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;

import static xyz.jienan.xkcd.Const.URI_XKCD_EXPLAIN_EDIT;

public class XkcdExplainUtil {
    public static String getExplainFromHtml(ResponseBody responseBody, String url) throws IOException {
        Document doc = Jsoup.parse(responseBody.string());
        Elements newsHeadlines = doc.select("h2");
        for (Element headline : newsHeadlines) {
            if (isH2ByType(headline, "Explanation")) {
                Element element = headline.nextElementSibling();
                StringBuilder htmlResult = new StringBuilder();
                while (!"h2".equals(element.nodeName())) {
                    if ("h3".equals(element.nodeName()) || "h4".equals(element.nodeName())) {
                        Elements elements = element.getElementsByClass("editsection");
                        if (elements != null && elements.size() > 0) {
                            elements.remove();
                        }
                    }
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
                    for (Element child : element.getAllElements()) {
                        if ("a".equals(child.tagName()) && child.hasAttr("href")) {
                            String href = child.attr("href");
                            if (!TextUtils.isEmpty(href)) {
                                if (href.startsWith("/wiki")) {
                                    child.attr("href", "https://www.explainxkcd.com" + href);
                                } else if (href.startsWith("#")) {
                                    child.attr("href", url + href);
                                } else if (href.startsWith("//www.explainxkcd") && href.endsWith("action=edit")) {
                                    child.attr("href", URI_XKCD_EXPLAIN_EDIT);
                                }
                            }
                        }
                        if ("tbody".equals(child.tagName())) {
                            for (Element tableElement : child.children()) {
                                tableElement.append("<br />");
                            }
                        }
                    }
                    htmlResult.append(element.toString());
                    Node node = element.nextSibling();
                    while (!(node instanceof Element)) {
                        htmlResult.append(node.toString());
                        node = node.nextSibling();
                    }
                    element = (Element) node;
                }
                if (!htmlResult.toString().endsWith("</p>"))
                    htmlResult.append("<br>");
                return htmlResult.toString();
            }
        }
        return null;
    }

    public static boolean isXkcdImageLink(String url) {
        final String regex = "^https?://www\\.explainxkcd\\.com/wiki/index\\.php/\\d+(?!#)[:]?\\w+$";
        return url.matches(regex);
    }

    public static long getXkcdIdFromImageLink(String url) {
        final String regex = "^https?://www\\.explainxkcd\\.com/wiki/index\\.php/(\\d+).*$";
        Matcher matcher = Pattern.compile(regex).matcher(url);
        if (matcher.find()) {
            return Long.valueOf(matcher.group(1));
        } else {
            return 0;
        }
    }

    private static boolean isH2ByType(Element element, String type) {
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
}
