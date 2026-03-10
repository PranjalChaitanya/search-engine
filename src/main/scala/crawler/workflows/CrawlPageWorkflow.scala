package crawler.workflows

import crawler.core.{Logger, StepResult, Workflow, WorkflowFailure, WorkflowStep, WorkflowSuccess, WorkflowTransition}
import crawler.frontier.{CrawlQueue, Frontier}
import crawler.html.{DOMObject, SeenURLStore, extractRootURL, extractURL, markURLAsSeen, normalizeAndFilterURLs, parseHTML}
import crawler.politeness.PolitenessManager
import crawler.scraper.scrapeWebpage

import java.util.UUID

class CrawlPageContext(
  val webpageUrl: String,
  val frontier: Frontier,
  val crawlQueue: CrawlQueue,
  val seenURLs: SeenURLStore
) {
  val loggingUUID: String = UUID.randomUUID().toString
  var scrapedResult: Option[String] = None
  var parsedHtml: Option[List[DOMObject]] = None
  var extractedUrls: Option[List[String]] = None
}

case object FetchWebpageStep extends WorkflowStep[CrawlPageContext] {
  override def run(input: CrawlPageContext): StepResult = {
    Logger.log(s"Workflow started with UUID ${input.loggingUUID} on URL: ${input.webpageUrl}")
    markURLAsSeen(input.webpageUrl, input.seenURLs)

    scrapeWebpage(input.webpageUrl) match {
      case Some(html) =>
        Logger.log(s"Workflow UUID ${input.loggingUUID}: Successfully scraped webpage on ${input.webpageUrl}")
        input.scrapedResult = Some(html)
        WorkflowSuccess
      case None =>
        Logger.log(s"Workflow UUID ${input.loggingUUID}: Failed to scrape webpage on ${input.webpageUrl}")
        WorkflowFailure
    }
  }
}

case object ParseWebpageStep extends WorkflowStep[CrawlPageContext] {
  override def run(input: CrawlPageContext): StepResult = {
    input.scrapedResult match {
      case None => WorkflowFailure
      case Some(html) =>
        input.parsedHtml = Some(parseHTML(html))
        input.scrapedResult = None
        Logger.log(s"Workflow UUID ${input.loggingUUID}: Successfully parsed result")
        WorkflowSuccess
    }
  }
}

case object ExtractURLStep extends WorkflowStep[CrawlPageContext] {
  override def run(input: CrawlPageContext): StepResult = {
    input.parsedHtml match {
      case None => WorkflowFailure
      case Some(dom) =>
        input.extractedUrls = Some(extractURL(dom))
        input.parsedHtml = None
        Logger.log(s"Workflow UUID ${input.loggingUUID}: Successfully extracted URLs from DOMObject")
        WorkflowSuccess
    }
  }
}

case object AddURLsToFrontierStep extends WorkflowStep[CrawlPageContext] {
  override def run(input: CrawlPageContext): StepResult = {
    input.extractedUrls match {
      case None => WorkflowFailure
      case Some(urls) =>
        val normalizedURLs = normalizeAndFilterURLs(urls, input.webpageUrl, input.seenURLs)
        Logger.log(s"Workflow UUID ${input.loggingUUID}: New list of normalized urls: ${normalizedURLs.toString()}")
        normalizedURLs.foreach { url =>
          val domain = extractRootURL(url)
          input.frontier.addURLToDomain(url, domain)
          input.crawlQueue.addDomain(domain, PolitenessManager.nextAllowedCrawlTime(domain))
        }
        WorkflowSuccess
    }
  }
}

class CrawlPageWorkflow extends Workflow[CrawlPageContext] {
  override val startingStep: WorkflowStep[CrawlPageContext] = FetchWebpageStep

  override val transitions: Map[WorkflowStep[CrawlPageContext], WorkflowTransition[CrawlPageContext]] = Map(
    FetchWebpageStep      -> WorkflowTransition(Some(ParseWebpageStep), None),
    ParseWebpageStep      -> WorkflowTransition(Some(ExtractURLStep), None),
    ExtractURLStep        -> WorkflowTransition(Some(AddURLsToFrontierStep), None),
    AddURLsToFrontierStep -> WorkflowTransition(None, None)
  )
}
