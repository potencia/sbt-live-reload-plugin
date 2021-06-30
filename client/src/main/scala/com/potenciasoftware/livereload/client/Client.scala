package com.potenciasoftware.livereload.client

import org.scalajs.dom, dom.document, dom.raw._

object Client {

  def main(args: Array[String]): Unit = {
    val h1 = document.createElement("h1")
    document.body.appendChild(h1)
    val ws = new WebSocket(wsUrl)
    ws.onopen = { (event: Event) =>
      println("Opened the WS connection")
    }
    ws.onmessage = { (event: MessageEvent) =>
      h1.textContent = event.data.toString
    }
  }

  private lazy val wsUrl = {

    val l = dom.document.location
    import l._
    val ws = protocol match {
      case "https:" => "wss:"
      case "http:" => "ws:"
    }
    s"$ws//$host/hello"
  }
}
