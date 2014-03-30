package plus2flickr.guice

import com.sun.jersey.guice.JerseyServletModule
import plus2flickr.web.resources.UserResource
import java.util.HashMap
import com.sun.jersey.api.core.ResourceConfig
import plus2flickr.web.filters.AuthenticationResponseFilter
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer

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
