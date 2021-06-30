import sbt._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {

  val AkkaVersion = "2.6.15"
  val AkkaHttpVersion = "10.2.4"
  val ScalaTestVersion = "3.2.9"

  // jvm dependencies
  val akka = "com.typesafe.akka" %% "akka-actor" % AkkaVersion
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % AkkaVersion
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion

  // test dependencies
  val scalaTest = "org.scalatest" %% "scalatest-flatspec" % ScalaTestVersion % Test
  val scalaTestMatchers = "org.scalatest" %% "scalatest-shouldmatchers" % ScalaTestVersion % Test
  val scalaMock = "org.scalamock" %% "scalamock" % "5.1.0" % Test

  // js and shared dependencies
  //val autowire = Def.setting("com.lihaoyi" %%% "autowire" % "0.3.2")
  val dom = Def.setting("org.scala-js" %%% "scalajs-dom" % "1.1.0")
  //val upickle = Def.setting("com.lihaoyi" %%% "upickle" % "1.1.0")
}
