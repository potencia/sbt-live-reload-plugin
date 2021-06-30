package com.potenciasoftware.livereload

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http, Http.ServerBinding
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.ws._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl._
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random
import akka.http.scaladsl.model.StatusCodes

object Server extends Utils with FileIO.Live {

  private val wsFlow: Flow[Message,Message,NotUsed] = Flow.fromSinkAndSource(
    Sink.foreach(println),
    Source.tick(5.seconds, 5.seconds, (): Unit).map(_ => TextMessage(Random.nextInt().toString)))

  private val routes: Route = concat(
    path("hello") { handleWebSocketMessages(wsFlow) },
    respondWithHeader(`Cache-Control`(
      CacheDirectives.`no-store`,
      CacheDirectives.`max-age`(0))) {
        get { extractUnmatchedPath { path =>
          path.toString match {
            case "/index.html" =>
              complete(fromResource("html/index.html"))
            case "/live-reload.js" =>
              complete(fromResource("live-reload-opt.js"))
            case path =>
              fromPath(".", path.drop(1).mkString) match {
                case Some(e) => complete(e)
                case None => complete(StatusCodes.NotFound)
              }
          }
        }}
      })

  private var _actorSystem: Option[ActorSystem] = None
  private def actorSystem: ActorSystem = _actorSystem match {
    case Some(a) => a
    case None =>
      val cl = getClass.getClassLoader
      _actorSystem = Some(ActorSystem(
        "live-reload",
        config = ConfigFactory.load(cl),
        classLoader = cl))
      _actorSystem.get
  }

  private var _binding: Option[ServerBinding] = None

  def ensureRunning(): String = {
    val b = _binding match {
      case Some(b) => b
      case None =>

        implicit val system = actorSystem
        _binding = Some(Await.result(Http()
          .newServerAt("0.0.0.0", 12345)
          .bindFlow(routes), 30.seconds))
        _binding.get
    }
    b.localAddress.toString
  }

  def ensureAkkaStopped(): Unit = {
    _binding map(a => Await.result(a.unbind(), 30.seconds))
    _binding = None
    _actorSystem map(a => Await.ready(a.terminate, 30.seconds))
    _actorSystem = None
  }
}
