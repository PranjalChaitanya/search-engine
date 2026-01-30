package crawler.html

import crawler.html.HTMLObjectToken.{CloseTagToken, CommentToken, OpenTagToken, RawTagToken, Tag, TextToken, VoidTagToken}

import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

sealed trait HTMLObjectToken

object HTMLObjectToken {
  final case class TextToken(text: String) extends HTMLObjectToken

  sealed trait Tag extends HTMLObjectToken

  // Parsing is done in 2 phases. First parse a tag as a RawTag and the refine it to the specific type
  final case class RawTagToken(tag: String) extends Tag

  // Specific Tag types
  final case class OpenTagToken(tagName: String, attrs: Map[String, String]) extends Tag
  final case class CloseTagToken(tagName: String) extends Tag
  final case class VoidTagToken(tagName: String, attrs: Map[String, String]) extends Tag

  final case class CommentToken(comment : String) extends HTMLObjectToken
}

// Types that are used internally to classify and return token kinds
private trait TagKind
private case object VoidKind extends TagKind
private case object NonVoidKind extends TagKind

// returns whether a tag is void or not
private def tagKind(tag: String): TagKind = {
  val voidTags: List[String] = List("area", "base", "br", "col", "embed", "hr", "img", "input",
    "link", "meta", "param", "source", "tract", "wbr")

  if (voidTags.contains(tag)) {
    return VoidKind
  }

  NonVoidKind
}

// None return means that it's not a valid tag
private def tagName(tag: String) : Option[String] = {
  val tagRegex : Regex = """<\s*/?\s*([a-zA-Z0-9:-]*).*>""".r

  tagRegex.findFirstMatchIn(tag).map(_.group(1))
}

private def extractTagAttributes(fullTag: String) : Map[String, String] = {
  // strips out attributes like in <a href=""> it will identify he href=""
  val attributeRegex: Regex = """([^\s]*)\s*=\s*\"([^\s]*)\"""".r

  val attributeMatchesIterator: Regex.MatchIterator = attributeRegex.findAllIn(fullTag)

  attributeRegex.findAllIn(fullTag).toArray.map(attr => {
    val Array(attrType, attrValue) = attr.split("=", 2)
    attrType.trim -> attrValue.trim.stripPrefix("\"").stripSuffix("\"")
  }).toMap
}

private def createClosedTagToken(tagName: String) : Tag = {
  CloseTagToken(tagName)
}

// TODO : bad interface that you need to pass both of these in, given that you can extract tagName from fullTag
private def createOpenTagToken(fullTag: String, tagName: String) : Tag = {
  OpenTagToken(tagName, extractTagAttributes(fullTag))
}

private def createVoidTagToken(fullTag: String, tagName: String) : Tag = {
  VoidTagToken(tagName, extractTagAttributes(fullTag))
}

private def createCommentToken(fullTag: String) : Option[CommentToken] = {
  // comes from <!-- and -->
  val minimumCommentLength: Int = 7

  if(fullTag.length >= minimumCommentLength && fullTag.substring(0, 4) == "<!--"
    && fullTag.substring(fullTag.length - 3) == "-->") {
    return Some(CommentToken(fullTag.substring(4, fullTag.length - 3)))
  }

  None
}

private def refineToken(token: HTMLObjectToken) : Option[HTMLObjectToken] = {
  token match {
    // only raw tag tokens need to be refined
    case RawTagToken(tag: String) =>
      val parsedCommentToken : Option[CommentToken] = createCommentToken(tag)
      parsedCommentToken match {
        case Some(token: CommentToken) =>
          return parsedCommentToken
        case None =>
      }

      val passedTagName : Option[String] = tagName(tag)

      // checking whether this is a valid tag
      passedTagName match {
        case Some(tagName: String) =>
          val voidOrNonVoidTagKind: TagKind = tagKind(tagName)

          voidOrNonVoidTagKind match {
            case NonVoidKind =>
              val closingTagRegex : Regex = """</.*>""".r
              val closingTagMatch : Option[Regex.Match] = closingTagRegex.findFirstMatchIn(tag)

              closingTagMatch match {
                case Some(tag: Regex.Match) =>
                  Some(createClosedTagToken(tagName))
                case None =>
                  Some(createOpenTagToken(tag, tagName))
              }
            case VoidKind =>
              Some(createVoidTagToken(tag, tagName))
          }

        case None =>
          None
      }
    case _ =>
      Some(token)
  }
}

object HTMLTokenizer {
  def tokenize(html: String) : ListBuffer[HTMLObjectToken] = {
    val listBuffer : ListBuffer[HTMLObjectToken] = ListBuffer.empty[HTMLObjectToken]
    val stringBuffer : StringBuilder = StringBuilder("")

    var openTag: Boolean = false

    for(htmlChar <- html) {
      htmlChar match {
        case '<' =>
          if(stringBuffer.nonEmpty) {
            listBuffer += TextToken(stringBuffer.toString())
          }
          stringBuffer.clear()
          openTag = true
          stringBuffer.append(htmlChar)
        case '>' =>
          stringBuffer.append(htmlChar)
          if(stringBuffer.nonEmpty) {
            listBuffer += RawTagToken(stringBuffer.toString())
          }
          stringBuffer.clear()
          openTag = false
        case _ =>
          stringBuffer.append(htmlChar)
      }
    }

    // Second phase of refining the tokens
    val refinedTokens = listBuffer.flatMap(refineToken)

    refinedTokens
  }
}

object testRuns {
  @main
  def mainFunc = {
//    println(isTag("</a>"))
//    println(isTag("</h1>"))
//    println(isTag("<h1>"))
//    println(isTag("<br>"))
//    println(isTag("<br/>"))
//    println(isTag("<img src = \"fdsfdsfdsfdsfdsfsd\" />"))
//    println(isTag("</a href = \"fsdfdsfdsfdsfdsfds\">"))
//    println(isTag("<--/a href = \"fsdfdsfdsfdsfdsfds\"-->"))
//    println(isTag("< fdsfdsfdsfsdfsdfdsfsd"))

//    </a href = "fsdfdsfdsfdsfdsfds">
//    println(createOpenTagToken("</a href = \"fsdfdsfdsfdsfdsfds\">", "a"))
    println(HTMLTokenizer.tokenize("<!-- html comment --><html><br /> Hello this <p>Basic <ul><li>Deep list</li></ul> paragraph <a href=\"https://gossim.com\"> ref</a></p> is basic html text</html>"))
//    println(HTMLTokenizer.tokenize("</html>"))
//    println(HTMLTokenizer.tokenize("<br />"))
//    println(HTMLTokenizer.tokenize("<a href = \"fsdfdsfdsfdsfdsfds\">"))
  }
}