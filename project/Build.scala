import sbt._, Keys._
import sbtrelease._
import xerial.sbt.Sonatype._
import ReleaseStateTransformations._
import com.typesafe.sbt.pgp.PgpKeys

object build extends Build {

  private def gitHash: String = scala.util.Try(
    sys.process.Process("git rev-parse HEAD").lines_!.head
  ).getOrElse("master")

  private val msgpack4zNativeName = "msgpack4z-native"

  val modules = msgpack4zNativeName :: Nil

  lazy val msgpack4zNative = Project("msgpack4z-native", file(".")).settings(
    ReleasePlugin.releaseSettings ++ ReleasePlugin.extraReleaseCommands ++ sonatypeSettings: _*
  ).settings(
    name := msgpack4zNativeName,
    javacOptions in compile ++= Seq("-target", "6", "-source", "6"),
    commands += Command.command("updateReadme")(UpdateReadme.updateReadmeTask),
    libraryDependencies ++= (
      ("com.github.xuwei-k" % "msgpack4z-api" % "0.1.0") ::
      ("org.scalacheck" %% "scalacheck" % "1.12.1" % "test") ::
      Nil
    ),
    ReleasePlugin.ReleaseKeys.releaseProcess := Seq[ReleaseStep](
      ReleaseStep{ state =>
        assert(Sxr.disableSxr == false)
        state
      },
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
      "-Ywarn-unused" ::
      "-Ywarn-unused-import" ::
      Nil
    ),
    scalacOptions in (Compile, doc) ++= {
      val tag = if(isSnapshot.value) gitHash else { "v" + version.value }
      Seq(
        "-sourcepath", (baseDirectory in LocalRootProject).value.getAbsolutePath,
        "-doc-source-url", s"https://github.com/msgpack4z/msgpack4z-native/tree/${tag}€{FILE_PATH}.scala"
      )
    },
    scalaVersion := "2.11.5",
    crossScalaVersions := scalaVersion.value :: Nil,
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
        <tag>{if(isSnapshot.value) gitHash else { "v" + version.value }}</tag>
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
  ).settings(
    Sxr.subProjectSxr(Compile, "classes.sxr"): _*
  )

}
