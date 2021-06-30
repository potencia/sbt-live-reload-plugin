package com.potenciasoftware.livereload

import sbt._

object ScalaJSLiveReloadPlugin extends AutoPlugin {

  object autoImport {
    val liveReloadServer = taskKey[String]("Ensure that the live reload server is running.")
  }

  import autoImport._
  override lazy val globalSettings: Seq[Setting[_]] = Seq(
    liveReloadServer := {
      Server.ensureRunning()
    },
  )
}
