addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.6.0")
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.1")
addSbtPlugin("com.github.sbt" % "sbt-release" % "1.4.0")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.11")
addSbtPlugin("com.github.scalaprops" % "sbt-scalaprops" % "0.5.3")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-language:existentials",
  "-language:implicitConversions",
)

fullResolvers ~= { _.filterNot(_.name == "jcenter") }
