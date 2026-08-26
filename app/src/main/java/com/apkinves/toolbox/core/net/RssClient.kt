package com.apkinves.toolbox.core.net

import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.net.HttpURLConnection
import java.net.URL

object RssClient {

    data class FeedItem(val title: String, val link: String)
    data class FeedReport(val feedTitle: String?, val items: List<FeedItem>)

    suspend fun fetch(url: String): Result<FeedReport> = withContext(Dispatchers.IO) {
        runCatching {
            val target = if (url.startsWith("http")) url else "https://$url"
            val conn = URL(target).openConnection() as HttpURLConnection
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            val stream = conn.inputStream
            try {
                parse(stream)
            } finally {
                stream.close()
                conn.disconnect()
            }
        }
    }

    private fun parse(input: java.io.InputStream): FeedReport {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(input, null)

        var feedTitle: String? = null
        val items = mutableListOf<FeedItem>()
        var inItem = false
        var itemTitle: String? = null
        var itemLink: String? = null
        var depth = 0

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val name = parser.name.lowercase()
                    depth++
                    if (name == "item" || name == "entry") {
                        inItem = true
                        itemTitle = null
                        itemLink = null
                    } else if (name == "title") {
                        val text = readText(parser)
                        if (inItem) itemTitle = text else if (feedTitle == null) feedTitle = text
                    } else if (name == "link") {
                        val href = parser.getAttributeValue(null, "href")
                        val text = if (href != null) href else readText(parser)
                        if (inItem) itemLink = text
                    }
                }
                XmlPullParser.END_TAG -> {
                    val name = parser.name.lowercase()
                    if ((name == "item" || name == "entry") && inItem) {
                        items.add(FeedItem(itemTitle ?: "(sin título)", itemLink ?: ""))
                        inItem = false
                    }
                    depth--
                }
            }
            if (items.size >= 30) break
            eventType = parser.next()
        }
        return FeedReport(feedTitle, items)
    }

    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result.trim()
    }
}
