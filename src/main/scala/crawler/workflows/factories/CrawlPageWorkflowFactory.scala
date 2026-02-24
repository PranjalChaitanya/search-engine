package crawler.workflows.factories

import crawler.core.WorkflowExecution
import crawler.workflows.CrawlPageWorkflow

class CrawlPageWorkflowFactory {
  def createCrawlPageWorkflowExecution(url: String) : WorkflowExecution = {
    val workflowExecution: WorkflowExecution = WorkflowExecution(CrawlPageWorkflow())
    workflowExecution.workflowContext.ctx.put("webpage_url", url)
    
    workflowExecution
  }
}
