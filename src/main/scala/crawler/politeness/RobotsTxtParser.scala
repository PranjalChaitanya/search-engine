package crawler.politeness

/** Parsed robots.txt rules applicable to our crawler's user agent. */
case class RobotsTxtRules(
  disallowedPaths: List[String],
  allowedPaths: List[String],
  crawlDelaySeconds: Option[Int]
)

object RobotsTxtParser {
  val crawlerUserAgent: String = "search-engine-bot"

  /**
   * Parse the raw text content of a robots.txt file and return the rules
   * that apply to our crawler. Exact user-agent match takes precedence over
   * the wildcard (*) catch-all group.
   */
  def parse(content: String): RobotsTxtRules = {
    case class Group(userAgents: List[String], directives: List[(String, String)])

    val groups = scala.collection.mutable.ListBuffer.empty[Group]
    val currentAgents = scala.collection.mutable.ListBuffer.empty[String]
    val currentDirectives = scala.collection.mutable.ListBuffer.empty[(String, String)]
    var inDirectiveBlock = false

    def flushGroup(): Unit =
      if (currentAgents.nonEmpty) {
        groups += Group(currentAgents.toList, currentDirectives.toList)
        currentAgents.clear()
        currentDirectives.clear()
        inDirectiveBlock = false
      }

    val lines = content.split("\n").map(_.trim).filterNot(l => l.isEmpty || l.startsWith("#"))

    for (line <- lines) {
      parseLine(line) match {
        case Some(("user-agent", value)) =>
          // A new User-agent line after directives starts a new group
          if (inDirectiveBlock) flushGroup()
          currentAgents += value

        case Some((key, value)) =>
          inDirectiveBlock = true
          currentDirectives += (key -> value)

        case None => // malformed line, skip
      }
    }
    flushGroup()

    // Prefer exact user-agent match; fall back to wildcard group
    val applicable =
      groups.find(_.userAgents.exists(_.equalsIgnoreCase(crawlerUserAgent)))
        .orElse(groups.find(_.userAgents.contains("*")))

    applicable match {
      case None => RobotsTxtRules(List.empty, List.empty, None)
      case Some(group) =>
        val disallowed = group.directives.collect { case ("disallow", v) if v.nonEmpty => v }
        val allowed    = group.directives.collect { case ("allow",    v) if v.nonEmpty => v }
        val crawlDelay = group.directives.collectFirst { case ("crawl-delay", v) => v }.flatMap(_.toIntOption)
        RobotsTxtRules(disallowed, allowed, crawlDelay)
    }
  }

  /**
   * Check whether a URL path (and optional query string) is permitted by the
   * given rules. The most specific matching rule wins; Allow beats Disallow
   * on equal specificity (per the de-facto standard).
   */
  def isPathAllowed(path: String, rules: RobotsTxtRules): Boolean = {
    val allowMatch    = rules.allowedPaths.filter(pathMatches(path, _)).maxByOption(_.length)
    val disallowMatch = rules.disallowedPaths.filter(pathMatches(path, _)).maxByOption(_.length)

    (allowMatch, disallowMatch) match {
      case (None, None)       => true
      case (Some(_), None)    => true
      case (None, Some(_))    => false
      case (Some(a), Some(d)) => a.length >= d.length // Allow wins on tie
    }
  }

  // ---- private helpers ----

  /** Returns true when `path` is matched by `pattern` (supports * wildcard and $ end-anchor). */
  private def pathMatches(path: String, pattern: String): Boolean = {
    if (pattern.isEmpty) return false

    val anchored       = pattern.endsWith("$")
    val cleanPattern   = if (anchored) pattern.dropRight(1) else pattern

    if (!cleanPattern.contains("*")) {
      // Simple prefix (or exact when anchored)
      if (anchored) path == cleanPattern
      else path.startsWith(cleanPattern)
    } else {
      // Wildcard matching: split on * and verify segments appear in order
      val segments = cleanPattern.split("\\*", -1)
      var pos      = 0
      var ok       = true

      for ((seg, idx) <- segments.zipWithIndex if ok) {
        if (idx == 0) {
          if (!path.startsWith(seg)) ok = false
          else pos = seg.length
        } else {
          val found = path.indexOf(seg, pos)
          if (found == -1) ok = false
          else pos = found + seg.length
        }
      }

      // When anchored, the entire path must have been consumed
      ok && (!anchored || pos == path.length)
    }
  }

  /** Split a robots.txt line on the first colon and normalise the key. */
  private def parseLine(line: String): Option[(String, String)] = {
    val colonIdx = line.indexOf(':')
    if (colonIdx == -1) None
    else {
      val key   = line.substring(0, colonIdx).trim.toLowerCase
      val raw   = line.substring(colonIdx + 1).trim
      val value = raw.split("#").head.trim // strip inline comments
      Some(key -> value)
    }
  }
}
