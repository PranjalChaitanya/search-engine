package crawler.html

import crawler.html.HTMLObjectToken.{CloseTagToken, CommentToken, DoctypeToken, OpenTagToken, RawTagToken, Tag, TextToken, VoidTagToken}

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
  final case class DoctypeToken() extends HTMLObjectToken
}

// Types that are used internally to classify and return token kinds
private trait TagKind
private case object VoidKind extends TagKind
private case object NonVoidKind extends TagKind

// returns whether a tag is void or not
private def tagKind(tag: String): TagKind = {
  val voidTags: List[String] = List("area", "base", "br", "col", "embed", "hr", "img", "input",
    "link", "meta", "param", "source", "tract", "wbr")

  if (voidTags.contains(tag)) VoidKind else NonVoidKind
}

// None return means that it's not a valid tag
private def extractTagName(tag: String) : Option[String] = {
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

private def createOpenTagToken(fullTag: String, tagName: String) : Tag = {
  OpenTagToken(tagName, extractTagAttributes(fullTag))
}

private def createVoidTagToken(fullTag: String, tagName: String) : Tag = {
  VoidTagToken(tagName, extractTagAttributes(fullTag))
}

private def parseCommentToken(fullTag: String) : Option[CommentToken] = {
  // comes from <!-- and -->
  val minimumCommentLength: Int = 7

  if(fullTag.length >= minimumCommentLength && fullTag.substring(0, 4) == "<!--"
    && fullTag.substring(fullTag.length - 3) == "-->") {
    return Some(CommentToken(fullTag.substring(4, fullTag.length - 3)))
  }

  None
}

private def parseDoctypeToken(tag: String) : Option[DoctypeToken] = {
  Option.when(tag == "<!DOCTYPE html>")(DoctypeToken())
}

private def parseTagToken(fullTag: String) : Option[Tag] = {
  extractTagName(fullTag).map(tagName => {
    val voidOrNonVoidTagKind: TagKind = tagKind(tagName)

    voidOrNonVoidTagKind match {
      case NonVoidKind =>
        if (fullTag.startsWith("</")) {
          createClosedTagToken(tagName)
        } else {
          createOpenTagToken(fullTag, tagName)
        }
      case VoidKind =>
        createVoidTagToken(fullTag, tagName)
    }
  })
}

private def refineRawTag(fullTag: String) : Option[HTMLObjectToken] = {
  parseDoctypeToken(fullTag)
    .orElse(parseCommentToken(fullTag))
    .orElse(parseTagToken(fullTag))
}

private def refineToken(token: HTMLObjectToken) : Option[HTMLObjectToken] = {
  token match {
    // only raw tag tokens need to be refined
    case RawTagToken(tag: String) =>
      refineRawTag(tag)
    case _ =>
      Some(token)
  }
}

object HTMLTokenizer {
  def tokenize(html: String) : List[HTMLObjectToken] = {
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

    if(stringBuffer.nonEmpty) {
      listBuffer += TextToken(stringBuffer.toString())
      stringBuffer.clear()
    }

    // Second phase of refining the tokens
    val refinedTokens : List[HTMLObjectToken] = listBuffer.flatMap(refineToken).toList

    refinedTokens
  }
}