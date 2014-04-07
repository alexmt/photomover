package plus2flickr.guice

import plus2flickr.thirdparty.google.GoogleAppSettings
import plus2flickr.thirdparty.flickr.FlickrAppSettings
import com.google.inject.AbstractModule
import com.google.inject.Provides
import plus2flickr.thirdparty.google.GoogleService
import plus2flickr.thirdparty.flickr.FlickrService
import plus2flickr.CloudServiceContainer
import plus2flickr.domain.ServiceType

class ServicesModule(
    val googleAppSettings: GoogleAppSettings, val flickrAppSettings: FlickrAppSettings) : AbstractModule() {

  override fun configure() {
    install(GoogleServiceModule(googleAppSettings))
    install(FlickrServiceModule(flickrAppSettings))
  }

  Provides fun provideServiceContainer(google: GoogleService, flickr: FlickrService): CloudServiceContainer {
    val container = CloudServiceContainer()
    container.register(ServiceType.GOOGLE, google)
    container.register(ServiceType.FLICKR, flickr)
    return container
  }
}