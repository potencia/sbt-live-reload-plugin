package com.potenciasoftware.livereload.client

import org.scalajs.dom, dom.document, dom.ext.Ajax
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object Client {

  def main(args: Array[String]): Unit = {

    val url = s"http://${dom.document.location.host}/reload"
    var reloadNum: Option[Int] = None

    def check(): Unit = {
      Ajax.get(url).onComplete {
        case Success(data) =>
          val count = data.responseText.toInt
          val loop = reloadNum match {
            case None =>
              println("Connected")
              reloadNum = Some(count)
              true
            case Some(prev) if prev != count =>
              println("Reloading...")
              document.location.reload(true)
              false
            case _ =>
              true
          }
          if (loop) dom.window.setTimeout(() => check(), 1000)
        case Failure(e) =>
          reloadNum foreach { _ =>
            println(s"Disconnected ($e)")
            reloadNum = None
          }
          dom.window.setTimeout(() => check(), 30000)
      }
    }

    check()
  }
}
