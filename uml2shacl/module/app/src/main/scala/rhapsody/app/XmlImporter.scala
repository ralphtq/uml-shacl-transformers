package uml2shacl.app

import org.apache.jena.rdf.model.ModelFactory
import org.topbraidlive.client.java.Client
import org.topbraidlive.client.java.api.`type`.ProjectGraphUri
import uml2shacl.lib.model.XmlFileItem
import uml2shacl.lib.transformer.XmlToRdfTransformer

/**
 * Implement the Rhapsody XML importer by composing the transformer (XmlToRdfTransformer) and the EDG client.
 *
 * Separate from the RhapsodyServlet for testability.
 */
final class XmlImporter(edgClient: Client) {
  /**
   * Import the given file items.
   *
   * @return the number of triples imported.
   */
  def importFiles(files: List[XmlFileItem], projectGraphUri: ProjectGraphUri): Int = {
    val outJenaModel = ModelFactory.createDefaultModel()
    val transformer = new XmlToRdfTransformer
    files.foreach(file => {
      transformer.transform(file, outJenaModel)
    })

    edgClient.clearProject(projectGraphUri)

    outJenaModel.clearNsPrefixMap()
    val tripleI = outJenaModel.getGraph.find()
    try {
      edgClient.addTriples(true, projectGraphUri, outJenaModel, tripleI)
    } finally {
      tripleI.close()
    }
    outJenaModel.size().toInt
  }
}
