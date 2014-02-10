package plus2flickr.web

import javax.servlet.http.HttpServletResponse
import plus2flickr.domain.User
import plus2flickr.services.UserService
import javax.servlet.http.HttpServletRequest
import com.google.inject.servlet.RequestScoped
import com.google.common.base.Suppliers
import com.google.inject.Inject

RequestScoped class RequestState[Inject] (
    val request: HttpServletRequest,
    val response: HttpServletResponse,
    val userService: UserService) {

  private var user: User? = null

  var currentUser: User
    get() = user!!
    set(value) {
      user = value
    }
}