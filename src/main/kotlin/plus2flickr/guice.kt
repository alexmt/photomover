package plus2flickr.guice

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
import plus2flickr.web.resources.AppResource
import plus2flickr.couchdb.CouchDbManager
import plus2flickr.repositories.couchdb.CouchDbUserRepository
import plus2flickr.repositories.UserRepository
import javax.servlet.ServletContextEvent
import com.sun.jersey.api.core.ResourceConfig
import plus2flickr.web.filters.AuthenticationResponseFilter
import plus2flickr.domain.AccountType
import plus2flickr.CloudServiceContainer
import plus2flickr.thirdparty.flickr.FlickrService
import java.util.Properties
import plus2flickr.thirdparty.flickr.FlickrAppSettings

class GuiceContext : GuiceServletContextListener() {
  override fun getInjector(): Injector? {
    val injector = Guice.createInjector(object : JerseyServletModule() {

      override fun configureServlets() {
        val packageName = javaClass<UserResource>().getPackage()!!.getName()!!
        val hashMap: HashMap<String, String> = hashMapOf(
            "com.sun.jersey.api.json.POJOMappingFeature" to "true",
            "com.sun.jersey.config.property.packages" to packageName,
            ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS to javaClass<AuthenticationResponseFilter>().getName(),
            ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS to javaClass<AuthenticationResponseFilter>().getName()
        )
        serve("/*")!!.with(javaClass<GuiceContainer>(), hashMap)
      }
    }, DbModule(), ServicesModule())
    injector!!.getInstance(javaClass<CouchDbManager>())!!.ensureDbExists()
    return injector
  }
}

class ServicesModule : AbstractModule() {
  override fun configure() {
    install(GoogleServiceModule())
    install(FlickrServiceModule())
  }

  Provides fun provideServiceContainer(google: GoogleService, flickr: FlickrService): CloudServiceContainer {
    val container = CloudServiceContainer()
    container.register(AccountType.GOOGLE, google)
    container.register(AccountType.FLICKR, flickr)
    return container
  }
}

class DbModule(
    val dbName: String = "plus2flickr",
    val host: String = "localhost",
    val port: Int = 5984) : AbstractModule() {

  override fun configure() {
    bind(javaClass<UserRepository>())!!.to(javaClass<CouchDbUserRepository>())
  }

  Provides fun provideDbInstance(): CouchDbInstance =
      StdCouchDbInstance(StdHttpClient.Builder().host(host)?.port(port)?.build())

  Provides fun provideDbConnector(dbInstance: CouchDbInstance): CouchDbConnector =
      StdCouchDbConnector(dbName, dbInstance)

  Provides fun provideDbManager(dbInstance: CouchDbInstance, injector: Injector): CouchDbManager {
    val manager = CouchDbManager(dbName, dbInstance, injector)
    manager.addRepClass(javaClass<CouchDbUserRepository>())
    return manager
  }
}

class GoogleServiceModule : AbstractModule() {
  override fun configure() {}

  Provides fun provideHttpTransport(): HttpTransport = GoogleNetHttpTransport.newTrustedTransport()!!

  Provides fun provideJsonFactory(): JsonFactory = JacksonFactory.getDefaultInstance()!!

  Provides Singleton fun provideGoogleAppSettings(): GoogleAppSettings {
    val secretResource = javaClass<GuiceContext>().getResource("/client_secret.json")
    if (secretResource == null) {
      throw IOException("Resource /client_secret.json not found")
    }
    val web = ObjectMapper().readTree(secretResource.openStream())!!.get("web")!!
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

class FlickrServiceModule : AbstractModule() {
  override fun configure() {}

  Provides
  fun provideFlickrAppSettings(): FlickrAppSettings {
    val resource = javaClass<GuiceContext>().getResource("/flickr_app.properties")
    if (resource == null) {
      throw IOException("Resource /flickr_app.properties not found")
    }
    val properties = Properties()
    properties.load(resource.openStream())
    return FlickrAppSettings(apiKey = properties.get("apiKey").toString(), apiSecret = properties.get("apiSecret").toString())
  }
}
