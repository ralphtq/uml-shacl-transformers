package uml2shacl.lib.transformer.rdf.vocabulary

import org.apache.jena.rdf.model.Model
import org.apache.jena.vocabulary.{RDF, RDFS, SKOS}

/**
 * RDF vocabulary singletons in the Jena style, one singleton per namespace.
 */
package object vocabulary {
  def bindNamespaces(model: Model): Unit = {
    model.setNsPrefix(EDG.PREFIX, EDG.NS)
    model.setNsPrefix(PGDATA.PREFIX, PGDATA.NS)
    model.setNsPrefix(PGONT.PREFIX, PGONT.NS)
    model.setNsPrefix("rdf", RDF.getURI)
    model.setNsPrefix("rdfs", RDFS.getURI)
    model.setNsPrefix("skos", SKOS.getURI)
  }
}
