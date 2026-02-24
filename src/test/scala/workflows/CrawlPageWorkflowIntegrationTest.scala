package workflows

import crawler.core.WorkflowExecution
import crawler.engine.ExecutionEngine
import crawler.workflows.factories.CrawlPageWorkflowFactory
import crawler.workflows.factories.CrawlPageWorkflowFactory.createCrawlPageWorkflowExecutionCallback
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CrawlPageWorkflowIntegrationTest extends AnyFlatSpec with Matchers {
  val url: String = "https://en.wikipedia.org/wiki/Apache_Iceberg"
  val engine: ExecutionEngine = ExecutionEngine(4)

  engine.start()

  val workflowExecution: WorkflowExecution =
    CrawlPageWorkflowFactory.createCrawlPageWorkflowExecution(url, engine)

  engine.submitJob(createCrawlPageWorkflowExecutionCallback(url, engine))

  Thread.sleep(1)

  engine.shutdown()
}
