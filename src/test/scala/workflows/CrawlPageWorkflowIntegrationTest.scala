package workflows

import crawler.core.Config
import crawler.engine.ExecutionEngine
import crawler.html.CrawlURLState
import crawler.workflows.factories.CrawlPageWorkflowFactory.createCrawlPageWorkflowExecutionCallback
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CrawlPageWorkflowIntegrationTest extends AnyFlatSpec with Matchers {
  val url: String = "https://news.ycombinator.com"
  val engine: ExecutionEngine = ExecutionEngine(Config.executionEngineExecutorThreads)
  val seenURLs: CrawlURLState = new CrawlURLState()

  engine.start()
  engine.submitJob(createCrawlPageWorkflowExecutionCallback(url, engine, seenURLs))

  Thread.sleep(10000)

  engine.shutdown()
}
