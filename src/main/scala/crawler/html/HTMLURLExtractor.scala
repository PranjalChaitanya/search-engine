package crawler.html

// TODO : You also have to eventually handle relative urls
def extractURL(domObjects: List[DOMObject]): List[String] = domObjects.flatMap(domObject => domObject match {
  case DOMNonVoidTag(tagType, children, attrs) =>
    val result: List[String] = extractURL(children)

    tagType match {
      case "a" =>
        attrs.get("href").map(link => {
          link :: result
        }).getOrElse(result)
      case _ => result
    }
  case _ =>
    List.empty[String]
})

def extractURL(html: String) : List[String] = extractURL(HTMLParser.parseHTML(html))