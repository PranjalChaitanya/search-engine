package crawler.workflows.factories

import crawler.core.{WorkflowExecution, executeEntireWorkflow}
import crawler.engine.ExecutionEngine
import crawler.workflows.CrawlPageWorkflow

object CrawlPageWorkflowFactory {
  def createCrawlPageWorkflowExecution(url: String, engine: ExecutionEngine) : WorkflowExecution = {
    val workflowExecution: WorkflowExecution = WorkflowExecution(CrawlPageWorkflow())
    workflowExecution.workflowContext.ctx.put("webpage_url", url)
    workflowExecution.workflowContext.ctx.put("execution_engine", engine)
    
    workflowExecution
  }

  def createCrawlPageWorkflowExecutionCallback(url: String, engine: ExecutionEngine) : (() => Unit) = {
    println("Callback")
    println("URL")
    println("Engine")
    println(url)
    println(engine)
    
    () => {
      val workflowExecution: WorkflowExecution =
        CrawlPageWorkflowFactory.createCrawlPageWorkflowExecution(url, engine)

      executeEntireWorkflow(workflowExecution)
    }
  }
}
