package rhapsody.lib.transformer.rdf.vocabulary

import org.apache.jena.rdf.model.ResourceFactory
import rhapsody.lib.transformer.sanitizeGuid

object EDG {
  val NS = "http://edg.topbraid.solutions/model/"
  val PREFIX = "edg"

  // Properties
  val id = ResourceFactory.createProperty(NS + "id")
  val source = ResourceFactory.createProperty(NS + "source")
  val target = ResourceFactory.createProperty(NS + "target")
  val transfersAsset = ResourceFactory.createProperty(NS + "transfersAsset")

  // Resources
  val Flow = ResourceFactory.createResource(NS + "Flow")
}
