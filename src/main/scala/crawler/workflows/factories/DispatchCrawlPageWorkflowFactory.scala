package crawler.workflows.factories

import crawler.core.{WorkflowExecution, executeEntireWorkflow}
import crawler.engine.ExecutionEngine
import crawler.frontier.{CrawlQueue, Frontier}
import crawler.workflows.{DispatchContext, DispatchCrawlPageWorkflow}

object DispatchCrawlPageWorkflowFactory {
  def createDispatchCrawlPageWorkflowExecution(
    engine: ExecutionEngine,
    frontier: Frontier,
    crawlQueue: CrawlQueue
  ): WorkflowExecution[DispatchContext] =
    WorkflowExecution(DispatchCrawlPageWorkflow(), DispatchContext(engine, frontier, crawlQueue))

  def createDispatchCrawlPageWorkflowExecutionCallback(
    engine: ExecutionEngine,
    frontier: Frontier,
    crawlQueue: CrawlQueue
  ): () => Unit =
    () => executeEntireWorkflow(createDispatchCrawlPageWorkflowExecution(engine, frontier, crawlQueue))
}
