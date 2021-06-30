package com.potenciasoftware.livereload

import FileIO.PathType._
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentType
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.model.HttpCharsets
import java.nio.file.Path

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

  def fromPath(root: Path, path: String): Option[HttpEntity.Strict] = {
    val absPath = root.resolve(path)
    fileIO.pathType(absPath) match {
      case NotFound => None
      case File => Some(fromPath(contentTypeFromFilename(path), absPath))
      case Directory =>

        val (directories, files) = absPath.toFile.listFiles.partition(_.isDirectory)

        def formatFile(f: java.io.File): String = {
          val name = f.getName + (if (f.isDirectory) "/" else "")
          s"""<a href="$name">$name</a>"""
        }

        def formatFileList(files: Array[java.io.File]) =
          if (files.isEmpty) ""
          else files
            .sortBy(_.getName)
            .map(formatFile(_))
            .mkString("\n", "\n", "")

        val parentDirectory =
          if (path.isEmpty) ""
          else """<a href="../">../</a>"""

        Some(HttpEntity(
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
             |      <h1>${if (path.isEmpty) "/" else path}</h1>
             |    </header>
             |    <hr />
             |    <main>
             |      <pre id="contents">$parentDirectory${formatFileList(directories)}${formatFileList(files)}</pre>
             |    </main>
             |    <hr />
             |  </body>
             |</html>
             |
             |""".stripMargin) withContentType `text/html(UTF-8)`)
    }
  }

  def fromPath(contentType: Option[ContentType], path: Path): HttpEntity.Strict =
    contentType.foldLeft(HttpEntity(fileIO.loadFile(path)))(_ withContentType _ )
}
