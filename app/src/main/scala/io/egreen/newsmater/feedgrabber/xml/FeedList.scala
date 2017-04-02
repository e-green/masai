package io.egreen.newsmater.feedgrabber.xml

import javax.xml.bind.annotation.{XmlAccessType, XmlAccessorType, XmlElement, XmlRootElement}

/**
  * Created by dewmal on 4/2/17.
  */
@XmlRootElement(name = "feeds")
@XmlAccessorType(XmlAccessType.FIELD)
class FeedList {

  @XmlElement(name = "interval",defaultValue = "60000")
  var interval:Long=_

  @XmlElement(name = "feed")
  var feeds: java.util.List[Feed] = _


  override def toString = s"FeedList(interval=$interval, feeds=$feeds)"
}
@XmlRootElement(name = "feed")
class Feed extends Serializable{

  @XmlElement(name = "url")
  var url: String = _
  @XmlElement(name = "source")
  var source: Source = _
  @XmlElement(name = "interval")
  var interval: Int = _


  override def toString = s"Feed($url, $source, $interval)"
}

@XmlRootElement(name = "source")
class Source  extends Serializable{
  @XmlElement(name = "name")
  var name: String = _
  @XmlElement(name = "tag")
  var tag: String = _
  @XmlElement(name = "description")
  var description: String = _


  override def toString = s"Source(name=$name, tag=$tag, description=$description)"
}