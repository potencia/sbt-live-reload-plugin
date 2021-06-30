package com.potenciasoftware.livereload

import FileIO.PathType._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalamock.scalatest.MockFactory
import akka.http.scaladsl.model.ContentType
import akka.http.scaladsl.model.ContentTypes._
import java.nio.file.Paths

class UtilsTest extends AnyFlatSpec with Matchers
    with MockFactory with Utils {

  override val fileIO = mock[FileIO.Service]

  def hasContentType(fileName: String, expected: ContentType): Unit = {

    it should s"have $expected when loaded from resource" in {
      fileIO.loadResource _ expects fileName returns "content"
      val entity = fromResource(fileName)
      entity.data.utf8String shouldBe "content"
      entity.contentType shouldBe expected
    }

    it should s"have $expected when loaded from path" in {
      val root = Paths.get("root")
      val path = root.resolve(fileName)
      fileIO.pathType _ expects path returns File
      fileIO.loadFile _ expects path returns "content"
      val entity = fromPath(root, fileName).get
      entity.data.utf8String shouldBe "content"
      entity.contentType shouldBe expected
    }
  }

  "no extension" should behave like hasContentType("test", `text/plain(UTF-8)`)

  "*.txt" should behave like hasContentType("test.txt", `text/plain(UTF-8)`)

  "*.scala" should behave like hasContentType("test.scala", `text/plain(UTF-8)`)

  "*.htm" should behave like hasContentType("test.htm", `text/html(UTF-8)`)

  "*.html" should behave like hasContentType("test.html", `text/html(UTF-8)`)

  "*.css" should behave like hasContentType("test.css", `text/css(UTF-8)`)

  "*.xml" should behave like hasContentType("test.xml", `text/xml(UTF-8)`)

  "*.js" should behave like hasContentType("test.js", `application/javascript(UTF-8)`)

  "*.json" should behave like hasContentType("test.json", `application/json`)

}
