package crawler.html

import scala.util.matching.Regex

// starts from an index and finds the corresponding closing tag and returns the full tag if it exists
private def getFullTagGivenLeftBracketIndex(html: String, index: Int): Option[String] = {
  var end: Int = html.indexOf('>', index)
  if (end == -1) {
    return None
  }

  if(html.charAt(end) == '/') {
    end -= - 1
  }

  Some(html.substring(index, end + 1))
}

// <body> -> </body>
private def getClosingTagGivenTag(tag: String): String = {
  val (processedTag, processedAttr) = extractTagMetadata(tag)
  val tagSubstring: String = processedTag.substring(1)
  s"</$tagSubstring"
}

// sample input: <a class = "hello" href="link">. Will return (<a>, and a map with all of the key values)
private def extractTagMetadata(tag: String) : (String, Map[String, String]) = {
  // removes < in the front and > in the back
  var innerString: String = tag.drop(1).dropRight(1)
  if(innerString.last == '/') {
    innerString = innerString.dropRight(1).trim
  }

  // in the example input -> ["a", "class = "hello" href="link""]
  val splitFirstItem: Array[String] = innerString.split("\\s+", 2)

  val attributesMap = {
    if(splitFirstItem.length == 1) {Map[String, String]()}
    else {Map[String, String]()}
//    else {
//      splitFirstItem(1)
//        .split("\\s+")
//        .map { attr =>
//          val Array(k, v) = attr.split("=", 2)
//          k.trim -> v.trim.stripPrefix("\"").stripSuffix("\"")
//        }.toMap
//    }
  }

  (s"<${splitFirstItem.head}>", attributesMap)
}

object HTMLTagExtractor {
  // TODO : Strip out the DOCTYPE HTML
  // TODO : Nowhere are we handling non void tags yet
  // TODO : You have to strip out comments. Pattern match first <html> and </html>
  // TODO : Class can have multiple values seperated by spaces
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
              // May potentially be a void tag
              // TODO: This is going to be insanely janky, look at this later, also do it idiomatically
              // There is also a bug that if what is in between is junk it'll try parsing
              val potential_void_closing_tag: Int = html.indexOf(">", iteratingIndex)
              if(potential_void_closing_tag != -1) {
                val (processedTag, processedAttr) = extractTagMetadata(tag)
                val tagType: TagKind = tagKind(processedTag)

                tagType match {
//                  case VoidTag =>
//                    returnList = returnList :+ DOMVoidTag(processedTag, processedAttr)
//                    previousCommitedIndex = potential_void_closing_tag + 2
//                    iteratingIndex = previousCommitedIndex
                  case _ =>
//                    println("Do something here")
                    // throw Exception("Undefined exception regarding invalid match. This should only be a NonVoidTag if in here")
                }
              } else {
                currentString.append(html.charAt(iteratingIndex))
                iteratingIndex += 1
              }
            } // TODO : Essentially the issue is over here since non void tags don't close it stops right here
            else {
              // TODO : This insert is def a code smell as the list is immutable
              if(previousCommitedIndex != iteratingIndex) {
                returnList = returnList :+ DOMText(html.substring(previousCommitedIndex, iteratingIndex))
              }

              val (processedTag, processedAttr) = extractTagMetadata(tag)
              val tagType: TagKind = tagKind(processedTag)

              tagType match {
//                case NonVoidTag =>
//                  returnList = returnList :+ DOMNonVoidTag(processedTag, parseHTML(html, iteratingIndex + tag.length,
//                    closingTagIndex - 1), processedAttr)
//                case VoidTag =>
//                  returnList = returnList :+ DOMVoidTag(processedTag, processedAttr)
                case _ =>
                  throw Exception("Undefined exception regarding invalid match. This should only be a VoidTag if in here")
              }

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
      returnList = returnList :+ DOMText(html.substring(previousCommitedIndex, iteratingIndex + 1))
    }

    returnList
  }

  def parseHTML(html:String): List[DOMObject] = {
    parseHTML(html, html.indexOf("<html"), html.indexOf("</html>") + "</html>".length)
  }
}