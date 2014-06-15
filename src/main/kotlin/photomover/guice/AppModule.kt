package photomover.guice

import photomover.thirdparty.google.GoogleAppSettings
import photomover.thirdparty.flickr.FlickrAppSettings
import com.google.inject.AbstractModule
import com.google.inject.Provides
import photomover.thirdparty.google.GoogleService
import photomover.thirdparty.flickr.FlickrService
import photomover.CloudServiceContainer
import photomover.AppPresentationSettings

class AppModule(
    val googleAppSettings: GoogleAppSettings,
    val flickrAppSettings: FlickrAppSettings,
    val presentationSettings: AppPresentationSettings) : AbstractModule() {

  override fun configure() {
    install(GoogleServiceModule(googleAppSettings))
    install(FlickrServiceModule(flickrAppSettings))
  }

  Provides fun provideServiceContainer(google: GoogleService, flickr: FlickrService): CloudServiceContainer {
    val container = CloudServiceContainer()
    container.register("google", google)
    container.register("flickr", flickr)
    return container
  }

  Provides fun provideAppPresentationSettings() = presentationSettings
}