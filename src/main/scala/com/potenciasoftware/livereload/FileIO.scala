package com.potenciasoftware.livereload

import java.nio.file.Path
import java.nio.file.Paths

/** Access to the files (classpath/file system) that can be mocked in tests. */
trait FileIO {
  def fileIO: FileIO.Service
}

object FileIO {

  private def loadSource(source: io.Source): String = {
    try source.mkString
    finally source.close()
  }

  /** Operations available at a given resource location */
  sealed trait Location {
    val path: Path

    val isDirectory: Boolean = false

    lazy val name: String = path.getFileName.toString

    /** The text following the last period. */
    lazy val ext: Option[String] =
      Some(name.split("\\.")).filter(_.length > 1).map(_.last)
  }
  object Location {
    def apply(path: Path): Location = {
      val file = path.toFile()
      if (!file.exists) NotFound(path)
      else if (file.isDirectory) Directory(path)
      else File(path, loadSource(io.Source.fromFile(path.toFile)))
    }
  }

  /** When the location cannot be located. */
  trait NotFound extends Location
  object NotFound {

    def apply(path: Path): NotFound = {
      val _path = path
      new NotFound { val path = _path }
    }

    def unapply(a: NotFound): Option[Path] = Some(a.path)
  }

  trait File extends Location {
    def content: String
  }
  object File {

    def apply(path: Path, content: => String): File = {
      val _path = path
      val _content = () => content
      new File {
        val path = _path
        lazy val content: String = _content()
      }
    }

    def unapply(a: File): Option[(Path, String)] = Some(a.path, a.content)
  }

  trait Directory extends Location {
    override val isDirectory: Boolean = true
    def list: Seq[Location]
  }
  object Directory {

    def apply(path: Path): Directory = {
      val _path = path
      new Directory {
        val path = _path
        def list: Seq[Location] =
          path.toFile.listFiles.toSeq.map(f => Location(f.toPath))
      }
    }

    def unapply(a: Directory): Option[Path] = Some(a.path)
  }

  trait Service {

    def loadResource(path: String): Location
    def loadFile(path: Path): Location
  }

  trait Live extends FileIO {

    override val fileIO: Service = new Service {

      override def loadResource(path: String): Location = {
        val _path = Paths.get(path)
        Option(getClass.getClassLoader.getResourceAsStream(path)) map { stream =>
          File(_path, loadSource(io.Source.fromInputStream(
            getClass.getClassLoader.getResourceAsStream(path))))
        } getOrElse NotFound(_path)
      }

      override def loadFile(path: Path): Location = Location(path)
    }
  }
}
