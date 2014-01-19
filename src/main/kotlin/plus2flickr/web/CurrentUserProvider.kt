package plus2flickr.web

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse
import plus2flickr.domain.User
import plus2flickr.services.UserService
import com.google.inject.Provider
import javax.servlet.http.HttpServletRequest
import com.google.inject.Inject
import com.google.inject.servlet.RequestScoped

RequestScoped class CurrentUserProvider [Inject](
    val request: HttpServletRequest,
    val response: HttpServletResponse,
    val userService: UserService) : Provider<User> {

  override fun get(): User? {
    val authCookieName = "auth"
    val authCookie = request.findByCookieName(authCookieName)?.getValue()
    val user = if (authCookie == null ) {
      userService.createUser()
    } else {
      userService.findUserById(authCookie) ?: userService.createUser()
    }
    response.addCookie(Cookie(authCookieName, user.getId()))
    return user
  }
}
