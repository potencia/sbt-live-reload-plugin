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
import java.io.File
import sbt.util.Logger
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.marshalling.Marshaller
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCode

class Server(
    log: Logger,
    host: String,
    port: Int,
    root: File,
    preferRaw: Boolean)
    extends Utils
    with FileIO.Live {

  implicit private val optionalStrictHttpEntityM: ToResponseMarshaller[Option[HttpEntity.Strict]] =
    Marshaller.apply[Option[HttpEntity.Strict], HttpResponse] { implicit ctx => o =>
      if (o.isEmpty) implicitly[ToResponseMarshaller[StatusCode]].apply(StatusCodes.NotFound)
      else implicitly[ToResponseMarshaller[HttpEntity.Strict]].apply(o.get)
    }

  private val rootPath = root.toPath

  private val routes: Route = concat(
    respondWithHeader(`Cache-Control`(
      CacheDirectives.`no-store`,
      CacheDirectives.`max-age`(0))) {
        get { extractUnmatchedPath { path => parameterMap { params =>
          path.toString match {
            case "/reload" =>
              complete(currentReload.toString)
            case "/live-reload.js" =>
              complete(fromResource("live-reload-opt.js"))
            case path =>
              complete(fromPath(rootPath, path.drop(1).mkString, preferRaw, params))
          }
        }}}
      })

  private var state: Option[(ActorSystem, ServerBinding)] = None
  private val _host = {
    import java.net._

    if (host != "0.0.0.0") host
    else {
      val socket = new Socket()
      try {
        socket.connect(new InetSocketAddress("potenciasoftware.com", 80))
        socket.getLocalAddress.getHostAddress
      } finally socket.close()
    }
  }

  def startup(): Unit = {
    if (state.isEmpty) {

      val cl = getClass.getClassLoader
      implicit val system = ActorSystem(
        "live-reload",
        config = ConfigFactory.load(cl),
        classLoader = cl)

      val binding = Await.result(
        Http().newServerAt(_host, port).bindFlow(routes),
        30.seconds)

      state = Some((system, binding))
      log.info(s"Started the live reload server. Try: http://${_host}:$port/?liveReload=on")
    }
  }

  def shutdown(): Unit = {

    state foreach { case (system, binding) =>
      Await.result(binding.unbind(), 30.seconds)
      Await.result(system.terminate(), 30.seconds)
      log.info(s"Stopped the live reload server")
    }

    currentReload = 0
    state = None
  }

  private var currentReload = 0

  def reload(): Unit = {
    if (state.isDefined) {
      currentReload += 1
      log.info("Triggered a live reload...")
    }
  }
}

