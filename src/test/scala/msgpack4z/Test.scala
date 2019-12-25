package msgpack4z

import java.math.BigInteger

import scalaprops._
import scalaprops.Property.{forAll, forAllG}

object Test extends Scalaprops {

  override def param = super.param.copy(
    minSuccessful = 1000,
    maxSize = 300
  )

  val ExtTypeHeader = forAll { (tpe: Byte, size: Int) =>
    val out = MsgOutBuffer.create()
    out.packExtTypeHeader(tpe, size)
    val bytes = out.result()
    val header = MsgInBuffer(bytes).unpackExtTypeHeader()
    (header.getType == tpe) && (header.getLength == size)
  }

  val MapHeader = forAll { size: Int =>
    val out = MsgOutBuffer.create()
    out.packMapHeader(size)
    val bytes = out.result()
    MsgInBuffer(bytes).unpackMapHeader() == size
  }

  val ArrayHeader = forAll { size: Int =>
    val out = MsgOutBuffer.create()
    out.packArrayHeader(size)
    val bytes = out.result()
    MsgInBuffer(bytes).unpackArrayHeader() == size
  }

  val byte = forAll { a: Byte =>
    val out = MsgOutBuffer.create()
    out.packByte(a)
    val bytes = out.result()
    assert(MsgInBuffer(bytes).unpackByte == a)
    assert(MsgInBuffer(bytes).unpackShort == a)
    assert(MsgInBuffer(bytes).unpackInt == a)
    assert(MsgInBuffer(bytes).unpackLong == a)
    MsgInBuffer(bytes).unpackBigInteger == BigInteger.valueOf(a)
  }

  val short = forAll { a: Short =>
    val out = MsgOutBuffer.create()
    out.packShort(a)
    val bytes = out.result()
    assert(MsgInBuffer(bytes).unpackShort == a)
    assert(MsgInBuffer(bytes).unpackInt == a)
    assert(MsgInBuffer(bytes).unpackLong == a)
    MsgInBuffer(bytes).unpackBigInteger == BigInteger.valueOf(a)
  }

  val int = forAll { a: Int =>
    val out = MsgOutBuffer.create()
    out.packInt(a)
    val bytes = out.result()
    assert(MsgInBuffer(bytes).unpackInt == a)
    assert(MsgInBuffer(bytes).unpackLong == a)
    MsgInBuffer(bytes).unpackBigInteger == BigInteger.valueOf(a)
  }

  val long = forAll { a: Long =>
    val out = MsgOutBuffer.create()
    out.packLong(a)
    val bytes = out.result()
    assert(MsgInBuffer(bytes).unpackLong == a)
    MsgInBuffer(bytes).unpackBigInteger == BigInteger.valueOf(a)
  }

  val double = forAll { a: Double =>
    val out = MsgOutBuffer.create()
    out.packDouble(a)
    val bytes = out.result()
    MsgInBuffer(bytes).unpackDouble match {
      case f if f.isNaN => a.isNaN
      case f => f == a
    }
  }

  val float = forAll { a: Float =>
    val out = MsgOutBuffer.create()
    out.packFloat(a)
    val bytes = out.result()
    MsgInBuffer(bytes).unpackFloat match {
      case f if f.isNaN => assert(a.isNaN)
      case f => f == a
    }
    MsgInBuffer(bytes).unpackDouble match {
      case f if f.isNaN => a.isNaN
      case f => f == a
    }
  }

  // TODO https://github.com/scalaprops/scalaprops/issues/26
  private[this] val unicodeStringGen: Gen[String] = {
    def isSurrogate(c: Char): Boolean =
      (Character.MIN_SURROGATE <= c) && (c <= Character.MAX_SURROGATE)

    Gen.arrayOfN(300, Gen.genCharAll).map { s =>
      val b = new java.lang.StringBuilder
      var i = 0
      while (i < s.length) {
        val c = s(i)
        if (!isSurrogate(c)) {
          b.append(c)
          i += 1
        } else if ((i + 1 < s.length) && Character.isSurrogatePair(c, s(i + 1))) {
          b.append(c)
          b.append(s(i + 1))
          i += 2
        } else {
          i += 1
        }
      }
      b.toString
    }
  }

  val string = forAllG(unicodeStringGen) { a =>
    val out = MsgOutBuffer.create()
    out.packString(a)
    val bytes = out.result()
    MsgInBuffer(bytes).unpackString() == a
  }.toProperties((), Param.minSuccessful(300))

  val binary = forAll { a: Array[Byte] =>
    val out = MsgOutBuffer.create()
    out.packBinary(a)
    val bytes = out.result()
    java.util.Arrays.equals(MsgInBuffer(bytes).unpackBinary(), a)
  }.toProperties((), Param.minSuccessful(300))

  val nil = forAll {
    val out = MsgOutBuffer.create()
    out.packNil()
    val bytes = out.result()
    MsgInBuffer(bytes).unpackNilWithCheck()
  }

  val `true` = forAll {
    val out = MsgOutBuffer.create()
    out.packBoolean(true)
    val bytes = out.result()
    MsgInBuffer(bytes).unpackBoolean()
  }

  val `false` = forAll {
    val out = MsgOutBuffer.create()
    out.packBoolean(false)
    val bytes = out.result()
    MsgInBuffer(bytes).unpackBoolean() == false
  }
}
