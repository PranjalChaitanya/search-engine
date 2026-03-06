package workflows

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable

class PersonalTest extends AnyFlatSpec with Matchers {
  it should "this is a personal test" in {
    println("Personal test initialized")

    object MinOrdering extends Ordering[Int] {
      override def compare(x: Int, y: Int): Int = {
        if (x > y) {
          return -1
        }

        return 1
      }
    }

    val ordered_priority_queue = mutable.PriorityQueue.empty[Int](Ordering[Int].reverse)

    ordered_priority_queue.addOne(1)
    ordered_priority_queue.addOne(2)
    ordered_priority_queue.addOne(3)
    ordered_priority_queue.addOne(4)
    ordered_priority_queue.addOne(5)
    ordered_priority_queue.addOne(6)
    ordered_priority_queue.addOne(7)

    while(ordered_priority_queue.nonEmpty) {
      println(ordered_priority_queue.dequeue())
    }
  }
}
