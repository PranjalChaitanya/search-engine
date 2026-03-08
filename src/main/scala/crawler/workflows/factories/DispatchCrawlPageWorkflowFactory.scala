package crawler.workflows.factories

import crawler.core.{WorkflowExecution, executeEntireWorkflow}
import crawler.engine.ExecutionEngine
import crawler.workflows.{DispatchContext, DispatchCrawlPageWorkflow}

object DispatchCrawlPageWorkflowFactory {
  def createDispatchCrawlPageWorkflowExecution(engine: ExecutionEngine): WorkflowExecution[DispatchContext] =
    WorkflowExecution(DispatchCrawlPageWorkflow(), DispatchContext(engine))

  def createDispatchCrawlPageWorkflowExecutionCallback(engine: ExecutionEngine): () => Unit =
    () => executeEntireWorkflow(createDispatchCrawlPageWorkflowExecution(engine))
}
