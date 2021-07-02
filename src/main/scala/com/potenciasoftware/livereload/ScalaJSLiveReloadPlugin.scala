package com.potenciasoftware.livereload

import sbt._, Keys._, nio.Keys._
import java.nio.file.Path
import org.scalajs.sbtplugin.ScalaJSPlugin, ScalaJSPlugin.autoImport._

object ScalaJSLiveReloadPlugin extends AutoPlugin {

  override def trigger = allRequirements
  override def requires = ScalaJSPlugin

  object autoImport {
    val liveReloadHost = settingKey[String]("The hostname used when binding the live reload server socket.")
    val liveReloadPort = settingKey[Int]("The port used when binding the live reload server socket.")
    val liveReloadRootPath = settingKey[Path]("The path to use as the root directory of the live reload server.")
    val liveReload = taskKey[Unit]("Requests a browser reload")

    val liveReloadMarkdown = taskKey[Seq[Path]]("Watch this task to trigger a live reload when any Markdown file in the project changes.")
  }

  val server = settingKey[Server]("The live reload server")

  val x = Compile / fastLinkJS

  import autoImport._
  override def projectSettings: Seq[Setting[_]] = Seq(
    liveReloadHost := "0.0.0.0",
    liveReloadPort := 12345,
    liveReloadRootPath := baseDirectory.value.toPath,
    server := {
      val server = new Server(liveReloadHost.value, liveReloadPort.value, liveReloadRootPath.value)
      server.startup()
      server
    },
    liveReload := {
      println("Triggering a reload")
      server.value.reload()
    },
    liveReloadMarkdown / fileInputs += baseDirectory.value.toGlob / ** / "*.md",
    liveReloadMarkdown := { liveReloadMarkdown.inputFiles },
    liveReloadMarkdown := liveReloadMarkdown.triggeredBy(Compile / fastLinkJS).value,
    liveReload := liveReload.triggeredBy(liveReloadMarkdown).value,
    Global / onUnload := {
      (Global / onUnload).value.compose { state =>
        server.value.shutdown()
        state
      }
    }
  )
}
