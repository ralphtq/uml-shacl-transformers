package uml2shacl.lib.transformer

import org.apache.jena.rdf.model.{Model, Property, Resource, ResourceFactory}
import org.apache.jena.vocabulary.{RDF, RDFS, SKOS}
import uml2shacl.lib.model._
import uml2shacl.lib.transformer.rdf.vocabulary.{EDG, PGDATA, PGONT}

/**
 * Implicit class that wraps a Jena Model with various .add methods specific to the abstract models in .model.
 *
 * The .add methods implement the model->RDF transformation process.
 * Some of the .add transformations require chasing references to other models, so it's not always .add(ModelT).
 */
package object rdf {
  implicit class JenaModelWrapper(jenaModel: Model) {
    def add(topLevelModels: TopLevelModels): Unit = {
      topLevelModels.attributes.foreach(add(_, topLevelModels))
      topLevelModels.classes.foreach(add(_, topLevelModels))
      topLevelModels.dependencies.foreach(add(_))
      topLevelModels.generalizations.foreach(add(_, topLevelModels))
      topLevelModels.informationFlows.foreach(add(_, topLevelModels))
      topLevelModels.parts.foreach(add(_, topLevelModels))
      topLevelModels.ports.foreach(add(_, topLevelModels))
      topLevelModels.sysMlPorts.foreach(add(_, topLevelModels))
    }

    def add(attribute: IAttribute, topLevelModels: TopLevelModels): Unit = {
      val attributeResource = createDataResource(attribute.id)

      //      attribute.displayName.foreach(displayName => jenaModel.addLiteral(attributeResource, SKOS.prefLabel, ResourceFactory.createPlainLiteral(displayName)))
      attribute.name.foreach(name => jenaModel.addLiteral(attributeResource, RDFS.label, ResourceFactory.createPlainLiteral(name)))
      attribute.stereotypesHandles.foreach(handle => jenaModel.add(attributeResource, RDF.`type`, createOntologyResource(handle)))

      attribute.aggregatesList.foreach(attributeAggregateId => {
        topLevelModels.tagsById.get(attributeAggregateId).find(tag => tag.name.getOrElse("") == "OwnerGUID").foreach(tag => {
          tag.aggregatesList.foreach(tagAggregateId => {
            topLevelModels.literalSpecificationsById.get(tagAggregateId).headOption.foreach(literalSpecification => {
              jenaModel.add(attributeResource, PGONT.composition, createDataResource(literalSpecification.value))
            })
          })
        })
      })
    }

    def add(klass: IClass, topLevelModels: TopLevelModels): Unit = {
      val classResource = createDataResource(klass.id)

      klass.displayName.foreach(displayName => jenaModel.addLiteral(classResource, SKOS.prefLabel, ResourceFactory.createPlainLiteral(displayName)))
      klass.name.foreach(name => jenaModel.addLiteral(classResource, RDFS.label, ResourceFactory.createPlainLiteral(name)))
      klass.stereotypesHandles.foreach(handle => jenaModel.add(classResource, RDF.`type`, createOntologyResource(handle)))

      klass.aggregatesList.foreach(attributeAggregateId => {
        topLevelModels.tagsById.get(attributeAggregateId).find(tag => tag.name.getOrElse("") == "OwnerGUID").foreach(tag => {
          tag.aggregatesList.foreach(tagAggregateId => {
            topLevelModels.literalSpecificationsById.get(tagAggregateId).headOption.foreach(literalSpecification => {
              jenaModel.add(classResource, PGONT.owner, createDataResource(literalSpecification.value))
            })
          })
        })
      })
    }

    def add(dependency: IDependency): Unit = {
      val dependentResource = createDataResource(dependency.dependent)
      val dependsOnResource = createDataResource(dependency.dependsOn)

      dependency.stereotypesHandles.foreach(handle => jenaModel.add(dependentResource, createOntologyProperty(handle), dependsOnResource))
    }

    def add(generalization: IGeneralization, topLevelModels: TopLevelModels): Unit = {
      val baseEndLiteralSpecification: Option[ILiteralSpecification] =
        generalization.aggregatesList.flatMap(attributeAggregateId =>
          topLevelModels.tagsById.get(attributeAggregateId)
            .find(tag => tag.name.getOrElse("") == "BaseEnd")
            .flatMap(tag => tag.aggregatesList.flatMap(tagAggregateId =>
              topLevelModels.literalSpecificationsById.get(tagAggregateId)).headOption)).headOption

      val derivedEndLiteralSpecification: Option[ILiteralSpecification] =
        generalization.aggregatesList.flatMap(attributeAggregateId =>
          topLevelModels.tagsById.get(attributeAggregateId)
            .find(tag => tag.name.getOrElse("") == "DerivedEnd")
            .flatMap(tag => tag.aggregatesList.flatMap(tagAggregateId =>
              topLevelModels.literalSpecificationsById.get(tagAggregateId)).headOption)).headOption

      if (baseEndLiteralSpecification.isDefined && derivedEndLiteralSpecification.isDefined) {
        jenaModel.add(createDataResource(derivedEndLiteralSpecification.get.value), PGONT.generalization, createDataResource(baseEndLiteralSpecification.get.value))
      }
    }

    def add(part: IPart, topLevelModels: TopLevelModels): Unit = {
      //      val partResource = createDataResource(part.id)
      //
      //      part.displayName.foreach(displayName => jenaModel.addLiteral(partResource, SKOS.prefLabel, ResourceFactory.createPlainLiteral(displayName)))
      //      part.name.foreach(name => jenaModel.addLiteral(partResource, RDFS.label, ResourceFactory.createPlainLiteral(name)))
      //      part.stereotypesHandles.foreach(handle => jenaModel.add(partResource, RDF.`type`, defineOntologyResource(handle)))

      val relationPartLiteralSpecification: Option[ILiteralSpecification] =
        part.aggregatesList.flatMap(attributeAggregateId =>
          topLevelModels.tagsById.get(attributeAggregateId)
            .find(tag => tag.name.getOrElse("") == "RelationPart")
            .flatMap(tag => tag.aggregatesList.flatMap(tagAggregateId =>
              topLevelModels.literalSpecificationsById.get(tagAggregateId)).headOption)).headOption

      val relationWholeLiteralSpecification: Option[ILiteralSpecification] =
        part.aggregatesList.flatMap(attributeAggregateId =>
          topLevelModels.tagsById.get(attributeAggregateId)
            .find(tag => tag.name.getOrElse("") == "RelationWhole")
            .flatMap(tag => tag.aggregatesList.flatMap(tagAggregateId =>
              topLevelModels.literalSpecificationsById.get(tagAggregateId)).headOption)).headOption

      if (relationPartLiteralSpecification.isDefined && relationWholeLiteralSpecification.isDefined) {
        jenaModel.add(createDataResource(relationPartLiteralSpecification.get.value), PGONT.composition, createDataResource(relationWholeLiteralSpecification.get.value))
      }
    }

    def add(port: IPort, topLevelModels: TopLevelModels): Unit = {
      val portResource = createDataResource(port.id)
      port.name.foreach(name => jenaModel.addLiteral(portResource, RDFS.label, ResourceFactory.createPlainLiteral(name)))
      port.stereotypesHandles.foreach(handle => jenaModel.add(portResource, RDF.`type`, createOntologyResource(handle)))
      val parentClass = topLevelModels.classes.find(_.aggregatesList.find(_ == port.id).isDefined)
      if (parentClass.isDefined) {
        jenaModel.add(createDataResource(parentClass.get.id), PGONT.port, portResource)
      }
    }

    def add(sysMlPort: ISysMlPort, topLevelModels: TopLevelModels): Unit = {
      val sysMlPortResource = createDataResource(sysMlPort.id)
      sysMlPort.name.foreach(name => jenaModel.addLiteral(sysMlPortResource, RDFS.label, ResourceFactory.createPlainLiteral(name)))
      sysMlPort.stereotypesHandles.foreach(handle => jenaModel.add(sysMlPortResource, RDF.`type`, createOntologyResource(handle)))
      val parentClass = topLevelModels.classes.find(_.aggregatesList.find(_ == sysMlPort.id).isDefined)
      if (parentClass.isDefined) {
        jenaModel.add(createDataResource(parentClass.get.id), PGONT.port, sysMlPortResource)
      }
    }

    def add(informationFlow: IInformationFlow, topLevelModels: TopLevelModels): Unit = {
      val flowResource = ResourceFactory.createResource(PGDATA.NS + s"FLOW_${sanitizeGuid(informationFlow.end1.id)}_${sanitizeGuid(informationFlow.end2.id)}")
      jenaModel.add(flowResource, RDF.`type`, EDG.Flow)

      val (sourceEnd, targetEnd) = informationFlow.direction match {
        case "toEnd1" => (informationFlow.end2, informationFlow.end1)
        case "toEnd2" => (informationFlow.end1, informationFlow.end2)
      }
      val sourceResource = createDataResource(sourceEnd.id)
      val targetResource = createDataResource(targetEnd.id)

      def getEndLabel(end: IHandle): String = {
        // If the end IHandle doesn't have its own name, chase its id to a top-level model and get the name from that.
        end.name.getOrElse(topLevelModels.models.filter(_.id == end.id).flatMap(_.name).head)
      }

      jenaModel.add(flowResource, EDG.source, sourceResource)
      jenaModel.add(flowResource, EDG.target, targetResource)
      jenaModel.add(flowResource, RDFS.label, ResourceFactory.createPlainLiteral(s"From: ${getEndLabel(sourceEnd)} to ${getEndLabel(targetEnd)}"))
      informationFlow.conveyed.foreach(conveyed => jenaModel.add(flowResource, EDG.transfersAsset, createDataResource(conveyed.id)))
      jenaModel.add(sourceResource, PGONT.`GUID_d10728e0-b034-4b51-afad-c00fb9c8c328`, targetResource)

      informationFlow.stereotypesHandles.foreach(handle => {
        jenaModel.add(sourceResource, createOntologyProperty(handle.id), targetResource)
      })
    }

    private def createDataResource(handle: IHandle): Resource = {
      val handleResource = createDataResource(handle.id)
      handle.name.foreach(name => jenaModel.add(handleResource, RDFS.label, ResourceFactory.createPlainLiteral(name)))
      handleResource
    }

    private def createOntologyProperty(handle: IHandle): Property = {
      val handleProperty = createOntologyProperty(handle.id)
      handle.name.foreach(name => jenaModel.add(handleProperty, RDFS.label, ResourceFactory.createPlainLiteral(name)))
      handleProperty
    }

    private def createOntologyResource(handle: IHandle): Resource = {
      val handleResource = createOntologyResource(handle.id)
      handle.name.foreach(name => jenaModel.add(handleResource, RDFS.label, ResourceFactory.createPlainLiteral(name)))
      handleResource
    }

    private def createOntologyResource(guid: String): Resource = {
      val resource = jenaModel.createResource(PGONT.NS + sanitizeGuid(guid))
      resource.addLiteral(EDG.id, guid)
      resource
    }

    private def createDataResource(guid: String): Resource = {
      val resource = jenaModel.createResource(PGDATA.NS + sanitizeGuid(guid))
      resource.addLiteral(EDG.id, guid)
      resource
    }

    private def createOntologyProperty(guid: String): Property = {
      val property = jenaModel.createProperty(PGONT.NS + sanitizeGuid(guid))
      property.addLiteral(EDG.id, guid)
      property
    }
  }
}
