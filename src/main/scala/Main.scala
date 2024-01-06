package io.github.mitchelllisle

import scala.xml.{Elem, Node, XML}
import scalaj.http.Http

import java.text.SimpleDateFormat
import java.util.Locale
import java.sql.{Date, Timestamp}
import scala.io.{BufferedSource, Source}
import io.circe.Encoder
import io.circe.syntax._
import io.circe.generic.semiauto._


final case class Article(title: String, link: String, description: String, pubTimestamp: String, pubDate: String)

class RssReader(url: String) {
  private val pubDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)

  def fetchLatestArticles(page: Int = 1): Elem = {
    val response = Http(s"$url/?page=$page").asString.body
    val xml = XML.loadString(response)
    xml
  }

  private def parseItem(item: Node): Article = {
    val title = (item \ "title").text
    val link = (item \ "link").text
    val description = (item \ "description").text
    val pubDate = (item \ "pubDate").text
    val pubTimestamp = new Timestamp(pubDateFormat.parse(pubDate).getTime).toString
    val pubDateObj = new Date(pubDateFormat.parse(pubDate).getTime).toString
    Article(title, link, description, pubTimestamp, pubDateObj)
  }

  def parseItems(elem: Elem): Seq[Article] = {
    val items = elem \\ "item"
    items.map(parseItem)
  }

  def apply(page: Int = 1): Seq[Article] = {
    val response = fetchLatestArticles(page)
    val items = parseItems(response)
    items
  }
}


object Main {
  private val gcs = GCS("find-a-breach-events")
  private val reader = new RssReader("https://www.databreaches.net/feed")
  lazy implicit val articleEncoder: Encoder[Article] = deriveEncoder[Article]

  def main(args: Array[String]): Unit = {
    val sampleFile: BufferedSource = Source.fromFile("src/test/resources/sample-response.xml")
    val sampleResponse: String = sampleFile.mkString("")
    val pages = reader.parseItems(XML.loadString(sampleResponse))
//    val pages = 1 to 1
//
    val items =
      pages
//        .flatMap(page => reader(page))
        .groupBy(_.pubDate.toString)

    items.foreach(day => {
        val asJson = day._2.asJson.noSpaces
        gcs.write(day._1, asJson)
      }
    )
  }
}
