package com.example.myapplication 

import android.util.Xml
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

@Entity(tableName = "favorites")
data class BlogItem(
    val title: String,
    val description: String,
    
    @PrimaryKey val link: String,
    val imageUrl: String?
)


fun String.removeHtmlTags(): String {
    
    return this.replace(Regex("<.*?>", RegexOption.DOT_MATCHES_ALL), "")
        .replace("&nbsp;", " ")
        .trim()
}


class RssParser {
    fun parse(inputStream: InputStream): List<BlogItem> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)
        parser.nextTag()
        return readFeed(parser)
    }

    private fun readFeed(parser: XmlPullParser): List<BlogItem> {
        val entries = mutableListOf<BlogItem>()
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            
            if (parser.name == "item" || parser.name == "entry") {
                entries.add(readItem(parser))
            }
        }
        return entries
    }

    private fun readItem(parser: XmlPullParser): BlogItem {
        var title = ""
        var description = ""
        var link = ""
        var imageUrl: String? = null

        while (parser.next() != XmlPullParser.END_TAG || (parser.name != "item" && parser.name != "entry")) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            val name = parser.name
            when (name) {
                "title" -> title = readText(parser)
                "link" -> link = readText(parser) 

                "description", "content:encoded", "content" -> {
                    val rawHtml = readText(parser)
                    
                    if (name == "description") description = rawHtml

                    
                    if (imageUrl == null) {
                        val imgRegex = Regex("""src\s*=\s*['"]([^'"]+)['"]""")
                        val match = imgRegex.find(rawHtml)
                        if (match != null) {
                            imageUrl = match.groupValues[1]
                        }
                    }
                }

                
                "media:thumbnail", "media:content", "enclosure" -> {
                    val url = parser.getAttributeValue(null, "url")
                    if (imageUrl == null && url != null) {
                        imageUrl = url
                    }
                    skipGenericTag(parser)
                }

                else -> skip(parser)
            }
        }
        return BlogItem(title, description, link, imageUrl)
    }

    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) return
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    private fun skipGenericTag(parser: XmlPullParser) {
        if (parser.eventType == XmlPullParser.START_TAG && parser.isEmptyElementTag) {
            parser.next()
        } else {
            skip(parser)
        }
    }
}
