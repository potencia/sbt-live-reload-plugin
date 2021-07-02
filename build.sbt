inThisBuild(Def.settings(
  version := "0.0.1-SNAPSHOT",
  organization := "com.potenciasoftware",
  pluginCrossBuild / sbtVersion := {
    scalaBinaryVersion.value match {
      case "2.12" => "1.3.0"
    }
  },
  scalacOptions ++= Seq(
    "-Ypartial-unification",
    "-feature",
    "-deprecation",
    "-unchecked",
    "-Xlint:unused",
    ),
  ))

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-scalajs-live-reload",
    Compile / resources += {
      (client / Compile / fullOptJS).value
      (client / Compile / fullOptJS / artifactPath).value
    },
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.6.0"),
    libraryDependencies ++= Seq(
      Dependencies.akka,
      Dependencies.akkaStream,
      Dependencies.akkaHttp,
      Dependencies.commonMark,
      Dependencies.scalaTest,
      Dependencies.scalaTestMatchers,
      Dependencies.scalaMock,
    ),
  )

lazy val client = project
  .in(file("client"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "live-reload",
    Compile / publishArtifact := false,
    makePom / publishArtifact := false,
    Compile / scalaJSLinkerConfig ~= { _.withSourceMap(false) },
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      Dependencies.dom.value
    ),
  )
