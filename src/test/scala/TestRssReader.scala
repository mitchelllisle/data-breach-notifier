package io.github.mitchelllisle

import org.mockito.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Try
import scala.io.{BufferedSource, Source}
import scala.xml.XML


class TestRssReader extends AnyFlatSpec with Matchers with MockitoSugar {
  val sampleFile: BufferedSource = Source.fromFile("src/test/resources/sample-response.xml")
  val sampleResponse: String = sampleFile.mkString("")
  sampleFile.close()

  val rssReader: RssReader = mock[RssReader]
  when(rssReader.fetchLatestArticles(1)).thenReturn(XML.loadString(sampleResponse))

  "RssReader" should "fetch the RSS feed from the specified URL" in {
    val result = Try(rssReader.fetchLatestArticles(1))
    result.isSuccess shouldBe true
    result.get should not be empty
  }

//  it should "correctly parse the fetched RSS feed into articles" in {
//    val articles = rssReader.fetchLatestArticles(1)
//
//    val parsed = rssReader.parseItems(articles)
//    parsed.foreach { article =>
//      article.title should not be empty
//      article.link should not be empty
//      article.pubDate should not be empty
//      article.description should not be empty
//    }
//  }
}
