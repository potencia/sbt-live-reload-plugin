package com.potenciasoftware.livereload

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentType
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.model.HttpCharsets
import java.nio.file.Path
import org.commonmark.parser.Parser
import org.commonmark.node.Node
import org.commonmark.renderer.html.HtmlRenderer
import akka.util.ByteString
import FileIO.{Directory, File, Location, NotFound}

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

  private def contentTypeFromExtension(ext: Option[String]): Option[ContentType] =
    for {
      _ext <- ext
      ct <- contentTypes.find(_.mediaType.fileExtensions.contains(_ext))
    } yield ct

  def fromResource(path: String): Option[HttpEntity.Strict] =
    Some(fileIO.loadResource(path))
      .collect { case f: File =>
        contentTypeFromExtension(f.ext)
          .foldLeft(HttpEntity(f.content))(_ withContentType _)
      }

  private val trueRegex = "(?i)^(true)|(yes)|(on)$".r
  private def testShowDotFiles(params: Map[String, String]): Boolean =
    params.get("showDotFiles").exists(trueRegex.findFirstIn(_).isDefined)
  private def testLiveReload(params: Map[String, String]): Boolean =
    params.get("liveReload").exists(trueRegex.findFirstIn(_).isDefined)
  private def testRaw(params: Map[String, String], preferRaw: Boolean): Boolean = {
    if (preferRaw) params.get("raw").forall(trueRegex.findFirstIn(_).isDefined)
    else params.get("raw").exists(trueRegex.findFirstIn(_).isDefined)
  }

  private def wrapJavascript(src: Option[String], preferRaw: Boolean): HttpEntity.Strict => HttpEntity.Strict = { e =>
    if (src.isDefined && e.contentType == `application/javascript(UTF-8)`)
      HttpEntity("<!DOCTYPE html><html><body>" +
        s"""<script src="/${src.get}${if (preferRaw) "" else "?raw=true"}"></script></body></html>""")
          .withContentType(`text/html(UTF-8)`)
    else e
  }

  private def addLiveReload(add: Boolean): HttpEntity.Strict => HttpEntity.Strict = { e =>
    if (add && e.contentType == `text/html(UTF-8)`)
      e.copy(data = ByteString(
        e.data.utf8String.replaceAllLiterally("</body>",
          """<script src="/live-reload.js"></script></body>""")))
    else e
  }

  def fromPath(root: Path, path: String, preferRaw: Boolean, params: Map[String, String]): Option[HttpEntity.Strict] = {
    val liveReload = testLiveReload(params)
    val raw = testRaw(params, preferRaw)
    (fileIO.loadFile(root.resolve(path)) match {
      case f: File => f.ext match {
        case Some("md") if !raw => fromMd(f)
        case ext => Some(fromFile(contentTypeFromExtension(ext), f))
      }
      case d: Directory =>
        fromDirectory(d, path.isEmpty, testShowDotFiles(params), liveReload)
      case _: NotFound => None
    })
      .map(wrapJavascript(Some(path.toString).filter(_ => !raw), preferRaw))
      .map(addLiveReload(liveReload))
  }

  def fromFile(contentType: Option[ContentType], file: File): HttpEntity.Strict =
    contentType.foldLeft(HttpEntity(file.content))(_ withContentType _ )

  def fromDirectory(dir: Directory, isRoot: Boolean, showDotFiles: Boolean, liveReload: Boolean): Option[HttpEntity.Strict] = {

    def a(text: String): String =
      if (liveReload) s"""<a href="$text?liveReload=on">$text</a>"""
      else s"""<a href="$text">$text</>"""

    val (directories, files) =
      dir.list
        .filter(f => (f.name, showDotFiles) match {
          case (_, true) => true
          case (name, false) => !name.startsWith(".")
        })
        .partition(_.isDirectory)

    def formatFile(f: Location): String =
      a(f.name + (if (f.isDirectory) "/" else ""))

    def formatFileList(files: Seq[Location]) =
      if (files.isEmpty) ""
      else files
        .sortBy(_.name)
        .map(formatFile(_))
        .mkString("\n", "\n", "")

    val parentDirectory = if (isRoot) "" else a("../")

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
         |      <h1>${if (isRoot) "/" else dir.path}</h1>
         |    </header>
         |    <hr />
         |    <main>
         |      <pre id="contents">$parentDirectory${formatFileList(directories)}${formatFileList(files)}</pre>
         |    </main>
         |    <hr />
         |  </body>
         |</html>
         |""".stripMargin) withContentType `text/html(UTF-8)`)
  }

  def fromMd(f: File): Option[HttpEntity.Strict] = {
    val parser = Parser.builder().build()
    val document: Node = parser.parse(f.content)
    val renderer = HtmlRenderer.builder().build()
    val html = renderer.render(document)
    Some(HttpEntity(
      "<!DOCTYPE html><html><head><style>body{font-family: BlinkMacSystemFont,Segoe UI,Helvetica,Arial,sans-serif;}pre{background-color: #eeeef4; padding: .25em;}</style><head>" +
      s"""<body>$html<script src="/live-reload.js"></script></body></html>""") withContentType `text/html(UTF-8)`)
  }
}
