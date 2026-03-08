package core

import crawler.core.{
  StepResult,
  Workflow,
  WorkflowExecution,
  WorkflowFailure,
  WorkflowStep,
  WorkflowSuccess,
  WorkflowTransition,
  executeEntireWorkflow,
  executeWorkflowStep
}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class WorkflowExecutionSpec extends AnyFlatSpec with Matchers {

  // Minimal typed context for tests — just tracks which steps ran
  class TestContext {
    val ran: scala.collection.mutable.ListBuffer[String] = scala.collection.mutable.ListBuffer.empty
  }

  case class RecordingStep(name: String, result: StepResult) extends WorkflowStep[TestContext] {
    override def run(input: TestContext): StepResult = {
      input.ran += name
      result
    }
  }

  "executeWorkflowStep" should "advance to success transition when step succeeds" in {
    val step1 = RecordingStep("step1", WorkflowSuccess)
    val step2 = RecordingStep("step2", WorkflowSuccess)

    val workflow = new Workflow[TestContext] {
      override val startingStep: WorkflowStep[TestContext] = step1
      override val transitions: Map[WorkflowStep[TestContext], WorkflowTransition[TestContext]] = Map(
        step1 -> WorkflowTransition(Some(step2), None),
        step2 -> WorkflowTransition(None, None)
      )
    }

    val ctx = TestContext()
    val execution = WorkflowExecution(workflow, ctx)
    executeWorkflowStep(execution)

    ctx.ran.toList shouldBe List("step1")
    execution.currentState shouldBe Some(step2)
  }

  it should "advance to failure transition when step fails" in {
    val step1    = RecordingStep("step1", WorkflowFailure)
    val fallback = RecordingStep("fallback", WorkflowSuccess)

    val workflow = new Workflow[TestContext] {
      override val startingStep: WorkflowStep[TestContext] = step1
      override val transitions: Map[WorkflowStep[TestContext], WorkflowTransition[TestContext]] = Map(
        step1    -> WorkflowTransition(None, Some(fallback)),
        fallback -> WorkflowTransition(None, None)
      )
    }

    val ctx = TestContext()
    val execution = WorkflowExecution(workflow, ctx)
    executeWorkflowStep(execution)

    execution.currentState shouldBe Some(fallback)
    ctx.ran.toList shouldBe List("step1")
  }

  it should "stop the workflow if a transition is missing for the current step" in {
    val lonelyStep = RecordingStep("lonely", WorkflowSuccess)

    val workflow = new Workflow[TestContext] {
      override val startingStep: WorkflowStep[TestContext] = lonelyStep
      override val transitions: Map[WorkflowStep[TestContext], WorkflowTransition[TestContext]] = Map.empty
    }

    val ctx = TestContext()
    val execution = WorkflowExecution(workflow, ctx)
    executeWorkflowStep(execution)

    ctx.ran.toList shouldBe List("lonely")
    execution.currentState shouldBe None
  }

  it should "do nothing when the workflow is already complete" in {
    val step = RecordingStep("step", WorkflowSuccess)

    val workflow = new Workflow[TestContext] {
      override val startingStep: WorkflowStep[TestContext] = step
      override val transitions: Map[WorkflowStep[TestContext], WorkflowTransition[TestContext]] = Map(
        step -> WorkflowTransition(None, None)
      )
    }

    val ctx = TestContext()
    val execution = WorkflowExecution(workflow, ctx)
    execution.currentState = None

    executeWorkflowStep(execution)

    ctx.ran shouldBe empty
  }

  "executeEntireWorkflow" should "run all steps until terminal state" in {
    val step1 = RecordingStep("step1", WorkflowSuccess)
    val step2 = RecordingStep("step2", WorkflowSuccess)
    val step3 = RecordingStep("step3", WorkflowSuccess)

    val workflow = new Workflow[TestContext] {
      override val startingStep: WorkflowStep[TestContext] = step1
      override val transitions: Map[WorkflowStep[TestContext], WorkflowTransition[TestContext]] = Map(
        step1 -> WorkflowTransition(Some(step2), None),
        step2 -> WorkflowTransition(Some(step3), None),
        step3 -> WorkflowTransition(None, None)
      )
    }

    val ctx = TestContext()
    val execution = WorkflowExecution(workflow, ctx)
    executeEntireWorkflow(execution)

    ctx.ran.toList shouldBe List("step1", "step2", "step3")
    execution.currentState shouldBe None
  }
}
