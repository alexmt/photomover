package plus2flickr.web.resources

import javax.ws.rs.Produces
import javax.ws.rs.Path
import plus2flickr.web.models.UserInfoViewModel
import javax.ws.rs.GET
import com.google.inject.Inject
import plus2flickr.domain.User
import com.google.inject.Provider
import javax.ws.rs.POST
import plus2flickr.services.UserService
import plus2flickr.web.models.OperationResponse
import plus2flickr.thirdparty.AuthorizationException
import plus2flickr.thirdparty.UserInfo
import plus2flickr.thirdparty.google.GoogleAppSettings
import plus2flickr.web.models.GoogleAppSettingsViewModel

Path("/user") Produces("application/json")
class UserResource [Inject] (
    val userProvider: Provider<User>,
    val userService: UserService){

  fun currentUser(): User = userProvider.get()!!

  private fun UserInfo.toViewModel(): UserInfoViewModel = UserInfoViewModel(
      name = if (firstName != null && lastName != null) {
        "$firstName $lastName"
      } else {
        firstName ?: lastName ?: "User"
      })

  GET Path("/info") fun info(): UserInfoViewModel {
    return currentUser().info.toViewModel()
  }

  POST Path("/authorizeGoogleAccount") fun authorizeGoogleAccount(authCode: String)
      : OperationResponse<UserInfoViewModel> {
    try {
      val user = currentUser()
      userService.authorizeGoogleAccount(user, authCode)
      return OperationResponse(data = user.info.toViewModel(), success = true)
    } catch (e: AuthorizationException){
      return OperationResponse(errorMessage = e.message, success = false)
    }
  }
}

Path("/app") Produces("application/json")
class AppResource[Inject](val googleAppSettings: GoogleAppSettings) {

  GET Path("/googleAppSettings") fun googleAppSettings(): GoogleAppSettingsViewModel {
    return GoogleAppSettingsViewModel(
        clientId = googleAppSettings.clientId, scopes = googleAppSettings.scopes)
  }
}
