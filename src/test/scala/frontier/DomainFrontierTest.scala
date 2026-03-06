package frontier

import crawler.frontier.DomainFrontier
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DomainFrontierTest extends AnyFlatSpec with Matchers {
  it should "add and pop multiple url from single domain into the domain frontier" in {
    val domain: String = "http://testdomain.com"

    val url1: String = "http://testdomain.com/url1"
    val url2: String = "http://testdomain.com/url2"
    val url3: String = "http://testdomain.com/url3"

    DomainFrontier.addURLToDomain(url1, domain)
    DomainFrontier.addURLToDomain(url2, domain)
    DomainFrontier.addURLToDomain(url3, domain)

    // currently DomainFrontier is FIFO but it might change so we're not testing that
    var urlList: List[String] = List.empty[String]

    val firstPoppedDomain : Option[String] = DomainFrontier.popURLFromDomain(domain)
    firstPoppedDomain.nonEmpty shouldBe true
    urlList = firstPoppedDomain.get :: urlList

    val secondPoppedDomain : Option[String] = DomainFrontier.popURLFromDomain(domain)
    secondPoppedDomain.nonEmpty shouldBe true
    urlList = secondPoppedDomain.get :: urlList

    val thirdPoppedDomain : Option[String] = DomainFrontier.popURLFromDomain(domain)
    thirdPoppedDomain.nonEmpty shouldBe true
    urlList = thirdPoppedDomain.get :: urlList

    urlList.contains(url1) shouldBe true
    urlList.contains(url2) shouldBe true
    urlList.contains(url3) shouldBe true
  }
}
