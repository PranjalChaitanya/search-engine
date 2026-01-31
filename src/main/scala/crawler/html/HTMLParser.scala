package crawler.html

import crawler.html.HTMLObjectToken.{CloseTagToken, CommentToken, DoctypeToken, OpenTagToken, VoidTagToken, TextToken}

import scala.collection.mutable.ListBuffer

private def isMatchingClosingTag(openTag: String, closingTagObject: HTMLObjectToken) : Boolean = {
  closingTagObject match {
    case CloseTagToken(closingTagName : String) =>
      closingTagName == openTag
    case _ => false
  }
}

private def parseTokenStream(tokenStream: List[HTMLObjectToken], index: Int) : (ListBuffer[DOMObject], Int) = {
  val domObjectList: ListBuffer[DOMObject] = ListBuffer[DOMObject]()
  var iterationIndex: Int = index + 1

  val currentOpenTag: String = tokenStream(index).asInstanceOf[OpenTagToken].tagName
  while(iterationIndex + 1 < tokenStream.length) {
    
    tokenStream(iterationIndex) match {
      case OpenTagToken(openTagName, openTagAttrs: Map[String, String]) =>
        val (childrenList, jumpIndex) = parseTokenStream(tokenStream, iterationIndex)
        domObjectList += DOMNonVoidTag(openTagName, childrenList.toList, openTagAttrs)
        iterationIndex = jumpIndex
      case CloseTagToken(closingTagName: String) =>
        if(isMatchingClosingTag(currentOpenTag, tokenStream(iterationIndex))) {
          return (domObjectList, iterationIndex)
        }
        // Otherwise there is an error. HTML Parsers usually ignore closing tags without open tags and proceed normally
      case VoidTagToken(voidTagName: String, openTagAttrs: Map[String, String]) =>
        domObjectList += DOMVoidTag(voidTagName, openTagAttrs)
      case TextToken(text: String) =>
        domObjectList += DOMText(text)
      case _ =>
    }
    iterationIndex += 1
  }

  (domObjectList, tokenStream.length)
}

object HTMLParser {
  def parseHTML(html:String): List[DOMObject] = {
    val parsedHTMLString: String = s"<dummy>$html</dummy>"

    val tokenizedHTML: List[HTMLObjectToken] = HTMLTokenizer.tokenize(parsedHTMLString).filterNot(x => {
      x.isInstanceOf[CommentToken] || x.isInstanceOf[DoctypeToken]
    })

    parseTokenStream(tokenizedHTML, 0)(0).toList
  }
}