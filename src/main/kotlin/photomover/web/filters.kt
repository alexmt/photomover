package photomover.web.filters

import com.google.inject.Provider
import photomover.services.UserService
import com.sun.jersey.spi.container.ContainerResponseFilter
import com.sun.jersey.spi.container.ContainerRequest
import com.sun.jersey.spi.container.ContainerResponse
import com.sun.jersey.spi.container.ContainerRequestFilter
import photomover.web.RequestState
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.Context
import javax.servlet.http.HttpSession
import com.google.common.base.Strings
import com.google.inject.Inject

class AuthenticationResponseFilter[Inject](
    val stateProvider: Provider<RequestState>,
    val userService: UserService,
    Context val requestProvider: Provider<HttpServletRequest> )
: ContainerResponseFilter, ContainerRequestFilter {

  class object {
    public fun setSessionUserId(userId: String?, session: HttpSession) {
      if (Strings.isNullOrEmpty(userId)) {
        session.removeAttribute("userId")
      } else {
        session.setAttribute("userId", userId)
      }
    }

    public fun getSessionUserId(session: HttpSession): String? {
      return session.getAttribute("userId")?.toString()
    }
  }

  override fun filter(request: ContainerRequest?): ContainerRequest? {
    val session = requestProvider.get().getSession(true)
    val userId = getSessionUserId(session)
    val state = stateProvider.get()
    state.user = if (userId == null || userId.isEmpty()) {
      userService.createUser()
    } else {
      userService.findUserById(userId) ?: userService.createUser()
    }
    return request
  }

  override fun filter(request: ContainerRequest?, response: ContainerResponse?): ContainerResponse? {
    val state = stateProvider.get()
    val session = requestProvider.get().getSession(true)
    setSessionUserId(state.user?.getId(), session)
    return response
  }
}
