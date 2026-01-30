package html

import crawler.html.{DOMObject, DOMNonVoidTag, HTMLTagExtractor}

object HTMLTest {
  @main
  def hello() = {
    // Note changing <br/> to <br /> breaks it because of incorrect handling of non void
    // TODO : Fails test case if multiple <<<<<<<
    val html: String = "<html><br /> Hello this <p>Basic <ul><li>Deep list</li>" +
      "</ul> paragraph <a href=\"https://gossim.com\"> ref</a></p> is basic html text</html>"
    val domObjects: List[DOMObject] = HTMLTagExtractor.parseHTML(html, 0, html.length - 1)

    println(domObjects.length)

    val dom1: DOMObject = domObjects(0)

    dom1 match {
      case DOMNonVoidTag(data: String, data2: List[DOMObject], data3: Map[String, String]) =>
        println("We have a non-void object here")
        println(data)
        println(data2)
        println(data3)
    }
  }
}
