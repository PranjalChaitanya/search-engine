package crawler.html

import java.util.concurrent.ConcurrentHashMap

trait SeenURLStore {
  def markIfNew(url: String): Boolean
}

class CrawlURLState extends SeenURLStore {
  private val seenUrls = ConcurrentHashMap.newKeySet[String]()

  def markIfNew(url: String): Boolean = seenUrls.add(url)

  def clear(): Unit = seenUrls.clear()
}

