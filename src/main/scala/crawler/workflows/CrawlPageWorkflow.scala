package crawler.workflows

import crawler.core.{Logger, StepResult, Workflow, WorkflowContext, WorkflowExecution, WorkflowFailure, WorkflowStep, WorkflowSuccess, WorkflowTransition, executeEntireWorkflow}
import crawler.engine.ExecutionEngine
import crawler.html.{DOMObject, extractURL, markURLAsSeen, normalizeAndFilterURLs, parseHTML}
import crawler.scraper.scrapeWebpage
import crawler.workflows.factories.CrawlPageWorkflowFactory.createCrawlPageWorkflowExecutionCallback

import java.util.UUID

case object FetchWebpageStep extends WorkflowStep {
  override def run(input: WorkflowContext): StepResult = {
    input.ctx.put("logging_uuid", UUID.randomUUID().toString) // TODO : Should add this to the workflow execution
    val loggingUUID : String = input.ctx("logging_uuid").toString
    val webpageURL : String = input.ctx("webpage_url").toString

    Logger.log(s"Workflow started with UUID $loggingUUID on URL : $webpageURL")

    markURLAsSeen(webpageURL)
//    markURLAsSeen(webpageURL) match {
//      case true =>
//        Logger.log(s"Workflow UUID $loggingUUID: Will execute workflow on $webpageURL")
//      case false =>
//        Logger.log(s"Workflow UUID $loggingUUID: Will not execute workflow on $webpageURL")
//        return WorkflowFailure
//    }

    val scrapeResult : String = scrapeWebpage(input.ctx("webpage_url").toString) match {
      case Some(s) =>
        Logger.log(s"Workflow UUID $loggingUUID: Successfully scraped webpage on $webpageURL")
        s.toString
      case None =>
        Logger.log(s"Workflow UUID $loggingUUID: Failed to scrape webpage on $webpageURL")
        return WorkflowFailure
    }
    input.ctx.put("scraped_result", scrapeResult)

    WorkflowSuccess
  }
}

case object ParseWebpageStep extends WorkflowStep {
  override def run(input: WorkflowContext): StepResult = {
    val loggingUUID : String = input.ctx("logging_uuid").toString

    val scrapedResult : String = input.ctx.get("scraped_result") match {
      case Some(s) => s.toString
      case None =>
        return WorkflowFailure
    }

    val parsedResult : List[DOMObject] = parseHTML(scrapedResult) // TODO : Add more robust error handling
    input.ctx.put("parsed_html", parsedResult)
    input.ctx.remove("scraped_result") // removing it since it's no longer needed and just uses memory

    Logger.log(s"Workflow UUID $loggingUUID: Successfully parsed result")

    WorkflowSuccess
  }
}

case object ExtractURLStep extends WorkflowStep {
  override def run(input: WorkflowContext): StepResult = {
    val loggingUUID : String = input.ctx("logging_uuid").toString

    val domObjects: List[DOMObject] = input.ctx.get("parsed_html") match {
      case Some(s) => s.asInstanceOf[List[DOMObject]]
      case None =>
        return WorkflowFailure
    }

    val extractedURLs : List[String] = extractURL(domObjects)
    Logger.log(s"Workflow UUID $loggingUUID: Successfully extracted URL's from DOMObject")

    input.ctx.remove("parsed_html")
    input.ctx.put("extracted_urls", extractedURLs)

    WorkflowSuccess
  }
}

case object SubmitNewJobsStep extends WorkflowStep {
  override def run(input: WorkflowContext): StepResult = {
    val loggingUUID : String = input.ctx("logging_uuid").toString

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

    val normalizedURLs: List[String] = normalizeAndFilterURLs(extractedURLs, webpageURL)

    Logger.log(s"Workflow UUID $loggingUUID: New list of normalized urls: ${normalizedURLs.toString()}")

    normalizedURLs.foreach(url => {
      executionEngine.submitJob(
        createCrawlPageWorkflowExecutionCallback(url, executionEngine)
      )
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