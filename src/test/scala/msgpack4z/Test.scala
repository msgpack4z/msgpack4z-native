package msgpack4z

import java.math.BigInteger

import org.scalacheck.{Prop, Properties}

object Test extends Properties("test") {

  property("MapHeader") = Prop.forAll { size: Int =>
    val out = MsgOutBuffer.create()
    out.packMapHeader(size)
    val bytes = out.result
    MsgInBuffer(bytes).unpackMapHeader() == size
  }

  property("ArrayHeader") = Prop.forAll { size: Int =>
    val out = MsgOutBuffer.create()
    out.packArrayHeader(size)
    val bytes = out.result
    MsgInBuffer(bytes).unpackArrayHeader() == size
  }

  property("byte") = Prop.forAll { a: Byte =>
    val out = MsgOutBuffer.create()
    out.packByte(a)
    val bytes = out.result
    MsgInBuffer(bytes).unpackByte == a
    MsgInBuffer(bytes).unpackShort == a
    MsgInBuffer(bytes).unpackInt == a
    MsgInBuffer(bytes).unpackLong == a
    MsgInBuffer(bytes).unpackBigInteger == new BigInteger(a.toString)
  }

  property("short") = Prop.forAll { a: Short =>
    val out = MsgOutBuffer.create()
    out.packShort(a)
    val bytes = out.result
    MsgInBuffer(bytes).unpackShort == a
    MsgInBuffer(bytes).unpackInt == a
    MsgInBuffer(bytes).unpackLong == a
    MsgInBuffer(bytes).unpackBigInteger == new BigInteger(a.toString)
  }

  property("int") = Prop.forAll { a: Int =>
    val out = MsgOutBuffer.create()
    out.packInt(a)
    val bytes = out.result
    MsgInBuffer(bytes).unpackInt == a
    MsgInBuffer(bytes).unpackLong == a
    MsgInBuffer(bytes).unpackBigInteger == new BigInteger(a.toString)
  }

  property("long") = Prop.forAll { a: Long =>
    val out = MsgOutBuffer.create()
    out.packLong(a)
    val bytes = out.result
    MsgInBuffer(bytes).unpackLong == a
    MsgInBuffer(bytes).unpackBigInteger == new BigInteger(a.toString)
  }

  property("double") = Prop.forAll { a: Double =>
    val out = MsgOutBuffer.create()
    out.packDouble(a)
    val bytes = out.result
    MsgInBuffer(bytes).unpackDouble == a
  }

  property("float") = Prop.forAll { a: Float =>
    val out = MsgOutBuffer.create()
    out.packFloat(a)
    val bytes = out.result
    MsgInBuffer(bytes).unpackFloat == a
    MsgInBuffer(bytes).unpackDouble == a
  }

  property("string") = Prop.forAll { a: String =>
    val out = MsgOutBuffer.create()
    out.packString(a)
    val bytes = out.result
    MsgInBuffer(bytes).unpackString() == a
  }

  property("binary") = Prop.forAll { a: Array[Byte] =>
    val out = MsgOutBuffer.create()
    out.packBinary(a)
    val bytes = out.result
    java.util.Arrays.equals(MsgInBuffer(bytes).unpackBinary(), a)
  }

  property("nil") = Prop {
    val out = MsgOutBuffer.create()
    out.packNil()
    val bytes = out.result
    MsgInBuffer(bytes).unpackNilWithCheck()
  }

  property("true") = Prop {
    val out = MsgOutBuffer.create()
    out.packBoolean(true)
    val bytes = out.result
    MsgInBuffer(bytes).unpackBoolean()
  }

  property("false") = Prop {
    val out = MsgOutBuffer.create()
    out.packBoolean(false)
    val bytes = out.result
    MsgInBuffer(bytes).unpackBoolean() == false
  }
}
