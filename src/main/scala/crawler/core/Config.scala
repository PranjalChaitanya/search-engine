package crawler.core

object Config {
  val loggerEnabled: Boolean = true
  val logPath: String = "/Users/pranjalchaitanya/Documents/search-engine/src/main/scala/crawler/logs"

  val executionEngineNormalThreads: Integer = 10
  val executionEngineUrgentThreads: Integer = 2
}
