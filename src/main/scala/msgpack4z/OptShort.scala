package msgpack4z

sealed abstract class OptShort {
  def get: Short
  def isEmpty: Boolean
  final def nonEmpty: Boolean = !isEmpty
}

object OptShort {
  private[this] final case class Just(get: Short) extends OptShort {
    def isEmpty = false
  }
  private[this] case object Empty extends OptShort {
    def get = sys.error("OptShort.Empty")
    def isEmpty = true
  }

  def unapply(a: OptShort): OptShort = a

  private[this] val values: Array[OptShort] = {
    (Short.MinValue to Short.MaxValue).map(b => Just(b.asInstanceOf[Short])).toArray
  }

  def apply(b: Short): OptShort = values(b + (1 << 15))
  def empty: OptShort = Empty
}
