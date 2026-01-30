package crawler.html

sealed trait DOMObject

final case class DOMText(text : String) extends DOMObject
final case class DOMNonVoidTag(tagType: String, children: List[DOMObject],
                        attrs: Map[String, String]) extends DOMObject
final case class DOMVoidTag(tagType: String, attrs: Map[String, String]) extends DOMObject

sealed trait TagKind

case object NonVoidTag extends TagKind
case object VoidTag extends TagKind
case object Invalid extends TagKind

// TODO : Eventually when you add the checks for void tags you should also check non valid
def tagKind(tag: String): TagKind = {
//  println("CALLING TAG TYPE ON")
//  println(tag)
  val voidTags: List[String] = List("<area>", "<base>", "<br>", "<col>", "<embed>", "<hr>", "<img>", "<input>",
      "<link>", "<meta>", "<param>", "<source>", "<tract>", "<wbr>")
  
  if(voidTags.contains(tag)) {
    return VoidTag
  }

  NonVoidTag
}