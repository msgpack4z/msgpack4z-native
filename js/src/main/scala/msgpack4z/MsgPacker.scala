package msgpack4z

import java.math.BigInteger

/**
  * @see [[https://github.com/msgpack4z/msgpack4z-api/blob/v0.2.0/src/main/java/msgpack4z/MsgPacker.java]]
  */
trait MsgPacker {
  def packByte(a: Byte): Unit
  def packShort(a: Short): Unit
  def packInt(a: Int): Unit
  def packLong(a: Long): Unit
  def packDouble(a: Double): Unit
  def packFloat(a: Float): Unit
  def packBigInteger(a: BigInteger): Unit
  def packArrayHeader(a: Int): Unit
  def arrayEnd(): Unit
  def packMapHeader(a: Int): Unit
  def mapEnd(): Unit
  def packBoolean(a: Boolean): Unit
  def packNil(): Unit
  def packString(a: String): Unit
  def packBinary(a: Array[Byte]): Unit
  def packExtTypeHeader(extType: Byte, payloadLen: Int): Unit
  def writePayload(a: Array[Byte]): Unit
  def result(): Array[Byte]
}
