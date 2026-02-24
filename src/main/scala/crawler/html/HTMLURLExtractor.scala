package crawler.html

import java.net.URI

def isRelativeURL(url: String) : Boolean = {
  !(url.startsWith("http://") && url.startsWith("https://"))
}

def createURLFromRelative(absoluteUrl: String, relative: String) : String = {
  extractRootURL(absoluteUrl) + relative
}

def extractRootURL(absoluteURL: String): String = {
  val uri = new URI(absoluteURL)

  val portPart =
    if (uri.getPort == -1) ""
    else s":${uri.getPort}"

  s"${uri.getScheme}://${uri.getHost}$portPart"
}

def createURL(newURL: String, originalAbsoluteURL : String) : String = {
  if(isRelativeURL(newURL)) {
    createURLFromRelative(originalAbsoluteURL, newURL)
  } else {
    newURL
  }
}

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