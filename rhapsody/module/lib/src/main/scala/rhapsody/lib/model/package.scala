package rhapsody.lib

/**
 * Models for an abstract syntax tree of the XML, produced by the transform.xml process.
 */
package object model {
  sealed trait TopLevelModel {
    val id: String
    val name: Option[String]
  }

  case class IAttribute(
                         aggregatesList: List[String],
                         displayName: Option[String],
                         id: String,
                         name: Option[String],
                         stereotypesHandles: List[IHandle]
                       ) extends TopLevelModel

  case class IClass(
                     aggregatesList: List[String],
                     displayName: Option[String],
                     id: String,
                     name: Option[String],
                     stereotypesHandles: List[IHandle]
                   ) extends TopLevelModel

  case class IDependency(
                          dependent: IHandle,
                          dependsOn: IHandle,
                          displayName: Option[String],
                          id: String,
                          name: Option[String],
                          stereotypesHandles: List[IHandle]
                        ) extends TopLevelModel

  case class IGeneralization(
                              aggregatesList: List[String],
                              id: String
                            ) extends TopLevelModel {
    val name = None
  }

  case class IHandle(
                      id: String,
                      m2Class: Option[String],
                      name: Option[String]
                    )

  case class IInformationFlow(
                               conveyed: Option[IHandle],
                               direction: String,
                               end1: IHandle,
                               end2: IHandle,
                               id: String,
                               stereotypesHandles: List[IHandle]
                             ) extends TopLevelModel {
    val name = None
  }

  case class ILiteralSpecification(id: String, value: String) extends TopLevelModel {
    val name = None
  }

  case class IPart(
                    aggregatesList: List[String],
                    id: String,
                    name: Option[String],
                    stereotypesHandles: List[IHandle]
                  ) extends TopLevelModel

  case class IPort(
                    id: String,
                    name: Option[String],
                    stereotypesHandles: List[IHandle]
                  ) extends TopLevelModel

  case class ISysMlPort(
                         id: String,
                         name: Option[String],
                         stereotypesHandles: List[IHandle]
                       ) extends TopLevelModel

  case class ITag(aggregatesList: List[String], id: String, name: Option[String]) extends TopLevelModel

  case class TopLevelModels(models: List[TopLevelModel]) {
    def attributes: List[IAttribute] = models.filter(_.isInstanceOf[IAttribute]).map(_.asInstanceOf[IAttribute])

    def classes: List[IClass] = models.filter(_.isInstanceOf[IClass]).map(_.asInstanceOf[IClass])

    def dependencies: List[IDependency] = models.filter(_.isInstanceOf[IDependency]).map(_.asInstanceOf[IDependency])

    def generalizations: List[IGeneralization] = models.filter(_.isInstanceOf[IGeneralization]).map(_.asInstanceOf[IGeneralization])

    def informationFlows: List[IInformationFlow] = models.filter(_.isInstanceOf[IInformationFlow]).map(_.asInstanceOf[IInformationFlow])

    def literalSpecificationsById: Map[String, ILiteralSpecification] = literalSpecifications.map(literalSpecification => (literalSpecification.id, literalSpecification)).toMap

    def literalSpecifications: List[ILiteralSpecification] = models.filter(_.isInstanceOf[ILiteralSpecification]).map(_.asInstanceOf[ILiteralSpecification])

    def parts: List[IPart] = models.filter(_.isInstanceOf[IPart]).map(_.asInstanceOf[IPart])

    def ports: List[IPort] = models.filter(_.isInstanceOf[IPort]).map(_.asInstanceOf[IPort])

    def sysMlPorts: List[ISysMlPort] = models.filter(_.isInstanceOf[ISysMlPort]).map(_.asInstanceOf[ISysMlPort])

    def tagsById: Map[String, ITag] = tags.map(tag => (tag.id, tag)).toMap

    def tags: List[ITag] = models.filter(_.isInstanceOf[ITag]).map(_.asInstanceOf[ITag])
  }
}

