package uml2shacl.lib.transformer

import org.slf4j.LoggerFactory
import uml2shacl.lib.model._
import uml2shacl.lib.xsd._

import scala.language.implicitConversions

/**
 * Package object with implicit function that implement the XML -> model transformation.
 *
 * Can't use type classes because the transformer is parameterized on both the input type and the output model type.
 *
 * The input is an instance of a case class generated from the XSD by scalaxb and populated by the scalaxb runtime XML parser.
 * The output is one of the abstract models in .model.
 */
package object xml {
  private val logger = LoggerFactory.getLogger(getClass)

  private val EmptyHandle = IHandle(id = "", m2Class = None, name = None)

  private def isValid(handle: IHandle) = !handle.id.isEmpty

  implicit def transformArchive(xml: XsdRhapsodyArchive): TopLevelModels =
    TopLevelModels(xml.RHAPSODYu45MODEL.xsdrhapsodyu45modeloption.flatMap(dataRecord => transformModelOption(dataRecord.value)).toList)

  implicit def transformAttribute(xml: XsdIAttribute): Option[IAttribute] =
    Option(
      xml.xsdiattributeoption.foldLeft(IAttribute(
        aggregatesList = List(),
        displayName = None,
        id = "",
        name = None,
        stereotypesHandles = List()
      ))((result, option) => option.value match {
        case aggregatesList: XsdAggregatesList => result.copy(aggregatesList = aggregatesList.value.toList)
        case displayName: Xsd_displayName => result.copy(displayName = Some(displayName.value))
        case name: Xsd_name2 => result.copy(name = Some(name.value))
        case id: Xsd_id => result.copy(id = id.value)
        case stereotypes: XsdStereotypes => result.copy(stereotypesHandles = stereotypes)
        case _ => {
          logger.debug("ignoring unknown attribute option {}", option.value)
          result
        }
      })).filter(!_.id.isEmpty).orElse({
      logger.warn("invalid attribute: {}", xml)
      None
    })

  implicit def transformClass(xml: XsdIClass): Option[IClass] =
    Option(
      xml.xsdiclassoption.foldLeft(IClass(
        aggregatesList = List(),
        displayName = None,
        id = "",
        name = None,
        stereotypesHandles = List()
      ))((result, option) => option.value match {
        case aggregatesList: XsdAggregatesList => result.copy(aggregatesList = aggregatesList.value.toList)
        case displayName: Xsd_displayName => result.copy(displayName = Some(displayName.value))
        case name: Xsd_name2 => result.copy(name = Some(name.value))
        case id: Xsd_id => result.copy(id = id.value)
        case stereotypes: XsdStereotypes => result.copy(stereotypesHandles = stereotypes)
        // Ignore
        case _ => {
          logger.debug("ignoring unknown class option {}", option.value)
          result
        }
      })).filter(!_.id.isEmpty).orElse({
      logger.warn("invalid class: {}", xml)
      None
    })

  implicit def transformDependency(xml: XsdIDependency): Option[IDependency] =
    Option(
      xml.xsdidependencyoption.foldLeft(IDependency(
        dependent = EmptyHandle,
        dependsOn = EmptyHandle,
        displayName = None,
        id = "",
        name = None,
        stereotypesHandles = List()
      ))((result, option) => option.value match {
        case dependent: Xsd_dependent => result.copy(dependent = transformInObjectHandle(dependent.INObjectHandle).getOrElse(result.dependent))
        case dependsOn: Xsd_dependsOn => result.copy(dependsOn = transformInObjectHandle(dependsOn.INObjectHandle).getOrElse(result.dependsOn))
        case displayName: Xsd_displayName => result.copy(displayName = Some(displayName.value))
        case name: Xsd_name2 => result.copy(name = Some(name.value))
        case id: Xsd_id => result.copy(id = id.value)
        case stereotypes: XsdStereotypes => result.copy(stereotypesHandles = stereotypes)
        case _ => {
          logger.debug("ignoring unknown dependency option {}", option.value)
          result
        }
      })).filter(result => !result.id.isEmpty && isValid(result.dependent) && isValid(result.dependsOn))
      .orElse({
        logger.warn("invalid dependency: {}", xml)
        None
      })

  implicit def transformGeneralization(xml: XsdIGeneralization): Option[IGeneralization] =
    Option(
      xml.xsdigeneralizationoption.foldLeft(IGeneralization(
        aggregatesList = List(),
        id = ""
      ))((result, option) => option.value match {
        case aggregatesList: XsdAggregatesList => result.copy(aggregatesList = aggregatesList.value.toList)
        case id: Xsd_id => result.copy(id = id.value)
        case _ => {
          logger.debug("ignoring unknown generalization option {}", option.value)
          result
        }
      })).filter(!_.id.isEmpty).orElse({
      logger.warn("invalid generalization: {}", xml)
      None
    })

  implicit def transformHandle(xml: XsdIHandle): Option[IHandle] =
    xml.xsdihandlesequence1.map(handleSequence =>
      handleSequence.xsdihandleoption.foldLeft(IHandle(
        id = "",
        m2Class = Some(handleSequence._hm2Class.value),
        name = None
      ))((result, option) => option.value match {
        case id: Xsd_hid => result.copy(id = id.value)
        case name: Xsd_hname => result.copy(name = Some(name.value))
        // Ignore
        case _ => {
          logger.debug("ignoring unknown handle option {}", option.value)
          result
        }
      })).filter(isValid(_)).orElse({
      logger.warn("invalid handle: {}", xml)
      None
    })

  implicit def transformInformationFlow(xml: XsdIInformationFlow): Option[IInformationFlow] =
    Option(
      xml.xsdiinformationflowoption.foldLeft(IInformationFlow(
        conveyed = None,
        direction = "",
        end1 = EmptyHandle,
        end2 = EmptyHandle,
        id = "",
        stereotypesHandles = List()
      ))((result, option) => option.value match {
        case conveyed: XsdConveyed => result.copy(conveyed = transformRPYContainer(conveyed.IRPYRawContainer).headOption)
        case direction: XsdDirectionu93 => result.copy(direction = direction.value)
        case end1: XsdEnd1u93 => result.copy(end1 = transformHandle(end1.IHandle).getOrElse(result.end1))
        case end2: XsdEnd2u93 => result.copy(end2 = transformHandle(end2.IHandle).getOrElse(result.end2))
        case id: Xsd_id => result.copy(id = id.value)
        case stereotypes: XsdStereotypes => result.copy(stereotypesHandles = stereotypes)
        case _ => {
          logger.debug("ignoring unknown information flow option {}", option.value)
          result
        }
      })).filter(result => {
      !result.direction.isEmpty && !result.id.isEmpty && isValid(result.end1) && isValid(result.end2)
    })
      .orElse({
        logger.warn("invalid information flow: {}", xml)
        None
      })

  implicit def transformInObjectHandle(xml: XsdINObjectHandle): Option[IHandle] =
    xml.xsdinobjecthandlesequence1.flatMap(handleSequence => handleSequence.xsdinobjecthandleoption.value match {
      case id: Xsd_hid => Some(IHandle(id = id.value, m2Class = Some(handleSequence._hm2Class.value), name = None))
      case nestedSequence: XsdINObjectHandleSequence2 =>
        Some(IHandle(id = nestedSequence._hid.value, m2Class = Some(handleSequence._hm2Class.value), name = None))
      case other => {
        logger.debug("ignoring unknown INObjectHandle option {}", other)
        None
      }
    }).orElse({
      logger.warn("invalid INObjectHandle: {}", xml)
      None
    })

  implicit def transformLiteralSpecification(xml: XsdILiteralSpecification): Option[ILiteralSpecification] =
    Option(
      xml.xsdiliteralspecificationoption.foldLeft(ILiteralSpecification(
        id = "",
        value = ""
      ))((result, option) => option.value match {
        case id: Xsd_id => result.copy(id = id.value)
        case value: Xsd_value2 => transformStringDataRecordSeq(value.mixed).map(value => result.copy(value = value)).getOrElse(result)
        // Ignore
        case _ => {
          logger.debug("ignoring unknown literal specification option {}", option.value)
          result
        }
      })).filter(result => !result.id.isEmpty && !result.value.isEmpty)
      .orElse({
        logger.warn("invalid literal specification: {}", xml)
        None
      })

  implicit def transformModelOption(xml: XsdRHAPSODYu45MODELOption): Option[TopLevelModel] =
    xml match {
      // <IAttribute>
      case attribute: XsdIAttribute => attribute
      // <IClass>
      case klass: XsdIClass => klass // Invokes the implicit conversion
      // <IGeneralization>
      case generalization: XsdIGeneralization => generalization
      // <IDependency>
      case dependency: XsdIDependency => dependency
      // <IInformationFlow>
      case informationFlow: XsdIInformationFlow => informationFlow
      // <ILiteralSpecification>
      case literalSpecification: XsdILiteralSpecification => literalSpecification
      // <IPart>
      case part: XsdIPart => part
      // <IPort>
      case port: XsdIPort => port
      // <ISysMLPort>
      case sysMlPort: XsdISysMLPort => sysMlPort
      // <ITag>
      case tag: XsdITag => tag
      // Ignore
      case _ => {
        logger.debug("ignoring unknown top-level model: {}", xml)
        None
      }
    }

  implicit def transformPart(xml: XsdIPart): Option[IPart] =
    Option(
      xml.xsdipartoption.foldLeft(IPart(
        aggregatesList = List(),
        id = "",
        name = None,
        stereotypesHandles = List()
      ))((result, option) => option.value match {
        case aggregatesList: XsdAggregatesList => result.copy(aggregatesList = aggregatesList.value.toList)
        case id: Xsd_id => result.copy(id = id.value)
        case name: Xsd_name2 => result.copy(name = Some(name.value))
        case stereotypes: XsdStereotypes => result.copy(stereotypesHandles = stereotypes)
        case _ => {
          logger.debug("ignoring unknown part option {}", option.value)
          result
        }
      })).filter(!_.id.isEmpty)
      .orElse({
        logger.warn("invalid part: {}", xml)
        None
      })

  implicit def transformPort(xml: XsdIPort): Option[IPort] =
    Option(
      xml.xsdiportoption.foldLeft(IPort(
        id = "",
        name = None,
        stereotypesHandles = List()
      ))((result, option) => option.value match {
        case id: Xsd_id => result.copy(id = id.value)
        case name: Xsd_name2 => result.copy(name = Some(name.value))
        case stereotypes: XsdStereotypes => result.copy(stereotypesHandles = stereotypes)
        case _ => {
          logger.debug("ignoring unknown port option {}", option.value)
          result
        }
      })).filter(!_.id.isEmpty)
      .orElse({
        logger.warn("invalid port: {}", xml)
        None
      })

  implicit def transformRPYContainer(xml: XsdIRPYRawContainer): List[IHandle] = {
    val list = xml.xsdirpyrawcontaineroption.flatMap(option => option.value match {
      case handle: XsdIHandle => transformHandle(handle)
      case _ => {
        logger.debug("ignoring unknown container option {}", option.value)
        None
      }
    }).toList
    if (list.isEmpty) {
      logger.warn("empty container: {}", xml)
    }
    list
  }

  implicit def transformStereotypes(xml: XsdStereotypes): List[IHandle] =
    transformRPYContainer(xml.IRPYRawContainer)

  private def transformStringDataRecordSeq(stringDataRecordSeq: Seq[scalaxb.DataRecord[Any]]): Option[String] =
    stringDataRecordSeq.headOption.filter(_.value.isInstanceOf[String]).map(_.value.asInstanceOf[String])

  implicit def transformSysMlPort(xml: XsdISysMLPort): Option[ISysMlPort] =
    Option(
      xml.xsdisysmlportoption.foldLeft(ISysMlPort(
        id = "",
        name = None,
        stereotypesHandles = List()
      ))((result, option) => option.value match {
        case id: Xsd_id => result.copy(id = id.value)
        case name: Xsd_name2 => result.copy(name = Some(name.value))
        case stereotypes: XsdStereotypes => result.copy(stereotypesHandles = stereotypes)
        case _ => {
          logger.debug("ignoring unknown SysML port option {}", option.value)
          result
        }
      })).filter(!_.id.isEmpty)
      .orElse({
        logger.warn("invalid SysML port: {}", xml)
        None
      })

  implicit def transformTag(xml: XsdITag): Option[ITag] =
    Option(
      xml.xsditagoption.foldLeft(ITag(
        aggregatesList = List(),
        id = "",
        name = None
      ))((result, option) => option.value match {
        case aggregatesList: XsdAggregatesList => result.copy(aggregatesList = aggregatesList.value.toList)
        case id: Xsd_id => result.copy(id = id.value)
        case name: Xsd_name2 => result.copy(name = Some(name.value))
        // Ignore
        case _ => {
          logger.debug("ignoring unknown tag option {}", option.value)
          result
        }
      })).filter(!_.id.isEmpty)
      .orElse({
        logger.warn("invalid tag: {}", xml)
        None
      })
}
