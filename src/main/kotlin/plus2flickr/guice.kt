package plus2flickr.guice

import com.google.inject.Injector
import com.sun.jersey.guice.JerseyServletModule
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer
import java.util.HashMap
import com.google.inject.AbstractModule
import org.ektorp.CouchDbInstance
import org.ektorp.http.StdHttpClient
import com.google.inject.Provides
import org.ektorp.impl.StdCouchDbInstance
import plus2flickr.web.resources.UserResource
import org.ektorp.CouchDbConnector
import org.ektorp.impl.StdCouchDbConnector
import com.google.api.client.http.HttpTransport
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import plus2flickr.thirdparty.google.GoogleAppSettings
import plus2flickr.thirdparty.google.GoogleService
import plus2flickr.couchdb.CouchDbManager
import plus2flickr.repositories.couchdb.CouchDbUserRepository
import plus2flickr.repositories.UserRepository
import com.sun.jersey.api.core.ResourceConfig
import plus2flickr.web.filters.AuthenticationResponseFilter
import plus2flickr.domain.AccountType
import plus2flickr.CloudServiceContainer
import plus2flickr.thirdparty.flickr.FlickrService
import plus2flickr.thirdparty.flickr.FlickrAppSettings
import plus2flickr.ServiceUrlResolver

class WebServicesModule : JerseyServletModule() {

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
}

class ServicesModule(
    val googleAppSettings: GoogleAppSettings, val flickrAppSettings: FlickrAppSettings) : AbstractModule() {

  override fun configure() {
    install(GoogleServiceModule(googleAppSettings))
    install(FlickrServiceModule(flickrAppSettings))
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
    val url: String = "http://localhost:5984") : AbstractModule() {

  override fun configure() {
    bind(javaClass<UserRepository>())!!.to(javaClass<CouchDbUserRepository>())
  }

  Provides fun provideDbInstance(): CouchDbInstance =
      StdCouchDbInstance(StdHttpClient.Builder().url(url)?.build())

  Provides fun provideDbConnector(dbInstance: CouchDbInstance): CouchDbConnector =
      StdCouchDbConnector(dbName, dbInstance)

  Provides fun provideDbManager(dbInstance: CouchDbInstance, injector: Injector): CouchDbManager {
    val manager = CouchDbManager(dbName, dbInstance, injector)
    manager.addRepClass(javaClass<CouchDbUserRepository>())
    return manager
  }
}

class GoogleServiceModule(val settings: GoogleAppSettings) : AbstractModule() {

  override fun configure() {
    bind(javaClass<GoogleAppSettings>())!!.toInstance(settings)
  }

  Provides fun provideHttpTransport(): HttpTransport = GoogleNetHttpTransport.newTrustedTransport()!!

  Provides fun provideJsonFactory(): JsonFactory = JacksonFactory.getDefaultInstance()!!
}

class FlickrServiceModule(val settings: FlickrAppSettings) : AbstractModule() {

  override fun configure() {
    bind(javaClass<FlickrAppSettings>())!!.toInstance(settings)
  }

  Provides
  fun provideFlickrService(): FlickrService {
    return FlickrService(settings, ServiceUrlResolver(AccountType.FLICKR))
  }
}
