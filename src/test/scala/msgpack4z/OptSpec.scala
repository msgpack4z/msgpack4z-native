package msgpack4z

import org.scalacheck.{Prop, Properties}

object OptSpec extends Properties("opt") {

  property("OptBool") = Prop.forAll{ a: Boolean =>
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

  property("OptByte") = Prop.forAll{ a: Byte =>
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

  property("OptShort") = Prop.forAll{ a: Short =>
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

  property("OptInt") = Prop.forAll{ a: Int =>
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