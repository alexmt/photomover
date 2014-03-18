package plus2flickr.cli.web

import org.kohsuke.args4j.Option
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import plus2flickr.guice.GuiceContext
import com.google.inject.servlet.GuiceFilter
import javax.servlet.DispatcherType
import org.eclipse.jetty.servlet.ServletHandler
import java.util.EnumSet
import java.util.logging.Logger

data class StartWebOptions(Option("-p") var port: Int = 8080 )

fun start(options: StartWebOptions) {
  val context = ServletContextHandler()
  context.setContextPath("/services")
  context.addEventListener(GuiceContext())
  context.addFilter(javaClass<GuiceFilter>(), "/*", EnumSet.of(DispatcherType.REQUEST))

  val server = Server(options.port)
  server.setHandler(context)
  server.start()
  server.join()
}