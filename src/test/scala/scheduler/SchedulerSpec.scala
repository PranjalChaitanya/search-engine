package scheduler

import crawler.engine.ExecutionEngine
import crawler.scheduler.{ScheduledTask, Scheduler}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SchedulerSpec extends AnyFlatSpec with Matchers {

  // A task that returns a fixed list of jobs, tracking how many times it was called
  class StubTask(jobs: List[() => Unit]) extends ScheduledTask {
    var callCount = 0
    def getTasks(): List[() => Unit] = { callCount += 1; jobs }
  }

  // Captures submitted jobs without running them
  class CapturingEngine extends ExecutionEngine(0) {
    val submitted = scala.collection.mutable.ListBuffer.empty[() => Unit]
    override def submitJob(job: () => Unit): Unit = submitted += job
  }

  // Runs a single tick rather than starting the thread
  def tick(scheduler: Scheduler): Unit = {
    scheduler.scheduledTaskList.foreach(task =>
      task.getTasks().foreach(scheduler.executionEngine.submitJob)
    )
  }

  "Scheduler" should "submit jobs from a single task" in {
    val engine = CapturingEngine()
    var ran = false
    val task = StubTask(List(() => ran = true))
    val scheduler = Scheduler(List(task), engine)

    tick(scheduler)

    engine.submitted should have length 1
  }

  it should "submit jobs from multiple tasks" in {
    val engine = CapturingEngine()
    val task1 = StubTask(List(() => (), () => ()))
    val task2 = StubTask(List(() => ()))
    val scheduler = Scheduler(List(task1, task2), engine)

    tick(scheduler)

    engine.submitted should have length 3
  }

  it should "submit nothing when all tasks return empty lists" in {
    val engine = CapturingEngine()
    val task = StubTask(List.empty)
    val scheduler = Scheduler(List(task), engine)

    tick(scheduler)

    engine.submitted shouldBe empty
  }

  it should "call getTasks on every task each tick" in {
    val engine = CapturingEngine()
    val task1 = StubTask(List.empty)
    val task2 = StubTask(List.empty)
    val scheduler = Scheduler(List(task1, task2), engine)

    tick(scheduler)

    task1.callCount shouldBe 1
    task2.callCount shouldBe 1
  }

  it should "handle a mix of empty and non-empty tasks" in {
    val engine = CapturingEngine()
    val emptyTask = StubTask(List.empty)
    val activeTask = StubTask(List(() => (), () => ()))
    val scheduler = Scheduler(List(emptyTask, activeTask), engine)

    tick(scheduler)

    engine.submitted should have length 2
  }

  it should "submit jobs from all tasks on each tick independently" in {
    val engine = CapturingEngine()
    val task = StubTask(List(() => ()))
    val scheduler = Scheduler(List(task), engine)

    tick(scheduler)
    tick(scheduler)

    task.callCount shouldBe 2
    engine.submitted should have length 2
  }
}
