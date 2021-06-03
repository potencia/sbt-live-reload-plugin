import sbt._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {

  // jvm dependencies
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.1.12"
  val akka = "com.typesafe.akka" %% "akka-actor" % "2.6.5"
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.6.5"

  // js and shared dependencies
  val autowire = Def.setting("com.lihaoyi" %%% "autowire" % "0.3.2")
  val dom = Def.setting("org.scala-js" %%% "scalajs-dom" % "1.0.0")
  val upickle = Def.setting("com.lihaoyi" %%% "upickle" % "1.1.0")
}
