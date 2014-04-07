package plus2flickr.guice

import plus2flickr.thirdparty.flickr.FlickrAppSettings
import com.google.inject.AbstractModule
import com.google.inject.Provides
import plus2flickr.thirdparty.flickr.FlickrService
import plus2flickr.ServiceUrlResolver
import plus2flickr.domain.ServiceType

class FlickrServiceModule(val settings: FlickrAppSettings) : AbstractModule() {

  override fun configure() {
    bind(javaClass<FlickrAppSettings>())!!.toInstance(settings)
  }

  Provides
  fun provideFlickrService(): FlickrService {
    return FlickrService(settings, ServiceUrlResolver(ServiceType.FLICKR))
  }
}
