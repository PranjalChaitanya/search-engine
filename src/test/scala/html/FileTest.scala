package html

import scala.io.Source
import scala.util.Using
import crawler.html.{DOMNonVoidTag, DOMObject, HTMLParser, HTMLTokenizer}

object FileTest extends Runnable {
  @main
  override def run(): Unit = {
    val filename = "/Users/pranjalchaitanya/Documents/search-engine/src/test/scala/html/Sample.txt"
    val fileContents: String = Using(Source.fromFile(filename)) { source =>
      source.mkString
    }.getOrElse("")

    println(HTMLParser.parseHTML(fileContents))

//    println(fileContents)
//    println(HTMLTokenizer.tokenize(fileContents))
  }
}
