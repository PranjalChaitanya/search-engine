package crawler.workflows.factories

import crawler.core.{WorkflowExecution, executeEntireWorkflow}
import crawler.engine.ExecutionEngine
import crawler.workflows.{CrawlPageContext, CrawlPageWorkflow}

object CrawlPageWorkflowFactory {
  def createCrawlPageWorkflowExecution(url: String, engine: ExecutionEngine): WorkflowExecution[CrawlPageContext] =
    WorkflowExecution(CrawlPageWorkflow(), CrawlPageContext(url, engine))

  def createCrawlPageWorkflowExecutionCallback(url: String, engine: ExecutionEngine): () => Unit =
    () => executeEntireWorkflow(createCrawlPageWorkflowExecution(url, engine))
}
