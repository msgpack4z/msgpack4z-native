package msgpack4z

import scala.util.Random
import java.math.BigInteger

object Main {
  def main(args: Array[String]): Unit = {
    tests1.foreach { case (name, test) =>
      println(name)
      assert(test.apply(), name)
    }
  }

  private final val N = 100

  val tests1 = Seq[(String, () => Boolean)](
    "OptShort" -> { () =>
      (1 to N).forall { _ =>
        val a = Random.nextInt.toShort
        val b = OptShort(a)
        assert(b.nonEmpty)
        assert(b.get == a)
        b match {
          case OptShort(c) =>
            assert(c == a)
          case _ =>
            sys.error(s"match error $b")
        }
        OptShort.empty match {
          case OptShort(_) =>
            sys.error("match error")
          case _ =>
            true
        }
      }
    },
    "OptInt" -> { () =>
      (1 to N).forall { _ =>
        val a = Random.nextInt
        val b = OptInt(a)
        assert(b.nonEmpty)
        assert(b.get == a)
        b match {
          case OptInt(c) =>
            assert(c == a)
          case _ =>
            sys.error(s"match error $b")
        }
        OptInt.empty match {
          case OptInt(_) =>
            sys.error("match error")
          case _ =>
            true
        }
      }
    },
    "OptByte" -> { () =>
      (1 to N).forall { _ =>
        val a = Random.nextInt.toByte
        val b = OptByte(a)
        assert(b.nonEmpty)
        assert(b.get == a)
        b match {
          case OptByte(c) =>
            assert(c == a)
          case _ =>
            sys.error(s"match error $b")
        }
        OptByte.empty match {
          case OptByte(_) =>
            sys.error("match error")
          case _ =>
            true
        }
      }
    },
    "ExtTypeHeader" -> { () =>
      (1 to N).forall { _ =>
        val tpe = Random.nextInt.toByte
        val size = Random.nextInt
        val out = MsgOutBuffer.create()
        out.packExtTypeHeader(tpe, size)
        val bytes = out.result()
        val header = MsgInBuffer(bytes).unpackExtTypeHeader()
        (header.getType == tpe) && (header.getLength == size)
      }
    },
    "MapHeader" -> { () =>
      (1 to N).forall { _ =>
        val size = Random.nextInt
        val out = MsgOutBuffer.create()
        out.packMapHeader(size)
        val bytes = out.result()
        MsgInBuffer(bytes).unpackMapHeader() == size
      }
    },
    "ArrayHeader" -> { () =>
      (1 to N).forall { _ =>
        val size = Random.nextInt
        val out = MsgOutBuffer.create()
        out.packArrayHeader(size)
        val bytes = out.result()
        MsgInBuffer(bytes).unpackArrayHeader() == size
      }
    },
    "byte" -> { () =>
      (1 to N).forall { _ =>
        val a = Random.nextInt.toByte
        val out = MsgOutBuffer.create()
        out.packByte(a)
        val bytes = out.result()
        assert(MsgInBuffer(bytes).unpackByte == a)
        assert(MsgInBuffer(bytes).unpackShort == a)
        assert(MsgInBuffer(bytes).unpackInt == a)
        assert(MsgInBuffer(bytes).unpackLong == a)
        MsgInBuffer(bytes).unpackBigInteger == BigInteger.valueOf(a)
      }
    },
    "short" -> { () =>
      (1 to N).forall { _ =>
        val a = Random.nextInt.toShort
        val out = MsgOutBuffer.create()
        out.packShort(a)
        val bytes = out.result()
        assert(MsgInBuffer(bytes).unpackShort == a)
        assert(MsgInBuffer(bytes).unpackInt == a)
        assert(MsgInBuffer(bytes).unpackLong == a)
        MsgInBuffer(bytes).unpackBigInteger == BigInteger.valueOf(a)
      }
    },
    "int" -> { () =>
      (1 to N).forall { _ =>
        val a = Random.nextInt
        val out = MsgOutBuffer.create()
        out.packInt(a)
        val bytes = out.result()
        assert(MsgInBuffer(bytes).unpackInt == a)
        assert(MsgInBuffer(bytes).unpackLong == a)
        MsgInBuffer(bytes).unpackBigInteger == BigInteger.valueOf(a)
      }
    },
    "long" -> { () =>
      (1 to N).forall { _ =>
        val a = Random.nextLong
        val out = MsgOutBuffer.create()
        out.packLong(a)
        val bytes = out.result()
        assert(MsgInBuffer(bytes).unpackLong == a)
        MsgInBuffer(bytes).unpackBigInteger == BigInteger.valueOf(a)
      }
    },
    "double" -> { () =>
      (1 to N).forall { _ =>
        val a = Random.nextDouble
        val out = MsgOutBuffer.create()
        out.packDouble(a)
        val bytes = out.result()
        MsgInBuffer(bytes).unpackDouble == a
      }
    },
    "float" -> { () =>
      (1 to N).forall { _ =>
        val a = Random.nextFloat
        val out = MsgOutBuffer.create()
        out.packFloat(a)
        val bytes = out.result()
        assert(MsgInBuffer(bytes).unpackFloat == a)
        MsgInBuffer(bytes).unpackDouble == a
      }
    },
    "binary" -> { () =>
      val a = Array.fill(10)(Random.nextInt.toByte)
      val out = MsgOutBuffer.create()
      out.packBinary(a)
      val bytes = out.result()
      java.util.Arrays.equals(MsgInBuffer(bytes).unpackBinary(), a)
    },
    "nil" -> { () =>
      val out = MsgOutBuffer.create()
      out.packNil()
      val bytes = out.result()
      MsgInBuffer(bytes).unpackNilWithCheck()
    },
    "true" -> { () =>
      val out = MsgOutBuffer.create()
      out.packBoolean(true)
      val bytes = out.result()
      MsgInBuffer(bytes).unpackBoolean()
    },
    "false" -> { () =>
      val out = MsgOutBuffer.create()
      out.packBoolean(false)
      val bytes = out.result()
      MsgInBuffer(bytes).unpackBoolean() == false
    }
  )
}
