package uml2shacl.lib

import org.apache.commons.io.IOUtils
import org.apache.jena.rdf.model.{Model, ModelFactory, ResourceFactory}
import org.apache.jena.vocabulary.RDFS
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import uml2shacl.lib.model.XmlFileItem
import uml2shacl.lib.transformer.XmlToRdfTransformer
import uml2shacl.lib.transformer.rdf.vocabulary.vocabulary.bindNamespaces

import collection.JavaConverters._
import java.io.FileInputStream
import java.nio.file.Paths

class XmlToRdfTransformerTest extends AnyFlatSpec with should.Matchers {
  private val expectedUnionModel = loadTtlResource("DATA_CSM_Instances_v2_union")
  expectedUnionModel.removeAll(ResourceFactory.createResource("http://data.pg.com/data/graph/DATA_CSM_Instances_v2_union"), null, null)
  // Tim's SPARQL queries accidentally pick up IClass's that are nested inside an _implicitClass
  List("Interface", "da_3_implicitType", "da_5_implicitType", "da_7_implicitType", "ia_1_implicitType", "ia_3_implicitType", "ia_5_implicitType", "ia_7_implicitType").foreach(implicitClassName =>
    expectedUnionModel.listSubjectsWithProperty(RDFS.label, ResourceFactory.createPlainLiteral(implicitClassName)).toList.asScala.toList.map(implicitClassResource => {
      expectedUnionModel.removeAll(implicitClassResource, null, null)
    }))
  private val sut = new XmlToRdfTransformer

  it should "handle syntactically malformed XML correctly" in {
    sut.transform(XmlFileItem(
      content = "some garbage".getBytes,
      contentType = None,
      name = "garbage.clsx"
    )).errors should not be empty
  }

  it should "handle semantically malformed XML correctly" in {
    sut.transform(XmlFileItem(
      content =
        """<?xml version="1.0" encoding="UTF-8"?>
<RhapsodyArchive>
<MagicNumber></MagicNumber>
<CODE-PAGE>1252</CODE-PAGE>
<version>109.0</version>
<lang>C++</lang>
<BuildNo>202002261110</BuildNo>
<RMMMinimumClientVersion>100.0</RMMMinimumClientVersion>
</RhapsodyArchive>
""".getBytes,
      contentType = None,
      name = "garbage.clsx"
    )).errors should not be empty
  }

  //  it should "transform all of the individual CLSX files" in {
  //    val actualUnionModel = ModelFactory.createDefaultModel()
  //    bindNamespaces(actualUnionModel)
  //
  //    // Progressively add files to the union model
  //    def transformClsxFile(fileBaseName: String) = {
  //      sut.transform(getClsxResourceFileItem(fileBaseName + ".clsx"), actualUnionModel)
  //      assertContainsAll(actualUnionModel, expectedUnionModel)
  //      //      actualUnionModel.write(System.out, "TTL")
  //    }
  //
  //    transformClsxFile("Logical_System_1_1")
  //  }

  it should "transform the Union CLSX file" in {
    val actualUnionModel = sut.transform(getClsxResourceFileItem("Union.clsx")).model
    // As of 20210616 the generated instance data has edg:id triples that aren't in Tim's one-off SPARQL-created instance data
    assertContainsAll(actualUnionModel, expectedUnionModel)
    actualUnionModel.write(System.out, "TTL")
  }

  it should "transform the .zip file with all of the individual .clsx's" in {
    val actualUnionModel = sut.transform(getClsxResourceFileItem("clsx.zip")).model
    // As of 20210616 the generated instance data has edg:id triples that aren't in Tim's one-off SPARQL-created instance data
    assertContainsAll(actualUnionModel, expectedUnionModel)
    actualUnionModel.write(System.out, "TTL")
  }

  private def assertContainsAll(superModel: Model, subModel: Model): Unit = {
    if (superModel.containsAll(subModel)) {
      if (superModel.size > subModel.size) {
        println(s"Super model (${superModel.size}) contains ${superModel.size - subModel.size} more statements than the sub model (${subModel.size})")
      }
      return
    }

    println("Super model:")
    superModel.write(System.out, "TTL")
    println()

    subModel.isEmpty should be(false)

    println("Sub model:")
    subModel.write(System.out, "TTL")
    println()

    println("Difference model:")
    val differenceModel = subModel.difference(superModel)
    bindNamespaces(differenceModel)
    differenceModel.write(System.out, "TTL")
    println()
    fail()
  }

  private def assertEquals(expectedModel: Model, actualModel: Model): Unit = {
    val differenceModel = expectedModel.difference(actualModel)
    if (differenceModel.isEmpty) {
      return
    }

    println(s"Difference model (${differenceModel.size} statements)")
    bindNamespaces(differenceModel)
    differenceModel.write(System.out, "TTL")
    println()

    //    println("Expected model:")
    //    expectedModel.write(System.out, "TTL")
    //    println()

    actualModel.isEmpty should be(false)

    println("Actual model:")
    actualModel.write(System.out, "TTL")
    println()

    fail()
  }

  private def getClsxResourceFileItem(fileName: String, contentType: Option[String] = None): XmlFileItem = {
    val fileUri = getClass.getResource(s"/clsx/${fileName}")
    val filePath = Paths.get(fileUri.getPath)
    val fileInputStream = new FileInputStream(filePath.toFile)
    XmlFileItem(
      content = try {
        IOUtils.toByteArray(fileInputStream)
      } finally {
        fileInputStream.close()
      },
      contentType = contentType,
      name = fileName
    )
  }

  private def loadTtlResource(fileBaseName: String): Model = {
    val ttlInputStream = getClass.getResourceAsStream(s"/ttl/${fileBaseName}.ttl")
    try {
      val model = ModelFactory.createDefaultModel()
      bindNamespaces(model)
      model.read(ttlInputStream, "http://example.com", "TTL")
    } finally {
      ttlInputStream.close()
    }
  }
}
