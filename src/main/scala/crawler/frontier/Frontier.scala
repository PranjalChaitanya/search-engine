package crawler.frontier

trait Frontier {
  def addURLToDomain(url: String, domain: String): Unit
  def popURLFromDomain(domain: String): Option[String]
}
