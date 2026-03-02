package crawler.workflows.factories

import crawler.core.{WorkflowExecution, executeEntireWorkflow}
import crawler.engine.ExecutionEngine
import crawler.workflows.DispatchCrawlPageWorkflow

object DispatchCrawlPageWorkflowFactory {
  def createDispatchCrawlPageWorkflowExecution(engine: ExecutionEngine): WorkflowExecution = {
    val workflowExecution: WorkflowExecution = WorkflowExecution(DispatchCrawlPageWorkflow())
    workflowExecution.workflowContext.ctx.put("execution_engine", engine)

    workflowExecution
  }

  def createDispatchCrawlPageWorkflowExecutionCallback(engine: ExecutionEngine): (() => Unit) = {
    () => {
      val workflowExecution: WorkflowExecution =
        DispatchCrawlPageWorkflowFactory.createDispatchCrawlPageWorkflowExecution(engine)

      executeEntireWorkflow(workflowExecution)
    }
  }
}
