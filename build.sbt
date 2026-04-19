import build._
import sbtrelease._
import ReleaseStateTransformations._

Global / onChangedBuildSource := ReloadOnSourceChanges

val tagName = Def.setting {
  s"v${if (releaseUseGlobalVersion.value) (ThisBuild / version).value else version.value}"
}

val tagOrHash = Def.setting {
  if (isSnapshot.value) gitHash() else tagName.value
}

def gitHash(): String = sys.process.Process("git rev-parse HEAD").lineStream_!.head

val unusedWarnings = Def.setting(
  scalaBinaryVersion.value match {
    case "3" =>
      Seq(
        "-Wunused:all",
      )
    case _ =>
      Seq(
        "-Ywarn-unused",
      )
  }
)

val Scala212 = "2.12.21"
val Scala3 = "3.3.7"

val scalaVersions = Scala212 :: "2.13.18" :: Scala3 :: Nil

lazy val commonSettings = Def.settings(
  ReleasePlugin.extraReleaseCommands,
  name := msgpack4zNativeName,
  commands += Command.command("updateReadme")(UpdateReadme.updateReadmeTask),
  publishTo := (if (isSnapshot.value) None else localStaging.value),
  fullResolvers ~= { _.filterNot(_.name == "jcenter") },
  compile / javacOptions ++= Seq("-target", "6", "-source", "6"),
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
        extracted.runAggregated(extracted.get(thisProjectRef) / (Global / PgpKeys.publishSigned), state)
      },
      enableCrossBuild = false
    ),
    releaseStepCommandAndRemaining("sonaRelease"),
    setNextVersion,
    commitNextVersion,
    UpdateReadme.updateReadmeProcess,
    pushChanges
  ),
  organization := "com.github.xuwei-k",
  homepage := Some(url("https://github.com/msgpack4z")),
  licenses := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php")),
  scalacOptions ++= {
    scalaBinaryVersion.value match {
      case "3" =>
        Nil
      case _ =>
        Seq(
          "-Xlint",
        )
    }
  },
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-language:existentials,higherKinds,implicitConversions",
  ) ++ unusedWarnings.value,
  scalacOptions ++= PartialFunction
    .condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
      case Some((2, v)) if v <= 12 =>
        Seq(
          "-Yno-adapted-args",
          "-Xfuture"
        )
    }
    .toList
    .flatten,
  (Compile / doc / scalacOptions) ++= {
    val tag = tagOrHash.value
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _)) =>
        Nil
      case _ =>
        Seq(
          "-sourcepath",
          (LocalRootProject / baseDirectory).value.getAbsolutePath,
          "-doc-source-url",
          s"https://github.com/msgpack4z/msgpack4z-native/tree/${tag}€{FILE_PATH}.scala"
        )
    }
  },
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
    </scm>,
  description := "msgpack4z",
  pomPostProcess := { node =>
    import scala.xml._
    import scala.xml.transform._
    def stripIf(f: Node => Boolean) =
      new RewriteRule {
        override def transform(n: Node) =
          if (f(n)) NodeSeq.Empty else n
      }
    val stripTestScope = stripIf { n => n.label == "dependency" && (n \ "scope").text == "test" }
    new RuleTransformer(stripTestScope).transform(node)(0)
  }
) ++ Seq(Compile, Test).flatMap(c =>
  c / console / scalacOptions := {
    (c / console / scalacOptions).value.filterNot(unusedWarnings.value.toSet)
  }
)

val jsNativeSettings = Def.settings(
  (Compile / unmanagedSourceDirectories) += {
    (projectMatrixBaseDirectory.value / "js_native/src/main/scala/").getAbsoluteFile
  }
)

lazy val msgpack4zNative = projectMatrix
  .in(file("."))
  .defaultAxes()
  .settings(
    commonSettings,
    scalapropsCoreSettings,
    libraryDependencies ++= Seq(
      "com.github.scalaprops" %%% "scalaprops" % "0.10.1" % "test",
    )
  )
  .jvmPlatform(
    scalaVersions,
    libraryDependencies ++= Seq(
      "com.github.xuwei-k" % "msgpack4z-api" % "0.2.0",
    )
  )
  .jsPlatform(
    scalaVersions,
    Def.settings(
      jsNativeSettings,
      scalacOptions ++= {
        val a = (LocalRootProject / baseDirectory).value.toURI.toString
        val g = "https://raw.githubusercontent.com/msgpack4z/msgpack4z-native/" + tagOrHash.value

        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((3, _)) =>
            Seq(s"-scalajs-mapSourceURI:$a->$g/")
          case _ =>
            Seq(s"-P:scalajs:mapSourceURI:$a->$g/")
        }
      },
    )
  )
  .nativePlatform(
    scalaVersions,
    Def.settings(
      scalapropsNativeSettings,
      jsNativeSettings,
    )
  )

lazy val noPublish = Seq(
  PgpKeys.publishSigned := {},
  PgpKeys.publishLocalSigned := {},
  publishLocal := {},
  Compile / publishArtifact := false,
  publish := {}
)

commonSettings
Compile / scalaSource := baseDirectory.value / "dummy"
Test / scalaSource := baseDirectory.value / "dummy"
noPublish
autoScalaLibrary := false
TaskKey[Unit]("testSequential") := Def
  .sequential(
    msgpack4zNative.allProjects().map(_._1).sortBy(_.id).map(_ / Test / test)
  )
  .value
