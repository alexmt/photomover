package photomover.cli.web

import org.kohsuke.args4j.Option
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import com.google.inject.servlet.GuiceFilter
import javax.servlet.DispatcherType
import java.util.EnumSet
import photomover.guice.AppModule
import photomover.guice.DbModule
import photomover.thirdparty.flickr.FlickrAppSettings
import java.io.IOException
import java.util.Properties
import java.io.StringReader
import photomover.thirdparty.google.GoogleAppSettings
import com.google.inject.servlet.GuiceServletContextListener
import com.google.inject.Injector
import com.google.inject.Guice
import photomover.couchdb.CouchDbManager
import org.codehaus.jackson.map.ObjectMapper
import photomover.guice.WebServicesModule
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.server.handler.ContextHandler
import org.eclipse.jetty.server.session.HashSessionIdManager
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.server.session.HashSessionManager
import photomover.AppPresentationSettings

data class StartWebOptions(
  port: Int = 8080,
  couchDb: String = "http://localhost:5984",
  flickrAppSettings: String? = null,
  googleAppSettings: String? = null,
  staticContentPath: String = "src/web/app") {

  var port: Int = port
    [Option("-p")] set
  var couchDb: String = couchDb
    [Option("-couchDb")] set
  var flickrAppSettings: String? = flickrAppSettings
    [Option("-flickrAppSettings")] set
  var googleAppSettings: String? = googleAppSettings
    [Option("-googleAppSettings")] set
  var staticContentPath: String = staticContentPath
    [Option("-staticContentPath")] set

  private fun readResource(path: String): String {
    val resource = javaClass<StartWebOptions>().getResource(path)
    if (resource == null) {
      throw IOException("Resource $path not found")
    }
    return resource.readText()
  }

  fun getFlickrAppSettings(): FlickrAppSettings {
    val settings = flickrAppSettings ?: readResource("/flickr_app.json")
    return ObjectMapper().readValue(settings.toByteArray(), javaClass<FlickrAppSettings>())
  }

  fun getGoogleAppSettings(): GoogleAppSettings {
    val web = ObjectMapper().readTree(googleAppSettings ?: readResource("/client_secret.json"))!!.get("web")
    return GoogleAppSettings(
      clientId = web.get("client_id").asText(),
      clientSecret = web.get("client_secret").asText(),
      applicationName = "Plus2Flickr",
      scopes = listOf(
        "https://www.googleapis.com/auth/plus.login",
        "https://www.googleapis.com/auth/userinfo.email",
        "https://www.googleapis.com/auth/userinfo.profile",
        "https://picasaweb.google.com/data/")
    )
  }
}

fun start(options: StartWebOptions) {
  val googleAppSettings = options.getGoogleAppSettings()
  val flickrAppSettings = options.getFlickrAppSettings()
  val presentationSettings = AppPresentationSettings(photosPerPage = 20, maxPagesCount = 5)
  val injector = Guice.createInjector(
    WebServicesModule(),
    DbModule(url = options.couchDb),
    AppModule(googleAppSettings, flickrAppSettings, presentationSettings))
  injector!!.getInstance(javaClass<CouchDbManager>())!!.ensureDbExists()

  val servicesContext = ServletContextHandler()
  servicesContext.setContextPath("/services")
  servicesContext.addEventListener(object : GuiceServletContextListener() {
    override fun getInjector(): Injector? {
      return injector
    }
  })
  servicesContext.addFilter(javaClass<GuiceFilter>(), "/*", EnumSet.of(DispatcherType.REQUEST))
  servicesContext.setSessionHandler(SessionHandler(HashSessionManager()))

  // TODO(amatyushentsev): stop serving static content in app server
  val resourcesHandler = ResourceHandler()
  resourcesHandler.setResourceBase(options.staticContentPath)
  val staticContext = ContextHandler("/")
  staticContext.setHandler(resourcesHandler)

  var handlers = HandlerList()
  handlers.setHandlers(array(servicesContext, staticContext))

  val server = Server(options.port)
  server.setSessionIdManager(HashSessionIdManager())
  server.setHandler(handlers)
  server.start()
  server.join()
}
