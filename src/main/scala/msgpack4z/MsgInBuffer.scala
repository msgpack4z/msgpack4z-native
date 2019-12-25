package msgpack4z

import java.io.{ByteArrayInputStream, EOFException}
import java.math.BigInteger

object MsgInBuffer {
  def apply(bytes: Array[Byte]): MsgInBuffer =
    new MsgInBuffer(new ByteArrayInputStream(bytes))
}

final class MsgInBuffer(buf: ByteArrayInputStream) extends MsgUnpacker {

  def skipBytes(n: Int): Unit = {
    buf.skip(n)
  }

  private[this] def readByte(): Byte = {
    val b = buf.read()
    if (b < 0) {
      throw new EOFException()
    } else {
      b.asInstanceOf[Byte]
    }
  }

  private[this] def readShort(): Short = {
    val ch1, ch2 = buf.read()
    if ((ch1 | ch2) < 0) {
      throw new EOFException()
    } else {
      ((ch1 << 8) + ch2).asInstanceOf[Short]
    }
  }

  private[this] def readInt(): Int = {
    val ch1, ch2, ch3, ch4 = buf.read()
    if ((ch1 | ch2 | ch3 | ch4) < 0) {
      throw new EOFException()
    } else {
      (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4
    }
  }

  private[this] def readLong(): Long = {
    val ch1, ch2, ch3, ch4, ch5, ch6, ch7, ch8 = buf.read().asInstanceOf[Long]
    if ((ch1 | ch2 | ch3 | ch4 | ch5 | ch6 | ch7 | ch8) < 0) {
      throw new EOFException()
    } else {
      (ch1 << 56) + (ch2 << 48) + (ch3 << 40) + (ch4 << 32) + (ch5 << 24) + (ch6 << 16) + (ch7 << 8) + ch8
    }
  }

  private[this] def readFloat(): Float =
    java.lang.Float.intBitsToFloat(readInt())

  private[this] def readDouble(): Double =
    java.lang.Double.longBitsToDouble(readLong())

  /**
   * @return true if success
   */
  def unpackNilWithCheck(): Boolean = {
    readByte() == Code.NIL
  }

  def unpackBooleanOpt(): OptBool = {
    readByte() match {
      case Code.TRUE =>
        OptBool.True
      case Code.FALSE =>
        OptBool.False
      case _ =>
        OptBool.Empty
    }
  }

  def unpackByteOpt(): OptByte = {
    val b = readByte()
    if (Code.isFixInt(b)) {
      OptByte(b)
    } else {
      b match {
        case Code.UINT8 =>
          val u8 = readByte()
          if (u8 < 0) {
            OptByte.empty
          } else {
            OptByte(u8)
          }
        case Code.UINT16 =>
          val u16 = readShort()
          if (u16 < 0 || u16 > Byte.MaxValue) {
            OptByte.empty
          } else {
            OptByte(u16.asInstanceOf[Byte])
          }
        case Code.UINT32 =>
          val u32 = readInt()
          if (u32 < 0 || u32 > Byte.MaxValue) {
            OptByte.empty
          } else {
            OptByte(u32.asInstanceOf[Byte])
          }
        case Code.UINT64 =>
          val u64 = readLong()
          if (u64 < 0L || u64 > Byte.MaxValue) {
            OptByte.empty
          } else {
            OptByte(u64.asInstanceOf[Byte])
          }
        case Code.INT8 =>
          OptByte(readByte())
        case Code.INT16 =>
          val i16 = readShort()
          if (i16 < Byte.MinValue || i16 > Byte.MaxValue) {
            OptByte.empty
          } else {
            OptByte(i16.asInstanceOf[Byte])
          }
        case Code.INT32 =>
          val i32 = readInt()
          if (i32 < Byte.MinValue || i32 > Byte.MaxValue) {
            OptByte.empty
          } else {
            OptByte(i32.asInstanceOf[Byte])
          }
        case Code.INT64 =>
          val i64 = readLong()
          if (i64 < Byte.MinValue || i64 > Byte.MaxValue) {
            OptByte.empty
          } else {
            OptByte(i64.asInstanceOf[Byte])
          }
        case _ =>
          OptByte.empty
      }
    }
  }

  def unpackShortOpt(): OptShort = {
    val b = readByte()
    if (Code.isFixInt(b)) {
      OptShort(b)
    } else {
      b match {
        case Code.UINT8 =>
          val u8 = readByte()
          OptShort((u8 & 0xff).asInstanceOf[Short])
        case Code.UINT16 =>
          val u16 = readShort()
          if (u16 < 0) {
            OptShort.empty
          } else {
            OptShort(u16)
          }
        case Code.UINT32 =>
          val u32 = readInt()
          if (u32 < 0 || u32 > Short.MaxValue) {
            OptShort.empty
          } else {
            OptShort(u32.asInstanceOf[Short])
          }
        case Code.UINT64 =>
          val u64 = readLong()
          if (u64 < 0L || u64 > Short.MaxValue) {
            OptShort.empty
          } else {
            OptShort(u64.asInstanceOf[Short])
          }
        case Code.INT8 =>
          val i8 = readByte()
          OptShort(i8.asInstanceOf[Short])
        case Code.INT16 =>
          OptShort(readShort())
        case Code.INT32 =>
          val i32 = readInt()
          if (i32 < Short.MinValue || i32 > Short.MaxValue) {
            OptShort.empty
          } else {
            OptShort(i32.asInstanceOf[Short])
          }
        case Code.INT64 =>
          val i64 = readLong()
          if (i64 < Short.MinValue || i64 > Short.MaxValue) {
            OptShort.empty
          } else {
            OptShort(i64.asInstanceOf[Short])
          }
        case _ =>
          OptShort.empty
      }
    }
  }

  def unpackIntOpt(): OptInt = {
    val b = readByte()
    if (Code.isFixInt(b)) {
      OptInt(b)
    } else {
      b match {
        case Code.UINT8 =>
          val u8 = readByte()
          OptInt(u8 & 0xff)
        case Code.UINT16 =>
          val u16 = readShort()
          OptInt(u16 & 0xffff)
        case Code.UINT32 =>
          val u32 = readInt()
          if (u32 < 0) {
            OptInt.empty
          } else {
            OptInt(u32)
          }
        case Code.UINT64 =>
          val u64 = readLong()
          if (u64 < 0L || u64 > Int.MaxValue) {
            OptInt.empty
          } else {
            OptInt(u64.asInstanceOf[Int])
          }
        case Code.INT8 =>
          OptInt(readByte())
        case Code.INT16 =>
          OptInt(readShort())
        case Code.INT32 =>
          OptInt(readInt())
        case Code.INT64 =>
          val i64 = readLong()
          if (i64 < Int.MinValue || i64 > Int.MaxValue) {
            OptInt.empty
          } else {
            OptInt(i64.asInstanceOf[Int])
          }
        case _ =>
          OptInt.empty
      }
    }
  }

  override def unpackLong(): Long = {
    val b = readByte()
    if (Code.isFixInt(b)) {
      b
    } else {
      b match {
        case Code.UINT8 =>
          val u8 = readByte()
          u8 & 0xff
        case Code.UINT16 =>
          val u16 = readShort()
          u16 & 0xffff
        case Code.UINT32 =>
          val u32 = readInt()
          if (u32 < 0) {
            (u32 & 0x7fffffff) + 0x80000000L
          } else {
            u32
          }
        case Code.UINT64 =>
          val u64 = readLong()
          if (u64 < 0L) {
            throw new RuntimeException("overflow " + u64)
          }
          u64
        case Code.INT8 =>
          readByte()
        case Code.INT16 =>
          readShort()
        case Code.INT32 =>
          readInt()
        case Code.INT64 =>
          readLong()
      }
    }
  }

  override def unpackBigInteger(): BigInteger = {
    val b = readByte()
    if (Code.isFixInt(b)) {
      BigInteger.valueOf(b)
    } else {
      b match {
        case Code.UINT8 =>
          val u8 = readByte()
          BigInteger.valueOf(u8 & 0xff)
        case Code.UINT16 =>
          val u16 = readShort()
          BigInteger.valueOf(u16 & 0xffff)
        case Code.UINT32 =>
          val u32 = readInt()
          if (u32 < 0) {
            BigInteger.valueOf((u32 & 0x7fffffff) + 0x80000000L)
          } else {
            BigInteger.valueOf(u32)
          }
        case Code.UINT64 =>
          val u64 = readLong()
          if (u64 < 0L) {
            BigInteger.valueOf(u64 + Long.MaxValue + 1L).setBit(63)
          } else {
            BigInteger.valueOf(u64)
          }
        case Code.INT8 =>
          val i8 = readByte()
          BigInteger.valueOf(i8)
        case Code.INT16 =>
          val i16 = readShort()
          BigInteger.valueOf(i16)
        case Code.INT32 =>
          val i32 = readInt()
          BigInteger.valueOf(i32)
        case Code.INT64 =>
          BigInteger.valueOf(readLong())
      }
    }
  }

  override def unpackFloat(): Float = {
    val b = readByte()
    b match {
      case Code.FLOAT32 =>
        readFloat()
      case Code.FLOAT64 =>
        readDouble().asInstanceOf[Float]
    }
  }

  override def unpackDouble(): Double = {
    val b = readByte()
    b match {
      case Code.FLOAT32 =>
        readFloat()
      case Code.FLOAT64 =>
        readDouble()
    }
  }

  def unpackArrayHeaderOpt(): OptInt = {
    val b = readByte()
    if (Code.isFixedArray(b)) {
      OptInt(b & 0x0f)
    } else {
      b match {
        case Code.ARRAY16 =>
          OptInt(readNextLength16())
        case Code.ARRAY32 =>
          OptInt(readInt())
        case _ =>
          OptInt.empty
      }
    }
  }

  def unpackMapHeaderOpt(): OptInt = {
    val b = readByte()
    if (Code.isFixedMap(b)) {
      OptInt(b & 0x0f)
    } else {
      b match {
        case Code.MAP16 =>
          OptInt(readNextLength16())
        case Code.MAP32 =>
          OptInt(readInt())
        case _ =>
          OptInt.empty
      }
    }
  }

  private def readStringHeader(b: Byte): OptInt = {
    b match {
      case Code.STR8 =>
        OptInt(readNextLength8())
      case Code.STR16 =>
        OptInt(readNextLength16())
      case Code.STR32 =>
        readNextLength32()
      case _ =>
        OptInt.empty
    }
  }

  def unpackRawStringHeader: OptInt = {
    val b = readByte()
    if (Code.isFixedRaw(b)) {
      OptInt(b & 0x1f)
    } else {
      readStringHeader(b)
    }
  }

  private[this] def readNextLength8(): Int =
    readByte() & 0xff

  private[this] def readNextLength16(): Int =
    readShort() & 0xffff

  private[this] def readNextLength32(): OptInt = {
    val u32 = readInt()
    if (u32 < 0) {
      OptInt.empty
    } else {
      OptInt(u32)
    }
  }

  override def close(): Unit = {
    buf.close()
  }

  override def nextType(): MsgType = {
    if (buf.markSupported()) {
      buf.mark(1024)
      try {
        Code.getType(readByte())
      } finally {
        buf.reset()
      }
    } else {
      sys.error("mark not supported")
    }
  }

  override def unpackBinary(): Array[Byte] = {
    val b = readByte()
    val len: Int =
      if (Code.isFixedRaw(b)) {
        0x1f & b
      } else {
        b match {
          case Code.BIN8 =>
            readNextLength8()
          case Code.BIN16 =>
            readNextLength16()
          case Code.BIN32 =>
            readNextLength32() match {
              case OptInt(l) =>
                l
              case _ =>
                sys.error("binary header length overflow")
            }
          case _ =>
            sys.error("not binary header " + b)
        }
      }
    val array = new Array[Byte](len)
    buf.read(array)
    array
  }

  override def mapEnd(): Unit = {
    // do nothing
  }

  override def unpackBoolean(): Boolean = {
    unpackBooleanOpt().get
  }

  override def unpackNil(): Unit = {
    assert(unpackNilWithCheck())
  }

  override def unpackString(): String = {
    val b = readByte()
    val len: Int =
      if (Code.isFixStr(b)) {
        0x1f & b
      } else {
        b match {
          case Code.STR8 =>
            readNextLength8()
          case Code.STR16 =>
            readNextLength16()
          case Code.STR32 =>
            readNextLength32().get
          case _ =>
            sys.error("not string header " + b)
        }
      }
    val array = new Array[Byte](len)
    buf.read(array)
    new String(array, "UTF-8")
  }

  override def arrayEnd(): Unit = {
    // do nothing
  }

  override def unpackByte(): Byte = {
    unpackByteOpt().get
  }

  override def unpackArrayHeader(): Int = {
    unpackArrayHeaderOpt().get
  }

  override def unpackInt(): Int = {
    unpackIntOpt().get
  }

  override def unpackShort(): Short = {
    unpackShortOpt().get
  }

  override def unpackMapHeader(): Int = {
    unpackMapHeaderOpt().get
  }

  override def readPayload(length: Int): Array[Byte] = {
    val array = new Array[Byte](length)
    buf.read(array)
    array
  }

  override def readPayload(a: Array[Byte]): Unit = {
    buf.read(a)
  }

  override def unpackExtTypeHeader(): ExtTypeHeader = {
    readByte() match {
      case Code.FIXEXT1 =>
        new ExtTypeHeader(readByte(), 1)
      case Code.FIXEXT2 =>
        new ExtTypeHeader(readByte(), 2)
      case Code.FIXEXT4 =>
        new ExtTypeHeader(readByte(), 4)
      case Code.FIXEXT8 =>
        new ExtTypeHeader(readByte(), 8)
      case Code.FIXEXT16 =>
        new ExtTypeHeader(readByte(), 16)
      case Code.EXT8 =>
        val length = readByte() & 0xff
        val tpe = readByte()
        new ExtTypeHeader(tpe, length)
      case Code.EXT16 =>
        val length = readShort() & 0xffff
        val tpe = readByte()
        new ExtTypeHeader(tpe, length)
      case Code.EXT32 =>
        val length = readInt()
        val tpe = readByte()
        new ExtTypeHeader(tpe, length)
      case other =>
        sys.error("unexpected type " + other)
    }
  }

}
