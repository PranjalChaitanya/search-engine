package url

import crawler.html.{createURL, isRelativeURL}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class URLCreation extends AnyFlatSpec with Matchers {
  it should "recognize relative URL" in {
    val url: String = "/wiki/Main_Page"

    isRelativeURL(url) shouldBe true
  }

  it should "recognize absolute URL" in {
    val url: String =
      "https://docs.oracle.com/en-us/iaas/autonomous-database-serverless/doc/query-external-data-apache-iceberg.html"

    isRelativeURL(url) shouldBe false
  }

  it should "create relative URL" in {
    val absoluteURL : String = "https://en.wikipedia.org/wiki/Apache_Iceberg"
    val relativeURL : String = "/wiki/Main_Page"

    val createdURL : String = createURL(relativeURL, absoluteURL)

    createdURL shouldBe "https://en.wikipedia.org/wiki/Main_Page"
  }

  it should "create absolute URL" in {
    val absoluteURL: String = "https://en.wikipedia.org/wiki/Apache_Iceberg"
    val relativeURL: String =
      "https://docs.oracle.com/en-us/iaas/autonomous-database-serverless/doc/query-external-data-apache-iceberg.html"

    val createdURL: String = createURL(relativeURL, absoluteURL)

    createdURL shouldBe
      "https://docs.oracle.com/en-us/iaas/autonomous-database-serverless/doc/query-external-data-apache-iceberg.html"
  }
}
