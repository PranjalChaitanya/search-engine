package crawler.concurrency

import crawler.core.{FutureState}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class AsyncQueueSpec extends AnyFlatSpec with Matchers {

  // --------------------------------------------------
  // Helpers
  // --------------------------------------------------

  def pollReady[A](f: crawler.core.Future[A]): A =
    f.poll(() => ()) match {
      case FutureState.READY(v) => v
      case other => fail(s"Expected READY but got $other")
    }

  def pollPending[A](f: crawler.core.Future[A]): Unit =
    f.poll(() => ()) shouldBe FutureState.PENDING

  // --------------------------------------------------
  // Basic correctness
  // --------------------------------------------------

  "AsyncQueue" should "return item immediately when available" in {
    val q = new AsyncQueue[Int]
    q.push(42)

    val f = q.pop()
    pollReady(f) shouldBe 42
  }

  it should "return PENDING when popping from empty queue" in {
    val q = new AsyncQueue[Int]
    val f = q.pop()

    pollPending(f)
  }

  // --------------------------------------------------
  // Wakeup semantics
  // --------------------------------------------------

  it should "wake a waiting pop when item is pushed" in {
    val q = new AsyncQueue[Int]
    var woken = false

    val f = q.pop()
    f.poll(() => woken = true) shouldBe FutureState.PENDING

    woken shouldBe false

    q.push(99)
    woken shouldBe true

    pollReady(f) shouldBe 99
  }

  it should "only register callback once" in {
    val q = new AsyncQueue[Int]
    var wakeCount = 0

    val f = q.pop()

    f.poll(() => wakeCount += 1)
    f.poll(() => wakeCount += 1)
    f.poll(() => wakeCount += 1)

    q.push(1)
    wakeCount shouldBe 1
  }

  // --------------------------------------------------
  // FIFO correctness
  // --------------------------------------------------

  it should "wake waiters in FIFO order" in {
    val q = new AsyncQueue[Int]
    val events = scala.collection.mutable.ArrayBuffer[String]()

    val f1 = q.pop()
    val f2 = q.pop()

    f1.poll(() => events += "woke1")
    f2.poll(() => events += "woke2")

    q.push(1)
    q.push(2)

    events shouldBe Seq("woke1", "woke2")

    pollReady(f1) shouldBe 1
    pollReady(f2) shouldBe 2
  }

  // --------------------------------------------------
  // Concurrency (stress / safety)
  // --------------------------------------------------

  it should "handle concurrent push and pop correctly" in {
    val q = new AsyncQueue[Int]
    val results = new java.util.concurrent.ConcurrentLinkedQueue[Int]()

    val consumer = new Thread(() => {
      (1 to 1000).foreach { _ =>
        val f = q.pop()
        while (f.poll(() => ()) == FutureState.PENDING) {
          Thread.`yield`()
        }
        f.poll(() => ()) match {
          case FutureState.READY(v) => results.add(v)
          case _ => ()
        }
      }
    })

    val producer = new Thread(() => {
      (1 to 1000).foreach(q.push)
    })

    consumer.start()
    producer.start()
    consumer.join()
    producer.join()

    results.size() shouldBe 1000
  }

  // --------------------------------------------------
  // Safety / non-crashing guarantees
  // --------------------------------------------------

  it should "not crash when pop is polled repeatedly" in {
    val q = new AsyncQueue[Int]
    val f = q.pop()

    noException shouldBe thrownBy {
      (1 to 100).foreach(_ => f.poll(() => ()))
    }
  }

  it should "not crash when push happens with no waiters" in {
    val q = new AsyncQueue[Int]

    noException shouldBe thrownBy {
      (1 to 100).foreach(q.push)
    }
  }
}
