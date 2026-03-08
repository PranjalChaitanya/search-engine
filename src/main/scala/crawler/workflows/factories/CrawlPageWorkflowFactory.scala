package crawler.workflows.factories

import crawler.core.{WorkflowExecution, executeEntireWorkflow}
import crawler.engine.ExecutionEngine
import crawler.html.SeenURLStore
import crawler.workflows.{CrawlPageContext, CrawlPageWorkflow}

object CrawlPageWorkflowFactory {
  def createCrawlPageWorkflowExecution(
    url: String,
    engine: ExecutionEngine,
    seenURLs: SeenURLStore
  ): WorkflowExecution[CrawlPageContext] =
    WorkflowExecution(CrawlPageWorkflow(), CrawlPageContext(url, engine, seenURLs))

  def createCrawlPageWorkflowExecutionCallback(
    url: String,
    engine: ExecutionEngine,
    seenURLs: SeenURLStore
  ): () => Unit =
    () => executeEntireWorkflow(createCrawlPageWorkflowExecution(url, engine, seenURLs))
}
