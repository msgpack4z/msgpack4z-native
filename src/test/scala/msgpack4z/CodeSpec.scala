package msgpack4z

import org.scalacheck.{Prop, Properties}

object CodeSpec extends Properties("Code") {

  private[this] val list = List(
    Code.NIL,
    Code.FALSE,
    Code.TRUE,
    Code.BIN8,
    Code.BIN16,
    Code.BIN32,
    Code.EXT8,
    Code.EXT16,
    Code.EXT32,
    Code.FLOAT32,
    Code.FLOAT64,
    Code.UINT8,
    Code.UINT16,
    Code.UINT32,
    Code.UINT64,
    Code.INT8,
    Code.INT16,
    Code.INT32,
    Code.INT64,
    Code.FIXEXT1,
    Code.FIXEXT2,
    Code.FIXEXT4,
    Code.FIXEXT8,
    Code.FIXEXT16,
    Code.STR8,
    Code.STR16,
    Code.STR32,
    Code.ARRAY16,
    Code.ARRAY32,
    Code.MAP16,
    Code.MAP32,
    0xc1.toByte
  )

  private[this] val functions = List[Byte => Boolean](
    Code.isPosFixInt,
    Code.isNegFixInt,
    Code.isFixStr,
    Code.isFixedArray,
    Code.isFixedMap
  ) ::: list.map(c => c == (_: Byte))

  property("no duplicate") = Prop.secure{
    assert(list.distinct.size == list.size)
    (Byte.MinValue to Byte.MaxValue).foreach{ b =>
      val a = functions.map(_ apply b.toByte).partition(identity)._1
      assert(a.size == 1, (a.size, b.toHexString))
    }
    true
  }

}
