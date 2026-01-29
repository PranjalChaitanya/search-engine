package crawler.core

sealed trait IO[A] {
  def flatMap[B](f: A => IO[B]): IO[B] =
    IO.FlatMap(this, f)

  def map[B](f: A => B): IO[B] =
    flatMap(a => IO.Pure(f(a)))
}

// TODO : Deprecate the use of this in favor of future
object IO {
  // A value that already exists
  private[core] final case class Pure[A](value: A) extends IO[A]

  // A delayed computation, pass in the effects
  private [core] final case class Delay[A](effect: () => A) extends IO[A]

  // Uses the output of call and decides what to do
  private [core] final case class FlatMap[A, B](input: IO[A], effect: A => IO[B]) extends IO[B]

  def pure[A](value: A) : IO[A] = Pure(value)

  def apply[A](effect: => A) : IO[A] = Delay(() => effect)

  def unsafeRun[A](io: IO[A]): A =
    run(io)(identity)

  private def run[A, R](io: IO[A])(k: A => R): R =
    io match {

      case IO.Pure(value) =>
        k(value)

      case IO.Delay(effect) =>
        k(effect())

      case IO.FlatMap(input, f) =>
        run(input) { a =>
          run(f(a))(k)
        }
    }
}