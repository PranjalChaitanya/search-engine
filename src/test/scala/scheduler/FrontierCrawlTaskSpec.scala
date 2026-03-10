package scheduler

import crawler.engine.ExecutionEngine
import crawler.frontier.{CrawlQueue, Frontier}
import crawler.html.CrawlURLState
import crawler.scheduler.tasks.FrontierCrawlTask
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDateTime

class FrontierCrawlTaskSpec extends AnyFlatSpec with Matchers {

  class StubFrontier(urlsPerDomain: Map[String, List[String]]) extends Frontier {
    private val store = scala.collection.mutable.Map(urlsPerDomain.toSeq: _*)
    def addURLToDomain(url: String, domain: String): Unit = ()
    def popURLFromDomain(domain: String): Option[String] =
      store.get(domain).flatMap(_.headOption).map { url =>
        store.update(domain, store(domain).tail)
        url
      }
  }

  class StubCrawlQueue(crawlableDomains: List[String]) extends CrawlQueue {
    private var popped = false
    def addDomain(domain: String, nextCrawlableTime: LocalDateTime): Boolean = true
    def popAllCrawlableDomains(): List[String] = {
      if (!popped) { popped = true; crawlableDomains } else List.empty
    }
    def containsDomain(domain: String): Boolean = false
  }

  class CapturingEngine extends ExecutionEngine(0) {
    val submitted: scala.collection.mutable.ListBuffer[() => Unit] =
      scala.collection.mutable.ListBuffer.empty
    override def submitJob(job: () => Unit): Unit = submitted += job
  }

  it should "return one callback per crawlable URL" in {
    val frontier   = new StubFrontier(Map("https://example.com" -> List("https://example.com/a", "https://example.com/b")))
    val crawlQueue = new StubCrawlQueue(List("https://example.com"))
    val seenURLs   = new CrawlURLState()
    val engine     = new CapturingEngine()

    val task    = new FrontierCrawlTask(engine, frontier, crawlQueue, seenURLs)
    val tasks   = task.getTasks()

    tasks should have length 1
  }

  it should "return empty list when no domains are crawlable" in {
    val frontier   = new StubFrontier(Map.empty)
    val crawlQueue = new StubCrawlQueue(List.empty)
    val seenURLs   = new CrawlURLState()
    val engine     = new CapturingEngine()

    val task  = new FrontierCrawlTask(engine, frontier, crawlQueue, seenURLs)
    val tasks = task.getTasks()

    tasks shouldBe empty
  }

  it should "return one callback per crawlable domain" in {
    val frontier = new StubFrontier(Map(
      "https://a.com" -> List("https://a.com/page"),
      "https://b.com" -> List("https://b.com/page")
    ))
    val crawlQueue = new StubCrawlQueue(List("https://a.com", "https://b.com"))
    val seenURLs   = new CrawlURLState()
    val engine     = new CapturingEngine()

    val task  = new FrontierCrawlTask(engine, frontier, crawlQueue, seenURLs)
    val tasks = task.getTasks()

    tasks should have length 2
  }
}
