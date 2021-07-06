package com.potenciasoftware.livereload

import sbt._, Keys._, nio.Keys._
import java.nio.file.Path

object LiveReloadPlugin extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {

    val liveReload = taskKey[Unit]("Requests a browser reload")
    val liveReloadStartServer = taskKey[Unit]("Starts the live reload server")
    val liveReloadStopServer = taskKey[Unit]("Stops the live reload server")
    val liveReloadMarkdown = taskKey[Seq[Path]]("Watch this task to trigger a live reload when any Markdown file in the project changes.")

    object LiveReloadStartMode extends Enumeration {
      val OnStartup, OnCompile, Manual = Value
    }
    type LiveReloadStartMode = LiveReloadStartMode.Value

    val liveReloadHost = settingKey[String]("The hostname used when binding the live reload server socket.")
    val liveReloadPort = settingKey[Int]("The port used when binding the live reload server socket.")
    val liveReloadRootPath = settingKey[File]("The path to use as the root directory of the live reload server.")
    val liveReloadStartMode = settingKey[LiveReloadStartMode]("When to start the live reload server.")
    val liveReloadPreferRaw = settingKey[Boolean]("When returning resources prefer raw content to rendered content.")
  }
  import autoImport._
  import LiveReloadStartMode._

  private val server = settingKey[Server]("The live reload server")

  override def globalSettings: Seq[Setting[_]] = Seq(
    liveReloadHost := "0.0.0.0",
    liveReloadPort := 12345,
    liveReloadRootPath := (ThisBuild / baseDirectory).value,
    liveReloadStartMode := OnStartup,
    liveReloadPreferRaw := false,
  )

  override def projectSettings: Seq[Setting[_]] = Seq(

    Global / server := {
      new Server(sLog.value, liveReloadHost.value, liveReloadPort.value, liveReloadRootPath.value, liveReloadPreferRaw.value)
    },
    Global / onLoad := {
      (Global / onLoad).value compose { state =>
        if (liveReloadStartMode.value == OnStartup)
          server.value.startup()
        state
      }
    },
    Global / onUnload := {
      (Global / onUnload).value compose { state =>
        server.value.shutdown()
        state
      }
    },
    Compile / compile := (Compile / compile)
      .dependsOn(Def.task {
        if (liveReloadStartMode.value != Manual)
          server.value.startup()
      }).value,

    liveReloadStartServer := { server.value.startup() },
    liveReloadStopServer := { server.value.shutdown() },

    liveReloadMarkdown / fileInputs += liveReloadRootPath.value.toGlob / ** / "*.md",
    liveReloadMarkdown := { liveReloadMarkdown.inputFiles },

    liveReload := { server.value.reload() },
    liveReload := liveReload.triggeredBy(liveReloadMarkdown).value
  )
}
