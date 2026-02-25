package engine

import crawler.engine.ExecutionEngine
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.concurrent.Futures.PatienceConfig
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}

import scala.collection.concurrent.TrieMap

class BasicExecutionEngineTest extends AnyFlatSpec with Matchers {
  implicit val patience: PatienceConfig =
    PatienceConfig(timeout = Span(2, Seconds), interval = Span(20, Millis))

  it should "basic engine test handling basic jobs" in {
    val executionEngine: ExecutionEngine = ExecutionEngine(4)
    val results = TrieMap.empty[Int, Int]

    executionEngine.start()

    (0 until 100).foreach(i => {
      executionEngine.submitJob(() => {
        results.put(i, 1)
      })
    })

    eventually {
      results.size shouldBe 100
      all(results.values) shouldBe 1
    }

    executionEngine.shutdown()
  }

  it should "handle jobs that create more jobs" in {
    val executionEngine: ExecutionEngine = ExecutionEngine(4)
    val results = TrieMap.empty[Int, Int]

    executionEngine.start()

    // Parent jobs
    (0 until 20).foreach { parent =>
      executionEngine.submitJob(() => {

        // mark parent executed
        results.put(parent, 1)

        // each parent spawns 5 child jobs
        (0 until 5).foreach { child =>
          val childId = 1000 + parent * 10 + child

          executionEngine.submitJob(() => {
            results.put(childId, 1)
          })
        }
      })
    }

    eventually {
      // 20 parents + (20 * 5) children
      results.size shouldBe 120
      all(results.values) shouldBe 1
    }

    executionEngine.shutdown()
  }
}
