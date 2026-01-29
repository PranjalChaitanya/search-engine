package crawler.html

sealed trait DOMObject

final case class DOMText(text : String) extends DOMObject
final case class DOMNonVoidTag(tagType: String, children: List[DOMObject],
                        attrs: Map[String, String]) extends DOMObject
final case class DOMVoidTag(tagType: String, attrs: Map[String, String]) extends DOMObject

type DOMTag = DOMNonVoidTag | DOMVoidTag

def isVoidTag(tag: String): Boolean = {
  val voidTags: List[String] = List("area", "base", "br", "col", "embed", "hr", "img", "input", 
      "link", "meta", "param", "source", "tract", "wbr")
  
  voidTags.contains(tag)
}

// TODO : Insert all of the non void tags later. This may be time consuming
def isNonVoidTag(tag: String) : Boolean = {
  true
}

def isDOMTag(tag: String) : Boolean = {
  isVoidTag(tag) || isNonVoidTag(tag)
}