package plus2flickr.web.resources

import javax.ws.rs.Produces
import javax.ws.rs.Path
import plus2flickr.web.models.UserInfo
import javax.ws.rs.GET
import com.google.inject.Inject
import plus2flickr.domain.User
import com.google.inject.Provider
import javax.ws.rs.POST
import plus2flickr.services.UserService
import plus2flickr.web.models.OperationResponse
import plus2flickr.thirdparty.AuthorizationException

Path("/user") Produces("application/json")
class UserResource [Inject] (
    val userProvider: Provider<User>,
    val userService: UserService){

  val user: User
    get() = userProvider.get()!!

  GET Path("/info") fun info(): UserInfo {
    return UserInfo(firstName = user.firstName, lastName = user.lastName)
  }

  POST Path("/authorizeGoogleAccount") fun authorizeGoogleAccount(authCode: String)
      : OperationResponse<UserInfo> {
    try {
      userService.authorizeGoogleAccount(user, authCode)
      return OperationResponse(data = info(), success = true)
    } catch (e: AuthorizationException){
      return OperationResponse(errorMessage = e.message, success = false)
    }
  }
}
