package plus2flickr.web

import com.google.inject.servlet.GuiceServletContextListener
import com.google.inject.Injector
import com.google.inject.Guice
import plus2flickr.web.resources.Account
import com.sun.jersey.guice.JerseyServletModule
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer
import java.util.HashMap

class GuiceContext : GuiceServletContextListener() {

  override fun getInjector(): Injector? {
    return Guice.createInjector(object : JerseyServletModule() {

      override fun configureServlets() {
        bind(javaClass<Account>())
        val hashMap: HashMap<String, String> = hashMapOf(
            "com.sun.jersey.api.json.POJOMappingFeature" to "true"
        )
        serve("/*")!!.with(javaClass<GuiceContainer>(), hashMap)
      }
    })
  }
}

