package uml2shacl.cli

import collection.JavaConverters._
import com.beust.jcommander.JCommander
import org.apache.commons.io.IOUtils
import org.apache.jena.rdf.model.ModelFactory
import org.slf4j.LoggerFactory
import uml2shacl.lib.XmlValidator
import uml2shacl.lib.model.XmlFileItem
import uml2shacl.lib.transformer.XmlToRdfTransformer

import java.io.{FileInputStream, FileNotFoundException, FileWriter}
import java.nio.file.{Files, Path, Paths}
import java.util.Collections
import java.util.stream.Collectors
import scala.xml.SAXException

object Cli {
  private val logger = LoggerFactory.getLogger(getClass)

  def main(argv: Array[String]): Unit = {
    val args = new CliArgs
    val argParser = JCommander.newBuilder().addObject(args).build()
    argParser.parse(argv: _*)
    if (args.inputFilePaths.isEmpty) {
      argParser.usage()
      return
    }

    val inputFilePaths = args.inputFilePaths.asScala.flatMap(inputFilePath => {
      val path = Paths.get(inputFilePath)
      if (Files.isRegularFile(path)) {
        List(path)
      } else if (Files.isDirectory(path)) {
        Files.list(path).collect(Collectors.toList[Path]).asScala.flatMap((filePath: Path) => {
          if (filePath.getFileName.toString.endsWith(".clsx")) {
            List(filePath)
          } else {
            List()
          }
        }).toList
      } else {
        throw new FileNotFoundException(s"${path} does not exist or is not a file or directory")
      }
    })

    val outputModel = ModelFactory.createDefaultModel()
    val transformer = new XmlToRdfTransformer
    val xmlValidator = new XmlValidator(Option(args.xsdFilePath).map(Paths.get(_)))
    inputFilePaths.zipWithIndex.foreach({ case (inputFilePath, inputFilePathIndex) => {
      val fileInputStream = new FileInputStream(inputFilePath.toFile)
      val fileContent = try {
        IOUtils.toByteArray(fileInputStream)
      } finally {
        fileInputStream.close()
      }

      if (args.validateOnly) {
        try {
          xmlValidator.validate(new String(fileContent, "UTF-8"))
        } catch {
          case e: SAXException => {
            logger.error(s"error in file ${inputFilePath}: ${e}")
          }
        }
      } else {
        val errors =
          transformer.transform(
            XmlFileItem(
              content = fileContent,
              contentType = None,
              name = inputFilePath.getFileName.toString
            ),
            outputModel
          ).errors
        errors.foreach(error => {
          logger.error(s"error in file ${error.fileName.get}: ${error.cause}")
        })
      }

      logger.info("processed {} / {} files", inputFilePathIndex + 1, inputFilePaths.length)
    }
    })

    if (args.outputFilePath != null && !args.outputFilePath.isBlank) {
      val outputFileWriter = new FileWriter(args.outputFilePath)
      try {
        outputModel.write(outputFileWriter, "TTL")
      } finally {
        outputFileWriter.close()
      }
    } else {
      outputModel.write(System.out, "TTL")
    }
  }
}