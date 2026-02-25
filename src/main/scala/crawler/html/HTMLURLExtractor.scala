package crawler.html

import java.net.URI
import java.util.concurrent.ConcurrentHashMap

private object CrawlURLState {
  private val seenUrls = ConcurrentHashMap.newKeySet[String]()

  def markIfNew(url: String): Boolean = {
    seenUrls.add(url)
  }

  def clear(): Unit = {
    seenUrls.clear()
  }
}

def isRelativeURL(url: String): Boolean = {
  val trimmed = url.trim
  !(trimmed.startsWith("http://") || trimmed.startsWith("https://"))
}

def extractRootURL(absoluteURL: String): String = {
  val uri = new URI(absoluteURL)

  val portPart =
    if (uri.getPort == -1) ""
    else s":${uri.getPort}"

  s"${uri.getScheme}://${uri.getHost}$portPart"
}

def createURLFromRelative(absoluteUrl: String, relative: String): String = {
  new URI(absoluteUrl).resolve(relative).toString
}

def createURL(newURL: String, originalAbsoluteURL: String): String = {
  val trimmed = newURL.trim

  if (trimmed.isEmpty) {
    originalAbsoluteURL
  } else if (isRelativeURL(trimmed)) {
    createURLFromRelative(originalAbsoluteURL, trimmed)
  } else {
    trimmed
  }
}

def canonicalizeURL(rawUrl: String): Option[String] = {
  try {
    val trimmed = rawUrl.trim
    if (trimmed.isEmpty) {
      None
    } else {
      val uri = URI.create(trimmed).normalize()

      val scheme = Option(uri.getScheme).map(_.toLowerCase).orNull
      val host = Option(uri.getHost).map(_.toLowerCase).orNull
      val port = uri.getPort

      val rawPath = Option(uri.getPath).getOrElse("")
      val normalizedPath =
        if (rawPath.isEmpty || rawPath == "/") ""
        else if (rawPath.endsWith("/")) rawPath.dropRight(1)
        else rawPath

      val normalized = new URI(
        scheme,
        uri.getUserInfo,
        host,
        port,
        normalizedPath,
        uri.getQuery,
        null // drop fragment
      )

      val normalizedString = normalized.toString

      if (
        normalizedString.startsWith("http://") ||
          normalizedString.startsWith("https://")
      ) {
        Some(normalizedString)
      } else {
        None
      }
    }
  } catch {
    case _: IllegalArgumentException => None
  }
}

def isCrawlableLink(url: String): Boolean = {
  val trimmed = url.trim.toLowerCase

  trimmed.nonEmpty &&
    !trimmed.startsWith("#") &&
    !trimmed.startsWith("javascript:") &&
    !trimmed.startsWith("mailto:") &&
    !trimmed.startsWith("tel:") &&
    !trimmed.startsWith("data:")
}

def markURLAsSeen(rawUrl: String): Boolean = {
  canonicalizeURL(rawUrl) match {
    case Some(url) => CrawlURLState.markIfNew(url)
    case None => false
  }
}

def normalizeAndFilterURLs(urls: List[String], baseURL: String): List[String] = {
  urls.foldLeft(List.empty[String]) { (acc, rawUrl) =>
    if (!isCrawlableLink(rawUrl)) {
      acc
    } else {
      val maybeFinalUrl =
        canonicalizeURL(createURL(rawUrl, baseURL))

      maybeFinalUrl match {
        case Some(finalUrl) if CrawlURLState.markIfNew(finalUrl) =>
          acc :+ finalUrl
        case _ =>
          acc
      }
    }
  }
}

def extractURL(domObjects: List[DOMObject]): List[String] = {
  def loop(nodes: List[DOMObject]): List[String] =
    nodes.flatMap {
      case DOMNonVoidTag(tagType, children, attrs) =>
        val childResults = loop(children)

        tagType match {
          case "a" =>
            attrs
              .get("href")
              .map(_.trim)
              .filter(_.nonEmpty)
              .map(link => link :: childResults)
              .getOrElse(childResults)

          case _ =>
            childResults
        }

      case _ =>
        List.empty[String]
    }

  // local dedup while preserving first-seen order
  loop(domObjects).foldLeft(List.empty[String]) { (acc, url) =>
    if (acc.contains(url)) acc else acc :+ url
  }
}