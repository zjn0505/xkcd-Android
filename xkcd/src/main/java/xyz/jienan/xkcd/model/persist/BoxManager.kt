package xyz.jienan.xkcd.model.persist

import io.objectbox.Box
import io.objectbox.BoxStore
import org.jsoup.internal.StringUtil
import xyz.jienan.xkcd.Const.PREF_WHAT_IF_SEARCH_ALL
import xyz.jienan.xkcd.Const.PREF_WHAT_IF_SEARCH_INCLUDE_READ
import xyz.jienan.xkcd.model.*
import java.util.*


object BoxManager {

    lateinit var xkcdBox: Box<XkcdPic>
        private set

    lateinit var whatIfBox: Box<WhatIfArticle>
        private set

    private lateinit var extraBox: Box<ExtraComics>

    fun init(boxStore: BoxStore?) {
        xkcdBox = boxStore!!.boxFor(XkcdPic::class.java)
        whatIfBox = boxStore.boxFor(WhatIfArticle::class.java)
        extraBox = boxStore.boxFor(ExtraComics::class.java)
    }

    val favXkcd: List<XkcdPic>
        get() {
            val queryFav = xkcdBox.query().equal(XkcdPic_.isFavorite, true).build()
            return queryFav.find()
        }

    val untouchedComicsList: List<XkcdPic>
        get() {
            val query = xkcdBox.query().notEqual(XkcdPic_.isFavorite, true)
                    .and().notEqual(XkcdPic_.hasThumbed, true).build()
            return query.find()
        }

    /********** what if  */

    val whatIfArchive: List<WhatIfArticle>?
        get() = whatIfBox.all

    val favWhatIf: List<WhatIfArticle>
        get() {
            val queryFav = whatIfBox.query().equal(WhatIfArticle_.isFavorite, true).build()
            return queryFav.find()
        }

    val untouchedArticleList: List<WhatIfArticle>
        get() {
            val query = whatIfBox.query().notEqual(WhatIfArticle_.isFavorite, true)
                    .and().notEqual(WhatIfArticle_.hasThumbed, true).build()
            return query.find()
        }

    val extraList: List<ExtraComics>
        get() = if (extraBox.isEmpty) ArrayList() else extraBox.all

    /********** xkcd  */

    fun getXkcd(index: Long): XkcdPic? {
        return xkcdBox.get(index)
    }

    fun saveXkcd(xkcdPic: XkcdPic) {
        xkcdBox.put(xkcdPic)
    }

    fun likeXkcd(index: Long): XkcdPic? {
        val xkcdPic = xkcdBox.get(index)
        if (xkcdPic != null) {
            xkcdPic.hasThumbed = true
            xkcdBox.put(xkcdPic)
        }
        return xkcdPic
    }

    fun favXkcd(index: Long, isFav: Boolean): XkcdPic? {
        val xkcdPic = xkcdBox.get(index)
        if (xkcdPic != null) {
            xkcdPic.isFavorite = isFav
            xkcdBox.put(xkcdPic)
        }
        return xkcdPic
    }

    fun getXkcdInRange(start: Long, end: Long): List<XkcdPic> {
        val query = xkcdBox.query().between(XkcdPic_.num, start, end).build()
        return query.find()
    }

    fun getValidXkcdInRange(start: Long, end: Long): List<XkcdPic> {
        val query = xkcdBox.query()
                .between(XkcdPic_.num, start, end)
                .and().greater(XkcdPic_.width, 0).build()
        return query.find()
    }

    fun updateAndSave(xkcdPics: List<XkcdPic>): List<XkcdPic> {
        for (pic in xkcdPics) {
            val xkcdPicInBox = xkcdBox.get(pic.num)
            if (xkcdPicInBox != null) {
                pic.isFavorite = xkcdPicInBox.isFavorite
                pic.hasThumbed = xkcdPicInBox.hasThumbed
                if (pic.width == 0 && xkcdPicInBox.width != 0) {
                    pic.width = xkcdPicInBox.width
                    pic.height = xkcdPicInBox.height
                }
            }
        }
        xkcdBox.put(xkcdPics)
        return xkcdPics
    }

    fun updateAndSave(xkcdPic: XkcdPic): XkcdPic = updateAndSave(listOf(xkcdPic))[0]

    fun getWhatIf(index: Long): WhatIfArticle? = whatIfBox.get(index)

    fun updateAndSaveWhatIf(whatIfArticles: MutableList<WhatIfArticle>): List<WhatIfArticle> {
        for (article in whatIfArticles) {
            val articleInBox = whatIfBox.get(article.num)
            if (articleInBox?.content != null) {
                whatIfArticles[article.num.toInt() - 1] = articleInBox
            }
        }
        whatIfBox.put(whatIfArticles)
        return whatIfArticles
    }

    fun updateAndSaveWhatIf(index: Long, content: String): WhatIfArticle? {
        val article = whatIfBox.get(index)
        if (article != null) {
            article.content = content
            whatIfBox.put(article)
        }
        return article
    }

    fun searchWhatIf(query: String, option: String): MutableList<WhatIfArticle> {
        var builder = whatIfBox.query().contains(WhatIfArticle_.title, query)
        if (StringUtil.isNumeric(query)) {
            builder = builder.or().equal(WhatIfArticle_.num, query.toLong())
        }
        when (option) {
            PREF_WHAT_IF_SEARCH_INCLUDE_READ -> {
                // ObjectBox is not good at such query filters
                val listInTitle = builder.build().find()
                val listInReadContent = whatIfBox.query()
                        .contains(WhatIfArticle_.content, query)
                        .build()
                        .find()
                        .filter { it.hasThumbed || it.isFavorite }
                return listInTitle.union(listInReadContent).toMutableList()
            }
            PREF_WHAT_IF_SEARCH_ALL -> builder = builder.or()
                    .contains(WhatIfArticle_.content, query)
        }
        return builder.build().find()
    }

    fun likeWhatIf(index: Long): WhatIfArticle? {
        val article = whatIfBox.get(index)
        if (article != null) {
            article.hasThumbed = true
            whatIfBox.put(article)
        }
        return article
    }

    fun favWhatIf(index: Long, isFav: Boolean): WhatIfArticle? {
        val article = whatIfBox.get(index)
        if (article != null) {
            article.isFavorite = isFav
            whatIfBox.put(article)
        }
        return article
    }

    /********** extra  */

    fun saveExtras(extraComics: List<ExtraComics>) {
        extraBox.put(extraComics)
    }

    fun getExtra(index: Int): ExtraComics = extraBox.get(index.toLong())

    fun loadExtraExplain(url: String) =
            extraBox.query().equal(ExtraComics_.explainUrl, url).build().findFirst()?.explainContent

    fun updateExtra(url: String, explainContent: String) {
        val extraComics = extraBox.query().equal(ExtraComics_.explainUrl, url).build().findFirst()
        if (extraComics != null) {
            extraComics.explainContent = explainContent
            extraBox.put(extraComics)
        }
    }
}
