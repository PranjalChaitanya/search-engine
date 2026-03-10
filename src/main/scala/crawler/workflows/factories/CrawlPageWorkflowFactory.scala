package crawler.workflows.factories

import crawler.core.{WorkflowExecution, executeEntireWorkflow}
import crawler.frontier.{CrawlQueue, Frontier}
import crawler.html.SeenURLStore
import crawler.workflows.{CrawlPageContext, CrawlPageWorkflow}

object CrawlPageWorkflowFactory {
  def createCrawlPageWorkflowExecution(
    url: String,
    frontier: Frontier,
    crawlQueue: CrawlQueue,
    seenURLs: SeenURLStore
  ): WorkflowExecution[CrawlPageContext] =
    WorkflowExecution(CrawlPageWorkflow(), CrawlPageContext(url, frontier, crawlQueue, seenURLs))

  def createCrawlPageWorkflowExecutionCallback(
    url: String,
    frontier: Frontier,
    crawlQueue: CrawlQueue,
    seenURLs: SeenURLStore
  ): () => Unit =
    () => executeEntireWorkflow(createCrawlPageWorkflowExecution(url, frontier, crawlQueue, seenURLs))
}
