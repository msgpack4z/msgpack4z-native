package msgpack4z

sealed abstract class OptBool extends Product with Serializable {
  def isEmpty: Boolean = this eq OptBool.Empty
  final def nonEmpty: Boolean = !isEmpty
  def toOption: Option[Boolean] = if (isEmpty) None else Some(get)
  def get: Boolean =
    this match {
      case OptBool.True => true
      case OptBool.False => false
      case OptBool.Empty => sys.error("OptBool.Empty")
    }
}
object OptBool {
  def apply(a: Boolean): OptBool = {
    if (a) True
    else False
  }
  def unapply(a: OptBool): OptBool = a
  def empty: OptBool = Empty
  case object True extends OptBool
  case object False extends OptBool
  case object Empty extends OptBool
}
