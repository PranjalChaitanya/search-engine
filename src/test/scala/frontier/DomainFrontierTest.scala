package frontier

import crawler.frontier.DomainFrontier
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DomainFrontierTest extends AnyFlatSpec with Matchers {
  it should "add and pop multiple urls from a single domain" in {
    val frontier = new DomainFrontier()
    val domain = "http://testdomain.com"

    frontier.addURLToDomain("http://testdomain.com/url1", domain)
    frontier.addURLToDomain("http://testdomain.com/url2", domain)
    frontier.addURLToDomain("http://testdomain.com/url3", domain)

    val popped = List(
      frontier.popURLFromDomain(domain),
      frontier.popURLFromDomain(domain),
      frontier.popURLFromDomain(domain)
    )

    popped.forall(_.nonEmpty) shouldBe true
    popped.flatten should contain allOf ("http://testdomain.com/url1", "http://testdomain.com/url2", "http://testdomain.com/url3")
  }
}
