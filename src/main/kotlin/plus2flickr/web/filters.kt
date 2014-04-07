package plus2flickr.web.filters

import com.google.inject.Provider
import plus2flickr.services.UserService
import com.sun.jersey.spi.container.ContainerResponseFilter
import com.sun.jersey.spi.container.ContainerRequest
import com.sun.jersey.spi.container.ContainerResponse
import com.sun.jersey.spi.container.ContainerRequestFilter
import plus2flickr.web.RequestState
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.NewCookie
import com.google.inject.Inject

class AuthenticationResponseFilter[Inject](val stateProvider: Provider<RequestState>, val userService: UserService)
: ContainerResponseFilter, ContainerRequestFilter {

  private final val authCookieName = "auth"

  override fun filter(request: ContainerRequest?): ContainerRequest? {
    val state = stateProvider.get()!!
    val authCookie = request?.getCookies()?.get(authCookieName)?.getValue()
    state.changeCurrentUser(if (authCookie == null ) {
      userService.createUser()
    } else {
      userService.findUserById(authCookie) ?: userService.createUser()
    })
    return request
  }

  override fun filter(request: ContainerRequest?, response: ContainerResponse?): ContainerResponse? {
    val cookie = NewCookie(authCookieName,
        stateProvider.get()!!.currentUser.getId(), "/", null, null, NewCookie.DEFAULT_MAX_AGE, false)
    response?.getHttpHeaders()!!.put(HttpHeaders.SET_COOKIE, listOf(cookie))
    return response
  }
}