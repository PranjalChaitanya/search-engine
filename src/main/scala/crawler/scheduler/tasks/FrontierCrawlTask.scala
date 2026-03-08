package crawler.scheduler.tasks

import crawler.core.{WorkflowExecution, executeEntireWorkflow}
import crawler.engine.ExecutionEngine
import crawler.scheduler.ScheduledTask
import crawler.workflows.factories.{CrawlPageWorkflowFactory, DispatchCrawlPageWorkflowFactory}

class FrontierCrawlTask(val engine: ExecutionEngine) extends ScheduledTask {
  override def getTasks(): List[() => Unit] = {
    val displatchWorkflowExecution: WorkflowExecution =
      DispatchCrawlPageWorkflowFactory.createDispatchCrawlPageWorkflowExecution(engine)
      
    executeEntireWorkflow(displatchWorkflowExecution)
    
    val urlList: List[String] =
      displatchWorkflowExecution.workflowContext.ctx.get("url_list")
        .map(_.asInstanceOf[List[String]])
        .getOrElse(List.empty)
      
    var crawlTasks: List[() => Unit] = List.empty[() => Unit]
    
    urlList.foreach(url => {
      crawlTasks = CrawlPageWorkflowFactory.createCrawlPageWorkflowExecutionCallback(url, engine) :: crawlTasks
    })

    crawlTasks
  }
}
