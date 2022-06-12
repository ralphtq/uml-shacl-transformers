package uml2shacl.app

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.configuration2.web.{ServletConfiguration, ServletContextConfiguration}
import org.apache.commons.io.IOUtils
import org.scalatra._
import org.scalatra.servlet.{FileUploadSupport, MultipartConfig}
import org.topbraidlive.app.framework.api.configuration._
import org.topbraidlive.client.java.api.`type`.ProjectGraphUri
import org.topbraidlive.client.java.{Client, ClientConfiguration, ClientCredentials}
import uml2shacl.lib.model.XmlFileItem

import java.util.Collections

/**
 * EDG application that exposes the Rhapsody XML importer. See the EDG-AF documentation for an overview of the app's API.
 *
 * @param edgClientCredentials optional EDG client credentials, only set for testing; the production system reads the credentials from the environment
 */
final class RhapsodyServlet(edgClientCredentials: Option[ClientCredentials] = None) extends ScalatraServlet with FileUploadSupport {
  configureMultipartHandling(MultipartConfig(maxFileSize = Some(10 * 1024 * 1024)))

  private val objectMapper = new ObjectMapper()

  /**
   * Get the app's configuration as a JSON object.
   */
  get("/configuration") {
    contentType = "application/json"

    // Use Jackson directly to write the Java class
    // Can use json4s or similar to write Scala case classes
    objectMapper.writeValueAsString(
      AppConfiguration.builder()
        .addImporter(
          ImporterConfiguration.builder()
            .setComment("Transform Rhapsody XML files (.clsx) to RDF and import the RDF into the collection")
            .setId("xml").setLabel("Rhapsody XML importer")
            .setProjectTypes(Collections.singletonList("http://teamwork.topbraidlive.org/datagraph/datagraphprojects#ProjectType"))
            .build()
        )
        .setId("rhapsody")
        .setLabel("Rhapsody")
        .build()
    )
  }

  /**
   * Get the HTML form for the importer, for EDG to display in an iframe when the importer is selected.
   *
   * The iframe src passes several variables to this endpoint. The relevant ones are embedded as hidden inputs in the form.
   * These are passed back to us on form submit, so the app can be stateless.
   *
   * See the EDG-AF documentation for more information.
   */
  get("/importer/xml") {
    contentType = "text/html"

    // Inline HTML template (https://scalatra.org/guides/2.7/views/inline-html.html)
    <html lang="en">
      <head>
        <meta charset="utf-8"/>
      </head>
      <body>
        <div style="clear: both; width: 500px">
          <form action="xml/action" enctype="multipart/form-data" method="post">
            <input id="projectGraph" name="projectGraph" type="hidden" value={params("projectGraph")}/>
            <input id="serverBaseUrl" name="serverBaseUrl" type="hidden" value={params("serverBaseUrl")}/>
            <label>
              <span style="display: inline-block; margin-bottom: 10px;">Attach one or more CLSX files, or a .zip file containing multiple files.</span>
              <input type="file" name="file" multiple="true" style="height: 40px; margin-bottom: 20px; width: 100%; clear: both"/>
            </label>
            <button type="submit">Start import</button>
          </form>
        </div>
      </body>
    </html>
  }

  /**
   * Create an EDG client instance, reading configuration from:
   * - System properties
   * - Environment variables
   * - Servlet context configuration (i.e., context.xml files on Tomcat)
   * - Servlet configuration (i.e., from web.xml)
   */
  private def newEdgClient(serverBaseUrl: String): Client = {
    val clientConfigurationBuilder = ClientConfiguration.builder()
    if (edgClientCredentials.isDefined) {
      clientConfigurationBuilder.setCredentials(edgClientCredentials.get)
    }
    clientConfigurationBuilder.setServerBaseUrl(serverBaseUrl)
    val clientConfiguration = clientConfigurationBuilder
      .setFromEnvironment()
      .setFromConfiguration(new ServletContextConfiguration(getServletContext).subset(ClientConfiguration.KEY_PREFIX))
      .setFromConfiguration(new ServletConfiguration(getServletConfig).subset(ClientConfiguration.KEY_PREFIX))
      .build()
    Client.create(clientConfiguration)
  }

  /**
   * Post inputs from the importer form served in the GET above and do the import.
   *
   */
  post("/importer/xml/action") {
    contentType = "text/html"

    val fileItems = fileMultiParams("file")
    val projectGraph = new ProjectGraphUri(params("projectGraph"))
    var serverBaseUrl = params("serverBaseUrl")
    // Hack to strip /tbl/ from ui:server()
    if (serverBaseUrl.endsWith("/tbl/"))
      serverBaseUrl = serverBaseUrl.substring(0, serverBaseUrl.length - "/tbl/".length)

    val importedTripleCount = new XmlImporter(newEdgClient(serverBaseUrl)).importFiles(
      // Convert servlet FileItems to the abstract XmlFileItem model
      files = fileItems.map(fileItem => {
        val inputStream = fileItem.getInputStream
        val content = try {
          IOUtils.toByteArray(fileItem.getInputStream)
        } finally {
          inputStream.close()
        }

        XmlFileItem(
          content = content,
          contentType = fileItem.contentType,
          name = fileItem.name
        )
      }).toList,
      projectGraphUri = projectGraph
    )

    // Return a simple message showing the results of the import.
    // The process doesn't take so long or create so many triple that we need to show progress indicators.
    <html lang="en">
      <head>
        <meta charset="utf-8"/>
      </head>
      <body>
        Imported
        {importedTripleCount}
        triples into
        {projectGraph}
        .
      </body>
    </html>

  }
}
