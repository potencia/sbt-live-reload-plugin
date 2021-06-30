package com.potenciasoftware.livereload

import java.nio.file.Path

trait FileIO {
  def fileIO: FileIO.Service
}

object FileIO {

  object PathType extends Enumeration {
    val NotFound = Value
    val File = Value
    val Directory = Value
  }
  type PathType = PathType.Value
  import PathType._

  trait Service {

    def pathType(path: Path): PathType

    def loadResource(path: String): String
    def loadFile(path: Path): String
  }

  trait Live extends FileIO {

    override val fileIO: Service = new Service {

      override def pathType(path: Path): PathType = {
        val file = path.toFile
        if (!file.exists) NotFound
        else if (file.isDirectory) Directory
        else File
      }

      private def loadSource(source: io.Source): String = {
        try source.mkString
        finally source.close()
      }

      override def loadResource(path: String): String =
        loadSource(io.Source.fromInputStream(
          getClass.getClassLoader.getResourceAsStream(path)))

      override def loadFile(path: Path): String =
        loadSource(io.Source.fromFile(path.toFile))
    }
  }
}
