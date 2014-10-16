package photomover.guice

import photomover.thirdparty.flickr.FlickrAppSettings
import com.google.inject.AbstractModule
import com.google.inject.Provides
import photomover.thirdparty.flickr.FlickrService

class FlickrServiceModule(val settings: FlickrAppSettings) : AbstractModule() {

  override fun configure() {
    bind(javaClass<FlickrAppSettings>()).toInstance(settings)
  }

  Provides
  fun provideFlickrService(): FlickrService {
    return FlickrService(settings)
  }
}
