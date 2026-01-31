package crawler.html

sealed trait DOMObject

final case class DOMText(text : String) extends DOMObject
final case class DOMNonVoidTag(tagType: String, children: List[DOMObject],
                        attrs: Map[String, String]) extends DOMObject
final case class DOMVoidTag(tagType: String, attrs: Map[String, String]) extends DOMObject