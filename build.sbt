import build._

lazy val msgpack4zNativeJVM = msgpack4zNative.jvm

lazy val msgpack4zNativeJS = msgpack4zNative.js

lazy val root = Project(
  "root", file(".")
).settings(
  commonSettings,
  scalaSource in Compile := file("dummy"),
  scalaSource in Test := file("dummy"),
  PgpKeys.publishSigned := {},
  PgpKeys.publishLocalSigned := {},
  publishLocal := {},
  publishArtifact in Compile := false,
  publish := {}
).aggregate(
  msgpack4zNativeJVM, msgpack4zNativeJS
)
