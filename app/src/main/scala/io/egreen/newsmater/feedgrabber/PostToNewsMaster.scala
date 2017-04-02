package io.egreen.newsmater.feedgrabber

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

import com.mashape.unirest.http.{HttpResponse, JsonNode, Unirest}
import com.rometools.modules.mediarss.{MediaEntryModuleImpl, MediaModule}
import com.rometools.modules.mediarss.types.{MediaGroup, Thumbnail}
import com.rometools.rome.feed.module.Module
import com.rometools.rome.feed.synd.SyndEntry
import io.egreen.newsmater.feedgrabber.xml.Feed
import org.json.JSONObject

import scala.collection.JavaConverters._

/**
  * Created by dewmal on 4/2/17.
  */
class PostToNewsMaster(syndEntry: SyndEntry, feed: Feed) {
  /** {
    * "seq": 0,
    * "link": null,
    * "source": null,
    * "content": {
    * "extended": "",
    * "brief": ""
    * },
    * "image": "",
    * "publishedDate": "2017-04-02T08:44:38.055Z",
    * "state": "draft",
    * "title": "",
    * "_id": "58e0ba0e68fdfe5264c9b59c"
    * } **/

  val postNews: JSONObject = new JSONObject();

  postNews.put("title", syndEntry.getTitle)
  postNews.put("link", syndEntry.getLink)
  var df: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset

  if (syndEntry.getPublishedDate != null) {
    postNews.put("publishedDate", df.format(syndEntry.getPublishedDate))
  } else if (syndEntry.getUpdatedDate != null) {
    postNews.put("publishedDate", df.format(syndEntry.getUpdatedDate))
  }
  postNews.put("state", "published")


  val source: JSONObject = new JSONObject()
  source.put("tag", feed.source.tag)
  source.put("name", feed.source.name)
  postNews.put("source", source)


  val content: JSONObject = new JSONObject()
  content.put("extended", "")
  if (syndEntry.getDescription != null) {
    content.put("brief", syndEntry.getDescription.getValue)
  } else if (syndEntry.getTitleEx != null) {
    content.put("brief", syndEntry.getTitleEx.getValue)
  }
  postNews.put("content", content)


  var mediaModule: MediaEntryModuleImpl = syndEntry.getModule(MediaModule.URI).asInstanceOf[MediaEntryModuleImpl]

  if (mediaModule != null && mediaModule.getMetadata != null) {
    for (thumb: Thumbnail <- mediaModule.getMetadata.getThumbnail) {
      postNews.put("image", thumb.getUrl)
    }
  }


  if (mediaModule != null && mediaModule.getMediaGroups != null) {
    for (mediaG: MediaGroup <- mediaModule.getMediaGroups) {

      mediaG.setDefaultContentIndex(0)


      postNews.put("image", mediaG.getContents.array(mediaG.getDefaultContentIndex).getReference)
    }
  }


  // Object to Json
  var postResponse: HttpResponse[String] = Unirest.post(Main.systemPro.getProperty("news.post.url", ""))
    .header("accept", "application/json")
    .header("Content-Type", "application/json")
    .body(postNews.toString).asString()


}
