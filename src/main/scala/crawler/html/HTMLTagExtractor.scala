package crawler.html

// starts from an index and finds the corresponding closing tag and returns the full tag if it exists
private def getFullTagGivenLeftBracketIndex(html: String, index: Int): Option[String] = {
  val end: Int = html.indexOf('>', index)
  if (end == -1) {
    None
  }

  Some(html.substring(index, end + 1))
}

// <body> -> </body>
private def getClosingTagGivenTag(tag: String): String = {
  "</".concat(tag.substring(1))
}

object HTMLTagExtractor {
  // TODO : Strip out the DOCTYPE HTML
  // TODO : Nowhere are we handling non void tags yet
  // TODO : Nowhere are attributes parsed
  def parseHTML(html: String, startIndex: Int, endIndex: Int): List[DOMObject] = {
    println(s"$startIndex, $endIndex")
    var iteratingIndex: Int = startIndex
    var previousCommitedIndex = startIndex

    var currentString: StringBuilder = StringBuilder("")
    var returnList: List[DOMObject] = List()

    while(iteratingIndex < endIndex) {
      if(html.charAt(iteratingIndex) == '<') {
        val fullTag: Option[String] = getFullTagGivenLeftBracketIndex(html, iteratingIndex)

        fullTag match {
          case None =>
            currentString.append(html.charAt(iteratingIndex))
            iteratingIndex += 1
          case Some(tag) =>
            val closingTag: String = getClosingTagGivenTag(tag)
            val closingTagIndex: Int = html.indexOf(closingTag, iteratingIndex)

            if(closingTagIndex == -1) {
              currentString.append(html.charAt(iteratingIndex))
              iteratingIndex += 1
            } else {
              // TODO : This insert is def a code smell as the list is immutable
              if(previousCommitedIndex != iteratingIndex) {
                returnList = returnList :+ DOMText(html.substring(previousCommitedIndex, iteratingIndex))
              }

              returnList = returnList :+ DOMNonVoidTag("<a>", parseHTML(html, iteratingIndex + tag.length,
                closingTagIndex - 1), Map("A" -> "A", "B" -> "A"))

              previousCommitedIndex = closingTagIndex + closingTag.length
              iteratingIndex = previousCommitedIndex
              // TODO : Code smell making it a var, can prob empty using a function
              currentString = StringBuilder("")
            }
        }
      } else {
        currentString.append(html.charAt(iteratingIndex))
        iteratingIndex += 1
      }
    }

    if(currentString.toString() != "") {
      returnList = returnList :+ DOMText(html.substring(previousCommitedIndex, iteratingIndex))
    }

    returnList
  }
}