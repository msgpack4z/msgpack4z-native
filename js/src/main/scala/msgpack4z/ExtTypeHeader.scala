package msgpack4z

/**
  * @see [[https://github.com/msgpack/msgpack/blob/master/spec.md#ext-format-family ext format family]]
  * @see [[https://github.com/msgpack4z/msgpack4z-api/blob/v0.2.0/src/main/java/msgpack4z/ExtTypeHeader.java]]
  */
final case class ExtTypeHeader(getType: Byte, getLength: Int)
