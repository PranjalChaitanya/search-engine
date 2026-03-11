package crawler.engine

sealed trait Priority
object Priority {
  case object HIGH   extends Priority
  case object NORMAL extends Priority
}
