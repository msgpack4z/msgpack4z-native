import sbt._, Keys._
import sbtrelease._
import xerial.sbt.Sonatype._
import ReleaseStateTransformations._
import sbtrelease.ReleasePlugin.autoImport._
import com.typesafe.sbt.pgp.PgpKeys
import org.scalajs.sbtplugin.cross.CrossProject
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object build extends Build {

  private[this] val tagName = Def.setting{
    s"v${if (releaseUseGlobalVersion.value) (version in ThisBuild).value else version.value}"
  }

  private[this] val tagOrHash = Def.setting{
    if(isSnapshot.value) gitHash() else tagName.value
  }

  private[this] def gitHash(): String = sys.process.Process("git rev-parse HEAD").lines_!.head

  private val msgpack4zNativeName = "msgpack4z-native"

  val modules = msgpack4zNativeName :: Nil

  private[this] val unusedWarnings = (
    "-Ywarn-unused" ::
    "-Ywarn-unused-import" ::
    Nil
  )

  private[this] val Scala211 = "2.11.8"

  val scalacheckVersion = SettingKey[String]("scalacheckVersion")

  lazy val commonSettings = ReleasePlugin.extraReleaseCommands ++ Seq(
    commands += Command.command("updateReadme")(UpdateReadme.updateReadmeTask)
  )

  lazy val msgpack4zNative = CrossProject("msgpack4z-native", file("."), CustomCrossType).settings(
    commonSettings ++ sonatypeSettings : _*
  ).settings(
    name := msgpack4zNativeName,
    resolvers += Opts.resolver.sonatypeReleases,
    fullResolvers ~= {_.filterNot(_.name == "jcenter")},
    javacOptions in compile ++= Seq("-target", "6", "-source", "6"),
    scalacheckVersion := "1.12.5",
    libraryDependencies ++= (
      ("org.scalacheck" %%% "scalacheck" % scalacheckVersion.value % "test") ::
      Nil
    ),
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
      ReleaseStep(state => Project.extract(state).runTask(PgpKeys.publishSigned, state)._1),
      setNextVersion,
      commitNextVersion,
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
      "-language:existentials" ::
      "-language:higherKinds" ::
      "-language:implicitConversions" ::
      "-Yno-adapted-args" ::
      Nil
    ) ::: unusedWarnings,
    scalacOptions in (Compile, doc) ++= {
      val tag = tagOrHash.value
      Seq(
        "-sourcepath", (baseDirectory in LocalRootProject).value.getAbsolutePath,
        "-doc-source-url", s"https://github.com/msgpack4z/msgpack4z-native/tree/${tag}€{FILE_PATH}.scala"
      )
    },
    scalaVersion := Scala211,
    crossScalaVersions := Scala211 :: "2.12.0-M3" :: Nil,
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
  ).jvmSettings(
    Sxr.settings : _*
  ).settings(
    Seq(Compile, Test).flatMap(c =>
      scalacOptions in (c, console) ~= {_.filterNot(unusedWarnings.toSet)}
    ) : _*
  ).jvmSettings(
    libraryDependencies ++= (
      ("com.github.xuwei-k" % "msgpack4z-api" % "0.2.0") ::
      Nil
    )
  ).jsSettings(
    scalacOptions += {
      val a = (baseDirectory in LocalRootProject).value.toURI.toString
      val g = "https://raw.githubusercontent.com/msgpack4z/msgpack4z-native/" + tagOrHash.value
      s"-P:scalajs:mapSourceURI:$a->$g/"
    }
  )

  lazy val msgpack4zNativeJVM = msgpack4zNative.jvm

  lazy val msgpack4zNativeJS = msgpack4zNative.js

  lazy val root = Project(
    "root", file(".")
  ).settings(
    commonSettings : _*
  ).settings(
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

}
