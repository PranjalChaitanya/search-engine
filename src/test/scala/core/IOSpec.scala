package core

import crawler.core.IO
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class IOSpec extends AnyFlatSpec with Matchers {

  "IO" should "defer effects until unsafeRun is invoked" in {
    var sideEffectCount = 0

    val io = IO {
      sideEffectCount += 1
      sideEffectCount
    }

    sideEffectCount shouldBe 0
    IO.unsafeRun(io) shouldBe 1
    sideEffectCount shouldBe 1
  }

  it should "map over values" in {
    val io = IO.pure(10).map(_ * 3)

    IO.unsafeRun(io) shouldBe 30
  }

  it should "flatMap computations in sequence" in {
    val io = IO.pure(2)
      .flatMap(x => IO.pure(x + 5))
      .flatMap(x => IO.pure(x * 4))

    IO.unsafeRun(io) shouldBe 28
  }

  it should "support long flatMap chains without losing ordering" in {
    val chained = (1 to 100).foldLeft(IO.pure(0)) { (acc, next) =>
      acc.flatMap(sum => IO.pure(sum + next))
    }

    IO.unsafeRun(chained) shouldBe 5050
  }

  it should "evaluate delayed effects each time unsafeRun is called" in {
    var counter = 0

    val io = IO {
      counter += 1
      counter
    }

    IO.unsafeRun(io) shouldBe 1
    IO.unsafeRun(io) shouldBe 2
    counter shouldBe 2
  }
}
