package html

import scala.io.Source
import scala.util.Using

import crawler.html.{DOMObject, DOMNonVoidTag, HTMLTagExtractor}

object FileTest extends Runnable {
  @main
  override def run(): Unit = {
    val filename = "/Users/pranjalchaitanya/Documents/search-engine/src/test/scala/html/Sample.txt"
    val fileContents: String = Using(Source.fromFile(filename)) { source =>
      source.mkString
    }.getOrElse("")

    HTMLTagExtractor.parseHTML(fileContents)

    println(fileContents)
  }
}
