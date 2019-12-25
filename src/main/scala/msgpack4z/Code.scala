package msgpack4z

object Code {
  def isFixInt(b: Byte): Boolean = {
    val v: Int = b & 0xFF
    v <= 0x7f || v >= 0xe0
  }

  def isPosFixInt(b: Byte): Boolean = {
    (b & POSFIXINT_MASK) == 0
  }

  def isNegFixInt(b: Byte): Boolean = {
    (b & NEGFIXINT_PREFIX) == NEGFIXINT_PREFIX
  }

  def isFixStr(b: Byte): Boolean = {
    (b & 0xe0.toByte) == Code.FIXSTR_PREFIX
  }

  def isFixedArray(b: Byte): Boolean = {
    (b & 0xf0.toByte) == Code.FIXARRAY_PREFIX
  }

  def isFixedMap(b: Byte): Boolean = {
    (b & 0xf0.toByte) == Code.FIXMAP_PREFIX
  }

  def isFixedRaw(b: Byte): Boolean = {
    (b & 0xe0.toByte) == Code.FIXSTR_PREFIX
  }

  val POSFIXINT_MASK: Byte = 0x80.toByte
  val FIXMAP_PREFIX: Byte = 0x80.toByte
  val FIXARRAY_PREFIX: Byte = 0x90.toByte
  val FIXSTR_PREFIX: Byte = 0xa0.toByte
  val NIL: Byte = 0xc0.toByte
  val FALSE: Byte = 0xc2.toByte
  val TRUE: Byte = 0xc3.toByte
  val BIN8: Byte = 0xc4.toByte
  val BIN16: Byte = 0xc5.toByte
  val BIN32: Byte = 0xc6.toByte
  val EXT8: Byte = 0xc7.toByte
  val EXT16: Byte = 0xc8.toByte
  val EXT32: Byte = 0xc9.toByte
  val FLOAT32: Byte = 0xca.toByte
  val FLOAT64: Byte = 0xcb.toByte
  val UINT8: Byte = 0xcc.toByte
  val UINT16: Byte = 0xcd.toByte
  val UINT32: Byte = 0xce.toByte
  val UINT64: Byte = 0xcf.toByte
  val INT8: Byte = 0xd0.toByte
  val INT16: Byte = 0xd1.toByte
  val INT32: Byte = 0xd2.toByte
  val INT64: Byte = 0xd3.toByte
  val FIXEXT1: Byte = 0xd4.toByte
  val FIXEXT2: Byte = 0xd5.toByte
  val FIXEXT4: Byte = 0xd6.toByte
  val FIXEXT8: Byte = 0xd7.toByte
  val FIXEXT16: Byte = 0xd8.toByte
  val STR8: Byte = 0xd9.toByte
  val STR16: Byte = 0xda.toByte
  val STR32: Byte = 0xdb.toByte
  val ARRAY16: Byte = 0xdc.toByte
  val ARRAY32: Byte = 0xdd.toByte
  val MAP16: Byte = 0xde.toByte
  val MAP32: Byte = 0xdf.toByte
  val NEGFIXINT_PREFIX: Byte = 0xe0.toByte

  def getType(b: Byte): MsgType = {
    val t = formatTable(b & 0xff)
    if (t == null) {
      throw new RuntimeException("invalid byte " + b)
    } else {
      t
    }
  }

  private[this] val formatTable: Array[MsgType] = {
    val array = new Array[MsgType](0x100)
    var b = 0
    while (b <= 0xFF) {
      array(b) = getType0(b.toByte)
      b += 1
    }
    array
  }

  private[this] def getType0(b: Byte): MsgType = {
    if (isPosFixInt(b) || isNegFixInt(b)) {
      MsgType.INTEGER
    } else if (Code.isFixStr(b)) {
      MsgType.STRING
    } else if (Code.isFixedArray(b)) {
      MsgType.ARRAY
    } else if (Code.isFixedMap(b)) {
      MsgType.MAP
    } else {
      b match {
        case Code.NIL =>
          MsgType.NIL
        case Code.FALSE | Code.TRUE =>
          MsgType.BOOLEAN
        case Code.BIN8 | Code.BIN16 | Code.BIN32 =>
          MsgType.BINARY
        case Code.FLOAT32 | Code.FLOAT64 =>
          MsgType.FLOAT
        case Code.UINT8 | Code.UINT16 | Code.UINT32 | Code.UINT64 | Code.INT8 | Code.INT16 | Code.INT32 | Code.INT64 =>
          MsgType.INTEGER
        case Code.STR8 | Code.STR16 | Code.STR32 =>
          MsgType.STRING
        case Code.ARRAY16 | Code.ARRAY32 =>
          MsgType.ARRAY
        case Code.MAP16 | Code.MAP32 =>
          MsgType.MAP
        case Code.FIXEXT1 | Code.FIXEXT2 | Code.FIXEXT4 | Code.FIXEXT8 | Code.FIXEXT16 | Code.EXT8 | Code.EXT16 | Code.EXT32 =>
          MsgType.EXTENSION
        case _ =>
          null
      }
    }
  }

}
