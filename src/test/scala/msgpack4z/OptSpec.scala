package msgpack4z

import scalaprops._
import scalaprops.Property.forAll

object OptSpec extends Scalaprops {

  override def param = super.param.copy(
    minSuccessful = 1000,
    maxSize = 300
  )

  val optBool = forAll { a: Boolean =>
    val b = OptBool(a)
    assert(b.nonEmpty)
    assert(b.get == a)
    b match {
      case OptBool(c) =>
        assert(c == a)
      case _ =>
        sys.error(s"match error $b")
    }
    assert(OptBool.empty.isEmpty)
    assert(OptBool.empty.nonEmpty == false)
    OptBool.empty match {
      case OptBool(_) =>
        sys.error("match error")
      case _ =>
        true
    }
  }

  val optByte = forAll { a: Byte =>
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

  val optShort = forAll { a: Short =>
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

  val optInt = forAll { a: Int =>
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

}
