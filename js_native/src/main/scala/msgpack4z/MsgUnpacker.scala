package msgpack4z

import java.math.BigInteger

/**
 * @see [[https://github.com/msgpack4z/msgpack4z-api/blob/v0.2.0/src/main/java/msgpack4z/MsgUnpacker.java]]
 */
trait MsgUnpacker {
  def nextType(): MsgType
  def unpackByte(): Byte
  def unpackShort(): Short
  def unpackInt(): Int
  def unpackLong(): Long
  def unpackBigInteger(): BigInteger
  def unpackDouble(): Double
  def unpackFloat(): Float
  def unpackArrayHeader(): Int
  def arrayEnd(): Unit
  def mapEnd(): Unit
  def unpackMapHeader(): Int
  def unpackBoolean(): Boolean
  def unpackNil(): Unit
  def unpackString(): String
  def unpackBinary(): Array[Byte]
  def unpackExtTypeHeader(): ExtTypeHeader
  def readPayload(a: Array[Byte]): Unit
  def readPayload(length: Int): Array[Byte]
  def close(): Unit
}
