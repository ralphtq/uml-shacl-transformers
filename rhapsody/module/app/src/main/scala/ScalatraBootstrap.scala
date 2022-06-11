import org.scalatra._
import rhapsody.app.RhapsodyServlet

import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new RhapsodyServlet, "/rhapsody/*")
  }
}
