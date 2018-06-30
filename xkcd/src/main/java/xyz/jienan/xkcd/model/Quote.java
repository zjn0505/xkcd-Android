package xyz.jienan.xkcd.model;

import android.text.TextUtils;

import static xyz.jienan.xkcd.Const.TAG_XKCD;

public class Quote {

    /**
     * author : Stacey's dad
     * content : Get out while you still can
     * num : 61
     * source : xkcd
     */

    private String author = "Man in Chair";
    private String content = "Sudo make me a sandwich";
    private int num = 149;
    private String source = TAG_XKCD;
    private long timestamp = System.currentTimeMillis();

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Quote) {
            Quote q = (Quote) obj;
            return q.num == this.num
                    && !TextUtils.isEmpty(q.content)
                    && q.content.equals(this.content);
        }
        return false;
    }
}
