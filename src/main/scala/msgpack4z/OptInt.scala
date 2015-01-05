package msgpack4z

final class OptInt private(private val value: Long) extends AnyVal {
  def get: Int = {
    if(isEmpty) sys.error("OptInt.Empty")
    else value.asInstanceOf[Int]
  }

  def isEmpty: Boolean = (value < Int.MinValue) || (Int.MaxValue < value)
  def nonEmpty: Boolean = !isEmpty
}

object OptInt {
  @inline def apply(value: Int): OptInt = new OptInt(value)
  val empty: OptInt = new OptInt(Long.MinValue)

  def unapply(x: OptInt): OptInt = x
}
