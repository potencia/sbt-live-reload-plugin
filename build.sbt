import sbt.Keys._

inThisBuild(
  Def.settings(
    version := "0.0.1-SNAPSHOT",
    organization := "com.potenciasoftware",
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.2.8"
      }
    },
    scalacOptions ++= Seq(
      "-Ypartial-unification",
      "-feature",
      "-deprecation",
      "-unchecked",
      "-Xlint:unused"
    )
  )
)

lazy val root = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-scalajs-live-reload",
    libraryDependencies ++= Seq(
      Dependencies.akka,
      Dependencies.akkaStream,
      Dependencies.akkaHttp,
      Dependencies.scalaTest,
      Dependencies.scalaTestMatchers,
      Dependencies.scalaMock
    ),
    Compile / resources += {
      (client / Compile / fullOptJS).value
      (client / Compile / fullOptJS / artifactPath).value
    }
  )

lazy val client = project
  .in(file("client"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "live-reload",
    Compile / publishArtifact := false,
    makePom / publishArtifact := false,
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      Dependencies.dom.value
    )
  )
  .settings(
    name := "live-reload",
    Compile / publishArtifact := false,
    makePom / publishArtifact := false,
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      Dependencies.dom.value
    )
  )
  .settings(
    name := "live-reload",
    Compile / publishArtifact := false,
    makePom / publishArtifact := false,
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      Dependencies.dom.value
    )
  )
