package io.egreen.newsmater.feedgrabber

import java.io.{File, FileReader}
import java.net.URL
import java.util
import java.util.Properties
import javax.xml.bind.{JAXBContext, Unmarshaller}

import com.mongodb.MongoClient
import com.rometools.rome.feed.synd.{SyndEntry, SyndFeed}
import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import io.egreen.newsmater.feedgrabber.model.FeedDataModel
import io.egreen.newsmater.feedgrabber.xml.{Feed, FeedList}
import org.bson.types.ObjectId
import org.mongodb.morphia.{Datastore, Key, Morphia}
import org.quartz.impl.StdSchedulerFactory
import org.quartz.{SimpleScheduleBuilder, _}

import scala.collection.JavaConverters._

/**
  * Created by dewmal on 4/2/17.
  */


object Main {
  val systemPro: Properties = new Properties();

  def main(args: Array[String]): Unit = {

    systemPro.load(new FileReader(new File("system.properties")))


    val morphia = new Morphia
    // tell Morphia where to find your classes
    // can be called multiple times with different packages or classes
    morphia.mapPackage("io.egreen.newsmater.feedgrabber.model")
    // create the Datastore connecting to the default port on the local host
    val datastore = morphia.createDatastore(new MongoClient(
      systemPro.getProperty("db.host", "localhost"),
      java.lang.Integer.parseInt(systemPro.getProperty("db.port", "27017"))
    ), "newsmaster_db")


    datastore.ensureIndexes()


    val jaxb: JAXBContext = JAXBContext.newInstance(classOf[FeedList])

    val unmarshaller: Unmarshaller = jaxb.createUnmarshaller();
    val feedList: FeedList = unmarshaller.unmarshal(new File(("feeds.xml"))).asInstanceOf[FeedList]


    var scheduler: Scheduler = StdSchedulerFactory.getDefaultScheduler();


    feedList.feeds.asScala.foreach(
      f = (feed: Feed) => {
        var jobDataMap: JobDataMap = new JobDataMap();
        jobDataMap.put("feed", feed);
        jobDataMap.put("datastore", datastore);




        var jobDetail: JobDetail = JobBuilder.newJob(classOf[HelloJob]).
          withIdentity("feed-" + feed.source, "FEED_GROUP1")
          .setJobData(jobDataMap)
          .build()


        var trigger: Trigger = TriggerBuilder.newTrigger()
          .withIdentity("trigger-" + feed.source, "FEED_GROUP1")
          .startNow()
          .withSchedule(SimpleScheduleBuilder.simpleSchedule()
            .withIntervalInSeconds(feed.interval)
            .repeatForever())
          .build()
        scheduler.scheduleJob(jobDetail, trigger)
      }
    )

    scheduler.start


  }
}


class HelloJob extends Job {


  override def execute(context: JobExecutionContext): Unit = {
    try {


      val feed: Feed = context.getJobDetail.getJobDataMap.get("feed").asInstanceOf[Feed]
      val datastore: Datastore = context.getJobDetail.getJobDataMap.get("datastore").asInstanceOf[Datastore]

      var feedDataModel: FeedDataModel = datastore.find(classOf[FeedDataModel]).filter("url =", feed.url).get();
      import io.egreen.newsmater.feedgrabber.model.FeedDataModel
      if (feedDataModel == null) {
        feedDataModel = new FeedDataModel
        feedDataModel.url = feed.url
        var key: Key[_] = datastore.save(feedDataModel)
        feedDataModel.id = key.getId.asInstanceOf[ObjectId]
      }
      val url = feedDataModel.url
      val feedDetails: SyndFeed = new SyndFeedInput().build(new XmlReader(new URL(url)))

      val lastURL: String = feedDataModel.lastUrl + "".concat("")
      var firstUrlInTheList: String = null


      var syndEntries: java.util.List[SyndEntry] = new util.ArrayList[SyndEntry]()

      feedDetails.getEntries.asScala.foreach((syndEntry: SyndEntry) => {
        if (firstUrlInTheList == null) {
          firstUrlInTheList = syndEntry.getLink
          if (firstUrlInTheList != null) {
            feedDataModel.lastUrl = firstUrlInTheList
            var key: Key[_] = datastore.save(feedDataModel)
          }
        }
        if (lastURL.equals(syndEntry.getLink)) {
          return
        }
        new PostToNewsMaster(syndEntry, feed)
      })



    } catch {
      case e => {
        e.printStackTrace()
      }
    }
  }
}



