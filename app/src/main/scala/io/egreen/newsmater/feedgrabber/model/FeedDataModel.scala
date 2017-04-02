package io.egreen.newsmater.feedgrabber.model

import org.bson.types.ObjectId
import org.mongodb.morphia.annotations.{Entity, Id}

import scala.beans.BeanProperty

/**
  * Created by dewmal on 4/2/17.
  */
@Entity
class FeedDataModel {

  @Id
  @BeanProperty
  var id:ObjectId=_

  @BeanProperty
  var url:String=_

  @BeanProperty
  var lastUrl:String=_


  override def toString = s"FeedDataModel($id, $url, $lastUrl)"
}
