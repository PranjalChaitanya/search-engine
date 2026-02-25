package crawler.core

import crawler.core.Config

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID.randomUUID

object Logger {
  private val filename =
    Config.logPath + "/" + randomUUID().toString + ".txt"

  private val path = os.Path(filename)

  os.makeDir.all(path / os.up)

  def log(logText: String): Unit = {
    if (Config.loggerEnabled) {
      val formatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

      val formattedDateTime =
        LocalDateTime.now().format(formatter)

      val line = s"Log $formattedDateTime: $logText\n"

      os.write.append(path, line)
      println(line)
    }
  }
}