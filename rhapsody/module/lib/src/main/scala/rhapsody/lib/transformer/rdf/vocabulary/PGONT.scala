package rhapsody.lib.transformer.rdf.vocabulary

import org.apache.jena.rdf.model.ResourceFactory

object PGONT {
  val NS = "http://ontologies.pg.com/pgontology/"
  val PREFIX = "pgont"

  // Properties
  //  val aggregate = ResourceFactory.createProperty(NS + "aggregate")
  val composition = ResourceFactory.createProperty(NS + "composition")
  val generalization = ResourceFactory.createProperty(NS + "generalization")
  val owner = ResourceFactory.createProperty(NS + "owner")
  val port = ResourceFactory.createProperty(NS + "port")
  val `GUID_d10728e0-b034-4b51-afad-c00fb9c8c328` = ResourceFactory.createProperty(NS + "GUID_d10728e0-b034-4b51-afad-c00fb9c8c328")
}
