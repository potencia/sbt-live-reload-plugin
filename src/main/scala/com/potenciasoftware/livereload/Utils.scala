package com.potenciasoftware.livereload

import FileIO.PathType._
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentType
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.model.HttpCharsets
import java.nio.file.{Path, Paths}

trait Utils extends FileIO {

  val `text/css(UTF-8)`: ContentType = ContentType(
    MediaTypes.`text/css`, HttpCharsets.`UTF-8`)

  val `application/javascript(UTF-8)`: ContentType = ContentType(
    MediaTypes.`application/javascript`, HttpCharsets.`UTF-8`)

  private val contentTypes: Seq[ContentType] = Seq(
    `text/html(UTF-8)`,
    `text/css(UTF-8)`,
    `text/xml(UTF-8)`,
    `application/json`,
    `application/javascript(UTF-8)`)

  private def contentTypeFromFilename(path: String): Option[ContentType] =
    for {
      parts <- Some(path.split("\\.")) if parts.length > 1
      ct <- contentTypes.find(_.mediaType.fileExtensions.contains(parts.last))
    } yield ct

  def fromResource(path: String): HttpEntity.Strict =
    fromResource(contentTypeFromFilename(path), path)

  def fromResource(contentType: Option[ContentType], path: String): HttpEntity.Strict =
    contentType.foldLeft(HttpEntity(fileIO.loadResource(path)))(_ withContentType _)

  def fromPath(root: String, path: String): Option[HttpEntity.Strict] = {
    val absPath = Paths.get(root).resolve(path)
    fileIO.pathType(absPath) match {
      case NotFound => None
      case File => Some(fromPath(contentTypeFromFilename(path), absPath))
      case Directory => Some(HttpEntity(
        s"""<!DOCTYPE html>
           |<html>
           |  <head>
           |    <title>Live Reload Server</title>
           |    <meta name="viewport" content="width=device-width, initial-scale=1.0">
           |    <style>
           |      body {
           |        background: #fff;
           |      }
           |    </style>
           |  </head>
           |  <body>
           |    <header>
           |      <h1>$path</h1>
           |    </header>
           |    <hr />
           |    <main>
           |      <pre id="contents">
           |Stuff goes here.
           |      </pre>
           |    </main>
           |    <hr />
           |  </body>
           |</html>
           |""".stripMargin) withContentType `text/html(UTF-8)`)
    }
  }

  def fromPath(contentType: Option[ContentType], path: Path): HttpEntity.Strict =
    contentType.foldLeft(HttpEntity(fileIO.loadFile(path)))(_ withContentType _ )
}
