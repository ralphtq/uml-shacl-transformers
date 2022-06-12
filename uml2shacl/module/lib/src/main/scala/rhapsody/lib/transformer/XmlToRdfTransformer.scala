package uml2shacl.lib.transformer
import org.apache.commons.io.IOUtils
import org.apache.jena.rdf.model.{Model, ModelFactory}

import org.slf4j.LoggerFactory
import uml2shacl.lib.XmlValidator
import uml2shacl.lib.model.{TopLevelModels, XmlFileItem}
import uml2shacl.lib.transformer.rdf._
import uml2shacl.lib.transformer.rdf.vocabulary.vocabulary.bindNamespaces
import uml2shacl.lib.transformer.xml._
import uml2shacl.lib.xsd._
import scalaxb.ParserFailure

import java.io.ByteArrayInputStream
import java.util.zip.{ZipEntry, ZipInputStream}
import scala.xml.{Elem, SAXException, XML}

/**
 * Singleton that composes other transformers in order to implement the end-to-end transformation process: XML->abstract model->RDF.
 */
class XmlToRdfTransformer {
  private val xmlValidator = new XmlValidator
  private val logger = LoggerFactory.getLogger(getClass)

  /**
   * Transform the given XML into RDF.
   *
   * @param inXml input XML
   * @return output RDF
   */
  final def transform(inXml: Elem): TransformResult =
    transform(inXml, ModelFactory.createDefaultModel())

  /**
   * Transform the given XML into RDF.
   *
   * @param inXml        input XML
   * @param outJenaModel output RDF
   */
  final def transform(inXml: Elem, outJenaModel: Model): TransformResult = {
    try {
      xmlValidator.validate(inXml)
    } catch {
      case e: SAXException => {
        return TransformResult(errors = List(XmlSchemaValidationError(e, fileName = None)), model = outJenaModel)
      }
    }

    val inModels: TopLevelModels = try {
      scalaxb.fromXML[XsdRhapsodyArchive](inXml)
    } catch {
      case e: ParserFailure => {
        return TransformResult(errors = List(XmlParseError(e, fileName = None)), model = outJenaModel)
      }
    }

    bindNamespaces(outJenaModel)
    outJenaModel.add(inModels)
    TransformResult(errors = List(), model = outJenaModel)
  }

  /**
   * Transform the given XML into RDF.
   *
   * @param inXmlFileItem XML file
   * @return output RDF
   */
  final def transform(inXmlFileItem: XmlFileItem): TransformResult =
    transform(inXmlFileItem, ModelFactory.createDefaultModel())

  /**
   * Transform the given XML into RDF.
   *
   * @param inXmlFileItem XML file
   * @param outJenaModel  output RDF
   */
  final def transform(inXmlFileItem: XmlFileItem, outJenaModel: Model): TransformResult =
    maybeTransformZipFile(inXmlFileItem, outJenaModel)

  private def maybeTransformZipFile(inXmlFileItem: XmlFileItem, outJenaModel: Model): TransformResult = {
    val zipInputStream = new ZipInputStream(new ByteArrayInputStream(inXmlFileItem.content))
    try {
      def transformNextZipEntry(zipEntry: ZipEntry): List[XmlToRdfTransformerError] = {
        if (zipEntry == null) {
          return List()
        }
        (try {
          transformTextFile(
            XmlFileItem(
              content = IOUtils.toByteArray(zipInputStream),
              contentType = None,
              name = zipEntry.getName
            ),
            outJenaModel
          )
        } finally {
          zipInputStream.closeEntry()
        }).errors ++ transformNextZipEntry(zipInputStream.getNextEntry)
      }

      val firstZipEntry = zipInputStream.getNextEntry
      if (firstZipEntry == null) {
        // The ZipInputStream has no way to check if the .zip file is valid
        // It will return null from the first call to .getNextEntry
        return transformTextFile(inXmlFileItem, outJenaModel)
      }

      TransformResult(errors = transformNextZipEntry(firstZipEntry), model = outJenaModel)
    } finally {
      zipInputStream.close()
    }
  }

  private def transformTextFile(inXmlFileItem: XmlFileItem, outJenaModel: Model): TransformResult = {
    logger.debug(s"processing {}", inXmlFileItem.name)

    val xml = try {
      XML.load(new ByteArrayInputStream(inXmlFileItem.content))
    } catch {
      case e: Exception => {
        return TransformResult(errors = List(XmlParseError(e, fileName = Some(inXmlFileItem.name))), model = outJenaModel)
      }
    }

    val result = transform(xml, outJenaModel)

    logger.info(s"processed {}", inXmlFileItem.name)

    // Add file name to errors
    TransformResult(
      errors = result.errors.map(error => error.copyAndSetFileName(fileName = inXmlFileItem.name)),
      model = result.model
    )
  }

  case class TransformResult(errors: List[XmlToRdfTransformerError], model: Model)
}
