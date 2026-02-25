package core

import crawler.core.StateMachine
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class StateMachineSpec extends AnyFlatSpec with Matchers {

  "StateMachine" should "start at the provided starting state" in {
    val sm = new StateMachine[String, Int](List("idle", "running"), "idle", _ => "running")

    sm.getState shouldBe "idle"
  }

  it should "transition to the state returned by the step function" in {
    val sm = new StateMachine[String, Int](List("small", "large"), "small", n => if (n > 10) "large" else "small")

    sm.step(5)
    sm.getState shouldBe "small"

    sm.step(11)
    sm.getState shouldBe "large"
  }

  it should "allow manually setting the current state" in {
    val sm = new StateMachine[String, Unit](List("a", "b", "c"), "a", _ => "b")

    sm.setState("c")
    sm.getState shouldBe "c"
  }

  it should "use event context for multiple transitions" in {
    val sm = new StateMachine[String, String](
      List("draft", "review", "published"),
      "draft",
      event => event match {
        case "submit" => "review"
        case "approve" => "published"
        case _ => "draft"
      }
    )

    sm.step("submit")
    sm.getState shouldBe "review"

    sm.step("approve")
    sm.getState shouldBe "published"
  }
}
