package crawler.scraper

import scala.sys.process._

// returns the html for the webpage as a string
def scrapeWebpage(url: String) : String = {
  val command: Seq[String] = Seq("curl", url)
  command.!!
}
