package plus2flickr.web.guice

import com.google.inject.servlet.GuiceServletContextListener
import com.google.inject.Injector
import com.google.inject.Guice
import com.sun.jersey.guice.JerseyServletModule
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer
import java.util.HashMap
import com.google.inject.AbstractModule
import org.ektorp.CouchDbInstance
import org.ektorp.http.StdHttpClient
import com.google.inject.Provides
import org.ektorp.impl.StdCouchDbInstance
import org.ektorp.DbPath
import plus2flickr.web.resources.UserResource
import com.google.inject.servlet.RequestScoped
import com.google.inject.Inject
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import com.google.inject.Provider
import plus2flickr.domain.User
import plus2flickr.services.UserService
import javax.servlet.http.Cookie
import org.ektorp.CouchDbConnector
import org.ektorp.impl.StdCouchDbConnector
import plus2flickr.web.findByCookieName
import plus2flickr.web.CurrentUserProvider
import com.google.api.client.http.HttpTransport
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import plus2flickr.thirdparty.google.GoogleAppSettings
import plus2flickr.thirdparty.CloudService
import plus2flickr.thirdparty.google.GoogleService
import org.codehaus.jackson.map.ObjectMapper
import java.io.IOException
import com.google.inject.Singleton

class DbModule(
    val dbName: String = "plus2flickr",
    val host: String = "localhost",
    val port: Int = 5984) : AbstractModule() {

  override fun configure() {
  }

  protected fun validateDb(dbInstance: StdCouchDbInstance) {
    if (!dbInstance.checkIfDbExists(DbPath(dbName))) {
      dbInstance.createDatabase(dbName)
    }
  }

  Provides fun provideDbInstance(): CouchDbInstance {
    val httpClient = StdHttpClient.Builder().host(host)?.port(port)?.build()
    val dbInstance = StdCouchDbInstance(httpClient)
    validateDb(dbInstance)
    return dbInstance
  }

  Provides fun provideDbConnector(dbInstance: CouchDbInstance): CouchDbConnector {
    return StdCouchDbConnector(dbName, dbInstance)
  }
}

class GuiceContext : GuiceServletContextListener() {
  override fun getInjector(): Injector? {
    return Guice.createInjector(object : JerseyServletModule() {

      override fun configureServlets() {
        bind(javaClass<UserResource>())
        val hashMap: HashMap<String, String> = hashMapOf(
            "com.sun.jersey.api.json.POJOMappingFeature" to "true"
        )
        serve("/*")!!.with(javaClass<GuiceContainer>(), hashMap)
      }
    }, DbModule(), WebAppModule(), GoogleServiceModule())
  }
}

class WebAppModule() : AbstractModule() {
  override fun configure() {
    bind(javaClass<User>())!!.toProvider(javaClass<CurrentUserProvider>())
  }
}

class GoogleServiceModule() : AbstractModule() {
  override fun configure() {
    bind(javaClass<CloudService>())!!.to(javaClass<GoogleService>())
  }

  Provides fun provideHttpTransport(): HttpTransport = GoogleNetHttpTransport.newTrustedTransport()!!

  Provides fun provideJsonFactory(): JsonFactory = JacksonFactory.getDefaultInstance()!!

  Provides Singleton fun provideGoogleAppSettings(): GoogleAppSettings {
    val secretResource = javaClass<GuiceContext>().getResource("/client_secret.json")
    if (secretResource == null) {
      throw IOException("Resource /client_secret.json not found")
    }
    val web = ObjectMapper().readTree(secretResource.openStream())!!.get("web")!!
    return GoogleAppSettings(
        clientId = web.get("client_id")!!.asText()!!,
        clientSecret = web.get("client_secret")!!.asText()!!,
        applicationName = "Plus2Flickr"
    )
  }
}
