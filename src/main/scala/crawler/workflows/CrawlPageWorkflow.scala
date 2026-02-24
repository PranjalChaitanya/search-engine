package crawler.workflows

import crawler.core.{StepResult, Workflow, WorkflowContext, WorkflowExecution, WorkflowFailure, WorkflowStep, WorkflowSuccess, WorkflowTransition, executeEntireWorkflow}
import crawler.engine.ExecutionEngine
import crawler.html.{DOMObject, createURL, extractURL, parseHTML}
import crawler.scraper.scrapeWebpage
import crawler.workflows.factories.CrawlPageWorkflowFactory
import crawler.workflows.factories.CrawlPageWorkflowFactory.createCrawlPageWorkflowExecutionCallback

case object FetchWebpageStep extends WorkflowStep {
  override def run(input: WorkflowContext): StepResult = {
    val scrapeResult : String = scrapeWebpage(input.ctx("webpage_url").toString)
    // TODO : Handle error cases. scrapeWebpage currently "never fails"
    input.ctx.put("scraped_result", scrapeResult)

    WorkflowSuccess
  }
}

case object ParseWebpageStep extends WorkflowStep {
  override def run(input: WorkflowContext): StepResult = {
    val scrapedResult : String = input.ctx.get("scraped_result") match {
      case Some(s) => s.toString
      case None =>
        return WorkflowFailure
    }

    val parsedResult : List[DOMObject] = parseHTML(scrapedResult) // TODO : Add more robust error handling
    input.ctx.put("parsed_html", parsedResult)

    input.ctx.remove("scraped_result") // removing it since it's no longer needed and just uses memory

    WorkflowSuccess
  }
}

case object ExtractURLStep extends WorkflowStep {
  override def run(input: WorkflowContext): StepResult = {
    val domObjects: List[DOMObject] = input.ctx.get("parsed_html") match {
      case Some(s) => s.asInstanceOf[List[DOMObject]]
      case None =>
        return WorkflowFailure
    }

    val extractedURLs : List[String] = extractURL(domObjects)
    input.ctx.remove("parsed_html")
    input.ctx.put("extracted_urls", extractedURLs)

    WorkflowSuccess
  }
}

case object SubmitNewJobsStep extends WorkflowStep {
  override def run(input: WorkflowContext): StepResult = {
    val executionEngine: ExecutionEngine = input.ctx.get("execution_engine") match {
      case Some(s) => s.asInstanceOf[ExecutionEngine]
      case None =>
        return WorkflowFailure
    }

    val extractedURLs : List[String] = input.ctx.get("extracted_urls") match {
      case Some(s) => s.asInstanceOf[List[String]]
      case None =>
        return WorkflowFailure
    }

    val webpageURL : String = input.ctx("webpage_url").toString

    val newURLS = extractedURLs.map(url => {
      createURL(url, webpageURL)
    })
    
    extractedURLs.foreach(url => {
      executionEngine.submitJob(() => {
        createCrawlPageWorkflowExecutionCallback(createURL(url, webpageURL), executionEngine)
      })
    })

    WorkflowSuccess
  }
}

class CrawlPageWorkflow extends Workflow {
  override val startingStep: WorkflowStep = FetchWebpageStep
  
  override val transitions: Map[WorkflowStep, WorkflowTransition] = Map(
    FetchWebpageStep -> WorkflowTransition(
      onSuccess = Some(ParseWebpageStep),
      onFailure = None
    ),

    ParseWebpageStep -> WorkflowTransition(
      onSuccess = Some(ExtractURLStep),
      onFailure = None
    ),

    ExtractURLStep -> WorkflowTransition(
      onSuccess = Some(SubmitNewJobsStep),
      onFailure = None
    ),

    SubmitNewJobsStep -> WorkflowTransition(
      onSuccess = None,
      onFailure = None
    )
  )
}
