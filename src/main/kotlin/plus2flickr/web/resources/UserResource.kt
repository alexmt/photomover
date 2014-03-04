package plus2flickr.web.resources

import plus2flickr.web.models.OperationResponse
import plus2flickr.thirdparty.AuthorizationException
import plus2flickr.web.models.UserInfoViewModel
import javax.ws.rs.Path
import javax.ws.rs.POST
import plus2flickr.domain.AccountType
import plus2flickr.thirdparty.Album
import javax.ws.rs.GET
import plus2flickr.domain.User
import plus2flickr.web.RequestState
import plus2flickr.services.UserService
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import com.google.common.base.CharMatcher
import com.google.inject.Inject
import org.scribe.model.OAuthConstants

Path("/user") Produces("application/json")
class UserResource [Inject] (
    val userService: UserService,
    val state: RequestState) {

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

  POST Path("/albums") fun albums(service: String): List<Album> {
    return userService.getServiceAlbums(state.getCurrentUser(), AccountType.valueOf(service))
  }

  POST Path("/google/verify") fun verifyGoogle(authCode: String)
      : OperationResponse<UserInfoViewModel> {
    try {
      val user = userService.authorizeGoogleAccount(state.getCurrentUser(), authCode)
      state.changeCurrentUser(user)
      return OperationResponse(data = user.getViewModel(), success = true)
    } catch (e: AuthorizationException){
      return OperationResponse(errorMessage = e.message, success = false)
    }
  }

  GET Path("/flickr/authorize") fun authorizeFlickr(
      Context request: HttpServletRequest, Context response: HttpServletResponse) {
    val path = CharMatcher.anyOf("/")!!.trimFrom(request.getRequestURI()!!.replace("/authorize", "/verify"))
    val url ="${request.getScheme()}://${request.getServerName()}:${request.getServerPort()}/$path"
    response.sendRedirect(userService.getFlickrAuthorizationUrl(state.getCurrentUser(), url))
  }

  GET Path("/flickr/verify") fun verifyFlickr(
      Context request: HttpServletRequest, Context response: HttpServletResponse) {
    val authCode = request.getParameter(OAuthConstants.TOKEN).toString()
    val verifier = request.getParameter(OAuthConstants.VERIFIER).toString()
    userService.authorizeFlickrAccount(state.getCurrentUser(), authCode, verifier)
    response.sendRedirect("/")
  }
}