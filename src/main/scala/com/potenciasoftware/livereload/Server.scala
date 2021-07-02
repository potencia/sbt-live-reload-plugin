package com.potenciasoftware.livereload

import akka.actor.ActorSystem
import akka.http.scaladsl.Http, Http.ServerBinding
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import java.nio.file.Paths
import java.nio.file.Path

class Server(host: String, port: Int, root: Path) extends Utils with FileIO.Live {

  private var currentReload = 0

  def reload(): Unit = {
    currentReload += 1
  }

  private val routes: Route = concat(
    respondWithHeader(`Cache-Control`(
      CacheDirectives.`no-store`,
      CacheDirectives.`max-age`(0))) {
        get { extractUnmatchedPath { path =>
          path.toString match {
            case "/reload" =>
              complete(currentReload.toString)
            case "/live-reload.js" =>
              complete(fromResource("live-reload-opt.js"))
            case path =>
              fromPath(root, path.drop(1).mkString) match {
                case Some(e) => complete(e)
                case None => complete(StatusCodes.NotFound)
              }
          }
        }}
      })

  private var state: Option[(ActorSystem, ServerBinding)] = None

  def startup(): String = (state match {

    case None =>

      val cl = getClass.getClassLoader
      implicit val system = ActorSystem(
        "live-reload",
        config = ConfigFactory.load(cl),
        classLoader = cl)

      val binding = Await.result(
        Http().newServerAt(host, port).bindFlow(routes),
        30.seconds)

      state = Some((system, binding))
      binding

    case Some((_, binding)) => binding

  }).localAddress.toString

  def shutdown(): Unit = {

    state foreach { case (system, binding) =>
      Await.result(binding.unbind(), 30.seconds)
      Await.result(system.terminate(), 30.seconds)
    }

    currentReload = 0
    state = None
  }
}

object Server extends Utils with FileIO.Live {

  private val server: Server = new Server("0.0.0.0", 12345, Paths.get("."))

  def ensureRunning(): String = server.startup()

  def ensureAkkaStopped(): Unit = {
    server.shutdown()
  }

  def triggerReload(): Unit = {
    server.reload()
  }
}
