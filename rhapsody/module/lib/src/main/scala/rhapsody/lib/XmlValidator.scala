package rhapsody.lib

import java.io.{FileInputStream, StringReader}
import java.nio.file.Path
import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.{Schema, SchemaFactory}
import scala.xml.Elem

class XmlValidator(xsdFilePath: Option[Path] = None) {
  private val schema: Schema = readSchema()

  def validate(xml: Elem): Unit =
    validate(xml.toString())

  def validate(xml: String): Unit = {
    val validator = schema.newValidator()
    val xmlSource = new StreamSource(new StringReader(xml))
    validator.validate(xmlSource)
  }

  private def readSchema(): Schema = {
    val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    val schemaInputStream =
      if (xsdFilePath.isDefined) new FileInputStream(xsdFilePath.get.toFile())
      else getClass.getResourceAsStream("/xsd/Union.xsd")
    try {
      schemaFactory.newSchema(new StreamSource(schemaInputStream))
    } finally {
      schemaInputStream.close()
    }
  }
}
