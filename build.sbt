import sbt.Keys._

inThisBuild(Def.settings(
  version := "0.0.1",
  organization := "com.potenciasoftware",
  scalaVersion := "2.13.6",
  scalacOptions ++= Seq("-feature", "-deprecation", "-Wunused:imports", "-Wunused"),
))

lazy val root = project.in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "liveReload",
    publishArtifact in Test := false,
    publishTo := Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"),
    pomExtra :=
      <url>https://github.com/potencia/sbt-live-reload-plugin</url>
      <licenses>
        <license>
          <name>MIT license</name>
          <url>http://www.opensource.org/licenses/mit-license.php</url>
        </license>
      </licenses>
      <scm>
        <url>https://github.com/potencia/sbt-live-reload-plugin.git</url>
        <connection>scm:git://github.com/potencia/sbt-live-reload-plugin.git</connection>
      </scm>
      <developers>
        <developer>
          <id>johnsonjii</id>
          <name>John Johnson II</name>
          <url>https://github.com/johnsonjii</url>
        </developer>
      </developers>
    ,
    (resources in Compile) += {
      (fullOptJS in (client, Compile)).value
      (artifactPath in (client, Compile, fullOptJS)).value
    },
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.5.1"),
    libraryDependencies ++= Seq(
      Dependencies.akkaHttp,
      Dependencies.akka,
      Dependencies.akkaStream,
      Dependencies.autowire.value,
      Dependencies.upickle.value
    )
  )

