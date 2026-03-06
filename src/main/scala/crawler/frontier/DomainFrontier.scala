package crawler.frontier

import scala.collection.concurrent.TrieMap

object DomainFrontier {
  // Key is domain and value is list of urls of that domain
  // TODO : This can be optimized further by only locking the specific domain
  private val domainFrontierMap: TrieMap[String, List[String]] = TrieMap.empty[String, List[String]]

  def addURLToDomain(url: String, domain: String): Unit = synchronized {
    val urlList : List[String] = domainFrontierMap.getOrElse(domain, {
      domainFrontierMap.put(domain, List.empty)
      domainFrontierMap(domain)
    })

    domainFrontierMap.update(
      domain,
      url :: urlList
    )
  }

  def popURLFromDomain(domain: String): Option[String] = synchronized {
    val urlList: List[String] = domainFrontierMap.get(domain) match {
      case Some(list) => list
      case None => return None
    }
    
    urlList.headOption match {
      case Some(url) => {
        domainFrontierMap.update(
          domain,
          urlList.tail
        )
        Some(url)
      }
      case None => return None
    }
  }
}