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
import plus2flickr.web.RequestState
import plus2flickr.domain.AccountType

Path("/user") Produces("application/json")
class UserResource [Inject] (
    val userService: UserService,
    val state: RequestState){

  private fun User.getViewModel(): UserInfoViewModel {
    val name = if (info.firstName != null && info.lastName != null) {
      "${info.firstName} ${info.lastName}"
    } else {
      info.firstName ?: info.lastName ?: "User"
    }
    val accountsState = hashMapOf<String, Boolean>()
    for(accountType in AccountType.values()) {
      accountsState.put(accountType.name(), accounts.containsKey(accountType))
    }

    return UserInfoViewModel(name, accountsState)
  }

  GET Path("/info") fun info(): UserInfoViewModel {
    return state.getCurrentUser().getViewModel()
  }

  POST Path("/authorizeGoogleAccount") fun authorizeGoogleAccount(authCode: String)
      : OperationResponse<UserInfoViewModel> {
    try {
      val user = userService.authorizeGoogleAccount(state.getCurrentUser(), authCode)
      state.changeCurrentUser(user)
      return OperationResponse(data = user.getViewModel(), success = true)
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
