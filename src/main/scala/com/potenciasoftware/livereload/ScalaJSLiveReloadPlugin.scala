package com.potenciasoftware.livereload

import sbt._
import org.scalajs.sbtplugin.ScalaJSPlugin, ScalaJSPlugin.autoImport._

object ScalaJSLiveReloadPlugin extends AutoPlugin {

  override def trigger = allRequirements
  override def requires = LiveReloadPlugin && ScalaJSPlugin

  import LiveReloadPlugin.autoImport._

  val liveReloadFastOptJS = taskKey[Unit]("Live reload triggered by fastOptJS")
  val liveReloadFullOptJS = taskKey[Unit]("Live reload triggered by fullOptJS")
  val liveReloadFastLinkJS = taskKey[Unit]("Live reload triggered by fastLinkJS")
  val liveReloadFullLinkJS = taskKey[Unit]("Live reload triggered by fullLinkJS")

  override def projectSettings: Seq[Setting[_]] = Seq(

    liveReloadFastOptJS := { liveReload.value },
    liveReloadFastOptJS := liveReloadFastOptJS.triggeredBy(Compile / fastOptJS).value,

    liveReloadFullOptJS := { liveReload.value },
    liveReloadFullOptJS := liveReloadFullOptJS.triggeredBy(Compile / fullOptJS).value,

    liveReloadFastLinkJS := { liveReload.value },
    liveReloadFastLinkJS := liveReloadFastLinkJS.triggeredBy(Compile / fastLinkJS).value,

    liveReloadFullLinkJS := { liveReload.value },
    liveReloadFullLinkJS := liveReloadFullLinkJS.triggeredBy(Compile / fullLinkJS).value,
  )
}
