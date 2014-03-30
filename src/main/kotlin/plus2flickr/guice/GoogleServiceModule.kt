package plus2flickr.guice

import plus2flickr.thirdparty.google.GoogleAppSettings
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.api.client.http.HttpTransport
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory

class GoogleServiceModule(val settings: GoogleAppSettings) : AbstractModule() {

  override fun configure() {
    bind(javaClass<GoogleAppSettings>())!!.toInstance(settings)
  }

  Provides fun provideHttpTransport(): HttpTransport = GoogleNetHttpTransport.newTrustedTransport()!!

  Provides fun provideJsonFactory(): JsonFactory = JacksonFactory.getDefaultInstance()!!
}