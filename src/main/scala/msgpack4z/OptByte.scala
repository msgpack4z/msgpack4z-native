package msgpack4z

sealed abstract class OptByte{
  def get: Byte
  def isEmpty: Boolean
  final def nonEmpty: Boolean = !isEmpty
}

object OptByte{
  private[this] final case class Just(get: Byte) extends OptByte{
    def isEmpty = false
  }
  private[this] case object Empty extends OptByte {
    def get = sys.error("OptByte.Empty")
    def isEmpty = true
  }

  def unapply(a: OptByte): OptByte = a

  private[this] val values: Array[OptByte] = {
    (Byte.MinValue to Byte.MaxValue).map(b => Just(b.asInstanceOf[Byte])).toArray
  }

  def apply(b: Byte): OptByte = values(b + 128)
  def empty: OptByte = Empty
}
