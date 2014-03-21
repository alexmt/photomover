package plus2flickr.cli.web

import org.kohsuke.args4j.Option
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import com.google.inject.servlet.GuiceFilter
import javax.servlet.DispatcherType
import java.util.EnumSet
import plus2flickr.guice.ServicesModule
import plus2flickr.guice.DbModule
import plus2flickr.thirdparty.flickr.FlickrAppSettings
import java.io.IOException
import java.util.Properties
import java.io.StringReader
import plus2flickr.thirdparty.google.GoogleAppSettings
import com.google.inject.servlet.GuiceServletContextListener
import com.google.inject.Injector
import com.google.inject.Guice
import plus2flickr.couchdb.CouchDbManager
import org.codehaus.jackson.map.ObjectMapper
import plus2flickr.guice.WebServicesModule

data class StartWebOptions(
    Option("-p") var port: Int = 8080,
    Option("-couchDb") var couchDb: String = "http://localhost:5984",
    Option("-flickrAppSettings") var flickrAppSettings: String? = null,
    Option("-googleAppSettings") var googleAppSettings: String? = null) {

  private fun readResource(path: String): String {
    val resource = javaClass<StartWebOptions>().getResource(path)
    if (resource == null) {
      throw IOException("Resource $path not found")
    }
    return resource.readText()
  }

  fun getFlickrAppSettings(): FlickrAppSettings {
    val properties = Properties()
    properties.load(StringReader(flickrAppSettings ?: readResource("/flickr_app.properties")))
    return FlickrAppSettings(
        apiKey = properties.get("apiKey").toString(), apiSecret = properties.get("apiSecret").toString())
  }

  fun getGoogleAppSettings(): GoogleAppSettings {
    val web = ObjectMapper().readTree(googleAppSettings ?: readResource("/client_secret.json"))!!.get("web")!!
    return GoogleAppSettings(
        clientId =  web.get("client_id")!!.asText()!!,
        clientSecret = web.get("client_secret")!!.asText()!!,
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
  val context = ServletContextHandler()
  context.setContextPath("/services")
  context.addEventListener(object : GuiceServletContextListener() {
    override fun getInjector(): Injector? {
      val injector = Guice.createInjector(
          WebServicesModule(),
          DbModule(url = options.couchDb),
          ServicesModule(options.getGoogleAppSettings(), options.getFlickrAppSettings()))
      injector!!.getInstance(javaClass<CouchDbManager>())!!.ensureDbExists()
      return injector
    }
  })
  context.addFilter(javaClass<GuiceFilter>(), "/*", EnumSet.of(DispatcherType.REQUEST))

  val server = Server(options.port)
  server.setHandler(context)
  server.start()
  server.join()
}