package rhapsody.app

import org.scalatra.test.scalatest._
import org.topbraidlive.client.java.UsernamePasswordClientCredentials

import java.io.File

class RhapsodyServletSpec extends ScalatraFlatSpec {
  addServlet(new RhapsodyServlet(
    edgClientCredentials = Some(UsernamePasswordClientCredentials.builder().setPassword("password32").setUsername("Admin_user").build())),
    "/rhapsody/*"
  )

  private val projectGraph = "urn:x-evn-master:rhapsody"
  private val serverBaseUrl = "http://localhost:8080"

  it should "return its app configuration" in {
    get("/rhapsody/configuration") {
      status should equal(200)
      body should include("\"label\":\"Rhapsody XML importer\"")
    }
  }

  it should "get the XML importer's page" in {
    get("/rhapsody/importer/xml", ("projectGraph" -> projectGraph), ("serverBaseUrl" -> serverBaseUrl)) {
      status should equal(200)
      response.header("Content-Type") should be("text/html;charset=utf-8")
      body should include(projectGraph)
      body should include(serverBaseUrl)
    }
  }

  if (!sys.env.get("CI").isDefined) {
    it should "post a .zip to the XML importer" in {
      val zipFilePath: Any = new File(getClass.getResource("/clsx/clsx.zip").toURI)
      post("/rhapsody/importer/xml/action", files = List("file" -> zipFilePath), params = List(("projectGraph" -> projectGraph), ("serverBaseUrl" -> serverBaseUrl))) {
        body should include("triples")
        status should equal(200)
        response.header("Content-Type") should be("text/html;charset=utf-8")
      }
    }
  }
}
