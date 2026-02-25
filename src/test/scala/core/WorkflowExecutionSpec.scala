package core

import crawler.core.{
  StepResult,
  Workflow,
  WorkflowContext,
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

  case class RecordingStep(name: String, result: StepResult, ran: scala.collection.mutable.ListBuffer[String]) extends WorkflowStep {
    override def run(input: WorkflowContext): StepResult = {
      ran += name
      input.ctx.put(s"ran-$name", true)
      result
    }
  }

  "executeWorkflowStep" should "advance to success transition when step succeeds" in {
    val ran = scala.collection.mutable.ListBuffer.empty[String]
    val step1 = RecordingStep("step1", WorkflowSuccess, ran)
    val step2 = RecordingStep("step2", WorkflowSuccess, ran)

    val workflow = new Workflow {
      override val startingStep: WorkflowStep = step1
      override val transitions: Map[WorkflowStep, WorkflowTransition] = Map(
        step1 -> WorkflowTransition(Some(step2), None),
        step2 -> WorkflowTransition(None, None)
      )
    }

    val execution = new WorkflowExecution(workflow)
    executeWorkflowStep(execution)

    ran.toList shouldBe List("step1")
    execution.currentState shouldBe Some(step2)
    execution.workflowContext.ctx.contains("ran-step1") shouldBe true
  }

  it should "advance to failure transition when step fails" in {
    val ran = scala.collection.mutable.ListBuffer.empty[String]
    val step1 = RecordingStep("step1", WorkflowFailure, ran)
    val fallback = RecordingStep("fallback", WorkflowSuccess, ran)

    val workflow = new Workflow {
      override val startingStep: WorkflowStep = step1
      override val transitions: Map[WorkflowStep, WorkflowTransition] = Map(
        step1 -> WorkflowTransition(None, Some(fallback)),
        fallback -> WorkflowTransition(None, None)
      )
    }

    val execution = new WorkflowExecution(workflow)
    executeWorkflowStep(execution)

    execution.currentState shouldBe Some(fallback)
    ran.toList shouldBe List("step1")
  }

  it should "stop the workflow if a transition is missing for the current step" in {
    val ran = scala.collection.mutable.ListBuffer.empty[String]
    val lonelyStep = RecordingStep("lonely", WorkflowSuccess, ran)

    val workflow = new Workflow {
      override val startingStep: WorkflowStep = lonelyStep
      override val transitions: Map[WorkflowStep, WorkflowTransition] = Map.empty
    }

    val execution = new WorkflowExecution(workflow)
    executeWorkflowStep(execution)

    ran.toList shouldBe List("lonely")
    execution.currentState shouldBe None
  }

  it should "do nothing when the workflow is already complete" in {
    val ran = scala.collection.mutable.ListBuffer.empty[String]
    val step = RecordingStep("step", WorkflowSuccess, ran)

    val workflow = new Workflow {
      override val startingStep: WorkflowStep = step
      override val transitions: Map[WorkflowStep, WorkflowTransition] = Map(step -> WorkflowTransition(None, None))
    }

    val execution = new WorkflowExecution(workflow)
    execution.currentState = None

    executeWorkflowStep(execution)

    ran shouldBe empty
  }

  "executeEntireWorkflow" should "run all steps until terminal state" in {
    val ran = scala.collection.mutable.ListBuffer.empty[String]
    val step1 = RecordingStep("step1", WorkflowSuccess, ran)
    val step2 = RecordingStep("step2", WorkflowSuccess, ran)
    val step3 = RecordingStep("step3", WorkflowSuccess, ran)

    val workflow = new Workflow {
      override val startingStep: WorkflowStep = step1
      override val transitions: Map[WorkflowStep, WorkflowTransition] = Map(
        step1 -> WorkflowTransition(Some(step2), None),
        step2 -> WorkflowTransition(Some(step3), None),
        step3 -> WorkflowTransition(None, None)
      )
    }

    val execution = new WorkflowExecution(workflow)
    executeEntireWorkflow(execution)

    ran.toList shouldBe List("step1", "step2", "step3")
    execution.currentState shouldBe None
  }
}
