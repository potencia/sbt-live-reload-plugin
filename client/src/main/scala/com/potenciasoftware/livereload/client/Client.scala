package com.potenciasoftware.livereload.client

import org.scalajs.dom, dom.document, dom.raw._

object Client {

  def main(args: Array[String]): Unit = {
    val ws = new WebSocket(wsUrl)
    ws.onopen = { (event: Event) =>
      println("Opened the WS connection")
    }
    ws.onmessage = { (event: MessageEvent) =>
      if (event.data == "RELOAD")
        document.location.reload(true)
      else println(s"Unknown event: ${event.data}")
    }
  }

  private lazy val wsUrl = {

    val l = dom.document.location
    import l._
    val ws = protocol match {
      case "https:" => "wss:"
      case "http:" => "ws:"
    }
    s"$ws//$host/reload"
  }
}
