package com.potenciasoftware.livereload

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalamock.scalatest.MockFactory
import akka.http.scaladsl.model.ContentType
import akka.http.scaladsl.model.ContentTypes._
import java.nio.file.Paths
import FileIO.File
import akka.http.scaladsl.marshalling.Marshal
import scala.concurrent.Awaitable
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.http.scaladsl.model.HttpResponse

class UtilsTest extends AnyFlatSpec with Matchers
    with MockFactory with Utils {

  override val fileIO = mock[FileIO.Service]

  private implicit class TestAwaitable[A](a: Awaitable[A]) {
    def await: A = Await.result(a, 1.second)
  }

  def hasRawContentType(fileName: String, expected: ContentType): Unit = {

    it should s"have $expected when loaded from resource" in {
      fileIO.loadResource _ expects fileName returns File(Paths.get(fileName), "content")
      val entity = fromResource(fileName)
      entity.map(_.data.utf8String) shouldBe Some("content")
      entity.map(_.contentType) shouldBe Some(expected)
    }

    it should s"have $expected when loaded from path with preferRaw=true" in {
      val root = Paths.get("root")
      val path = root.resolve(fileName)
      fileIO.loadFile _ expects path returns File(path, "content")
      val entity = fromPath(root, fileName, true, Map.empty).get
      entity.data.utf8String shouldBe "content"
      entity.contentType shouldBe expected
    }

    it should s"have $expected when loaded from path with preferRaw=false and raw=true" in {
      val root = Paths.get("root")
      val path = root.resolve(fileName)
      fileIO.loadFile _ expects path returns File(path, "content")
      val entity = fromPath(root, fileName, false, Map("raw" -> "true")).get
      entity.data.utf8String shouldBe "content"
      entity.contentType shouldBe expected
    }
  }

  def hasRenderedContentType(fileName: String): Unit = {

    val expected = `text/html(UTF-8)`
    val root = Paths.get("root")
    val path = root.resolve(fileName)

    it should s"have $expected when loaded with preferRaw=false" in {
      fileIO.loadFile _ expects path returns File(path, "content")
      val entity = fromPath(root, fileName, false, Map.empty).get
      entity.contentType shouldBe expected
    }

    it should s"have $expected when loaded with preferRaw=true and raw=false" in {
      fileIO.loadFile _ expects path returns File(path, "content")
      val entity = fromPath(root, fileName, true, Map("raw" -> "false")).get
      entity.data.utf8String should endWith("</html>")
      entity.contentType shouldBe expected
    }
  }

  "File" should "marshal to text/plain" in {
    val file = File(Paths.get("filename.txt"), "content")
    val response = Marshal(file).to[HttpResponse].await
    response.entity.contentType shouldBe `text/plain(UTF-8)`
  }

  "no extension" should behave like hasRawContentType("test", `text/plain(UTF-8)`)

  "*.txt" should behave like hasRawContentType("test.txt", `text/plain(UTF-8)`)

  "*.scala" should behave like hasRawContentType("test.scala", `text/plain(UTF-8)`)

  "*.htm" should behave like hasRawContentType("test.htm", `text/html(UTF-8)`)

  "*.html" should behave like hasRawContentType("test.html", `text/html(UTF-8)`)

  "*.css" should behave like hasRawContentType("test.css", `text/css(UTF-8)`)

  "*.xml" should behave like hasRawContentType("test.xml", `text/xml(UTF-8)`)

  "*.json" should behave like hasRawContentType("test.json", `application/json`)

  "*.js" should behave like hasRawContentType("test.js", `application/javascript(UTF-8)`)
  "*.js" should behave like hasRenderedContentType("test.js")

  "*.md" should behave like hasRawContentType("test.md", `text/plain(UTF-8)`)
  "*.md" should behave like hasRenderedContentType("test.md")

}
