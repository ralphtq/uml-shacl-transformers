package uml2shacl.lib.transformer

sealed trait XmlToRdfTransformerError {
  val cause: Exception
  val fileName: Option[String]

  def copyAndSetFileName(fileName: String): XmlToRdfTransformerError
}

final case class XmlParseError(cause: Exception, fileName: Option[String]) extends XmlToRdfTransformerError {
  final override def copyAndSetFileName(fileName: String) = copy(fileName = Some(fileName))
}

final case class XmlSchemaValidationError(cause: Exception, fileName: Option[String]) extends XmlToRdfTransformerError {
  final override def copyAndSetFileName(fileName: String) = copy(fileName = Some(fileName))
}
