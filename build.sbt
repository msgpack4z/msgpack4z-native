import build._
import sbtrelease._
import ReleaseStateTransformations._
import com.typesafe.sbt.pgp.PgpKeys
import sbtcrossproject.crossProject

val tagName = Def.setting{
  s"v${if (releaseUseGlobalVersion.value) (version in ThisBuild).value else version.value}"
}

val tagOrHash = Def.setting{
  if(isSnapshot.value) gitHash() else tagName.value
}

def gitHash(): String = sys.process.Process("git rev-parse HEAD").lines_!.head

val unusedWarnings = (
  "-Ywarn-unused" ::
  Nil
)

val Scala211 = "2.11.12"

val SetScala211 = releaseStepCommand("++" + Scala211)

lazy val commonSettings = Def.settings(
  ReleasePlugin.extraReleaseCommands,
  name := msgpack4zNativeName,
  crossScalaVersions := Scala211 :: "2.12.8" :: "2.13.0-RC1" :: Nil,
  commands += Command.command("updateReadme")(UpdateReadme.updateReadmeTask),
  resolvers += Opts.resolver.sonatypeReleases,
  publishTo := Some(
    if (isSnapshot.value)
      Opts.resolver.sonatypeSnapshots
    else
      Opts.resolver.sonatypeStaging
  ),
  fullResolvers ~= {_.filterNot(_.name == "jcenter")},
  javacOptions in compile ++= Seq("-target", "6", "-source", "6"),
  releaseTagName := tagName.value,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    UpdateReadme.updateReadmeProcess,
    tagRelease,
    ReleaseStep(
      action = { state =>
        val extracted = Project extract state
        extracted.runAggregated(PgpKeys.publishSigned in Global in extracted.get(thisProjectRef), state)
      },
      enableCrossBuild = true
    ),
    SetScala211,
    releaseStepCommand("msgpack4zNativeNative/publishSigned"),
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    UpdateReadme.updateReadmeProcess,
    pushChanges
  ),
  credentials ++= PartialFunction.condOpt(sys.env.get("SONATYPE_USER") -> sys.env.get("SONATYPE_PASS")){
    case (Some(user), Some(pass)) =>
      Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, pass)
  }.toList,
  organization := "com.github.xuwei-k",
  homepage := Some(url("https://github.com/msgpack4z")),
  licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php")),
  scalacOptions ++= (
    "-deprecation" ::
    "-unchecked" ::
    "-Xlint" ::
    "-Xfuture" ::
    "-language:existentials" ::
    "-language:higherKinds" ::
    "-language:implicitConversions" ::
    Nil
  ) ::: unusedWarnings,
  scalacOptions ++= PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
    case Some((2, v)) if v <= 12 => "-Yno-adapted-args"
  }.toList,
  scalacOptions in (Compile, doc) ++= {
    val tag = tagOrHash.value
    Seq(
      "-sourcepath", (baseDirectory in LocalRootProject).value.getAbsolutePath,
      "-doc-source-url", s"https://github.com/msgpack4z/msgpack4z-native/tree/${tag}€{FILE_PATH}.scala"
    )
  },
  scalaVersion := Scala211,
  pomExtra :=
    <developers>
      <developer>
        <id>xuwei-k</id>
        <name>Kenji Yoshida</name>
        <url>https://github.com/xuwei-k</url>
      </developer>
    </developers>
    <scm>
      <url>git@github.com:msgpack4z/msgpack4z-native.git</url>
      <connection>scm:git:git@github.com:msgpack4z/msgpack4z-native.git</connection>
      <tag>{tagOrHash.value}</tag>
    </scm>
  ,
  description := "msgpack4z",
  pomPostProcess := { node =>
    import scala.xml._
    import scala.xml.transform._
    def stripIf(f: Node => Boolean) = new RewriteRule {
      override def transform(n: Node) =
        if (f(n)) NodeSeq.Empty else n
    }
    val stripTestScope = stripIf { n => n.label == "dependency" && (n \ "scope").text == "test" }
    new RuleTransformer(stripTestScope).transform(node)(0)
  }
) ++ Seq(Compile, Test).flatMap(c =>
  scalacOptions in (c, console) ~= {_.filterNot(unusedWarnings.toSet)}
)

lazy val msgpack4zNative = crossProject(
  JSPlatform, JVMPlatform, NativePlatform
).crossType(CustomCrossType).in(file(".")).settings(
  commonSettings,
  scalapropsCoreSettings,
  libraryDependencies ++= (
    ("com.github.scalaprops" %%% "scalaprops" % "0.6.0" % "test") ::
    Nil
  )
).jvmSettings(
  Sxr.settings,
  libraryDependencies ++= (
    ("com.github.xuwei-k" % "msgpack4z-api" % "0.2.0") ::
    Nil
  )
).platformsSettings(NativePlatform, JSPlatform)(
  unmanagedSourceDirectories in Compile += {
    baseDirectory.value.getParentFile / "js_native/src/main/scala/"
  }
).jsSettings(
  scalaJSSemantics ~= { _.withStrictFloats(true) },
  scalacOptions += {
    val a = (baseDirectory in LocalRootProject).value.toURI.toString
    val g = "https://raw.githubusercontent.com/msgpack4z/msgpack4z-native/" + tagOrHash.value
    s"-P:scalajs:mapSourceURI:$a->$g/"
  }
)

lazy val msgpack4zNativeJVM = msgpack4zNative.jvm

lazy val msgpack4zNativeJS = msgpack4zNative.js

lazy val msgpack4zNativeNative = msgpack4zNative.native.settings(
  scalapropsNativeSettings
)

lazy val noPublish = Seq(
  PgpKeys.publishSigned := {},
  PgpKeys.publishLocalSigned := {},
  publishLocal := {},
  publishArtifact in Compile := false,
  publish := {}
)

lazy val root = Project(
  "root", file(".")
).settings(
  commonSettings,
  scalaSource in Compile := file("dummy"),
  scalaSource in Test := file("dummy"),
  noPublish
).aggregate(
  msgpack4zNativeJVM, msgpack4zNativeJS
)
