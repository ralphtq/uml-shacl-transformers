package rhapsody.lib.model

/**
 * Abstraction of the servlet FileItem, passed to a transformer. It has fewer fields and no dependencies. This is useful to synthesize instances of FileItem's in the transformation process.
 */
case class XmlFileItem(content: Array[Byte], contentType: Option[String], name: String)
