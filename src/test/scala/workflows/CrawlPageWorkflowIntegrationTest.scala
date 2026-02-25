package workflows

import crawler.core.{Config, WorkflowExecution}
import crawler.engine.ExecutionEngine
import crawler.workflows.factories.CrawlPageWorkflowFactory
import crawler.workflows.factories.CrawlPageWorkflowFactory.createCrawlPageWorkflowExecutionCallback
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CrawlPageWorkflowIntegrationTest extends AnyFlatSpec with Matchers {
  val url: String = "https://en.wikipedia.org/wiki/Apache_Iceberg"
  val engine: ExecutionEngine = ExecutionEngine(Config.executionEngineExecutorThreads)

  engine.start()
  engine.submitJob(createCrawlPageWorkflowExecutionCallback(url, engine))

  Thread.sleep(10000)

  engine.shutdown()
}
