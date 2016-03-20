package msgpack4z

/**
  * @see [[https://github.com/msgpack4z/msgpack4z-api/blob/v0.2.0/src/main/java/msgpack4z/MsgType.java]]
  */
sealed abstract class MsgType extends Product with Serializable

object MsgType {
  /**
    * [[https://github.com/msgpack/msgpack/blob/master/spec.md#formats-nil nil type]]
    */
  case object NIL extends MsgType
  /**
    * [[https://github.com/msgpack/msgpack/blob/master/spec.md#bool-format-family boolean type]]
    */
  case object BOOLEAN extends MsgType
  /**
    * [[https://github.com/msgpack/msgpack/blob/master/spec.md#int-format-family integer type]]
    */
  case object INTEGER extends MsgType
  /**
    * [[https://github.com/msgpack/msgpack/blob/master/spec.md#float-format-family float type]]
    */
  case object FLOAT extends MsgType
  /**
    * [[https://github.com/msgpack/msgpack/blob/master/spec.md#str-format-family string type]]
    */
  case object STRING extends MsgType
  /**
    * [[https://github.com/msgpack/msgpack/blob/master/spec.md#array-format-family array type]]
    */
  case object ARRAY extends MsgType
  /**
    * [[https://github.com/msgpack/msgpack/blob/master/spec.md#map-format-family map type]]
    */
  case object MAP extends MsgType
  /**
    * [[https://github.com/msgpack/msgpack/blob/master/spec.md#bin-format-family binary type]]
    */
  case object BINARY extends MsgType
  /**
    * [[https://github.com/msgpack/msgpack/blob/master/spec.md#ext-format-family extension type]]
    */
  case object EXTENSION extends MsgType
}
