package plus2flickr.web.resources

import plus2flickr.web.models.OperationResponse
import plus2flickr.thirdparty.AuthorizationException
import plus2flickr.web.models.UserInfoViewModel
import javax.ws.rs.Path
import javax.ws.rs.POST
import plus2flickr.domain.ServiceType
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
import org.scribe.model.OAuthConstants
import javax.ws.rs.PathParam
import plus2flickr.thirdparty.ImageSize
import plus2flickr.thirdparty.Photo
import plus2flickr.web.filters.AuthenticationResponseFilter
import plus2flickr.web.models.DetailedUserInfoViewModel
import plus2flickr.domain.UserInfo
import plus2flickr.web.models.ServiceAlbumInput
import plus2flickr.web.models.ErrorInfo
import com.google.inject.Inject

Path("/user") Produces("application/json")
class UserResource [Inject] (val userService: UserService, val state: RequestState) {

  private fun parseServiceType(serviceType: String) = ServiceType.valueOf(serviceType.toUpperCase())

  private fun User.getUserInfo(): UserInfoViewModel {
    val name = if (info.firstName != null && info.lastName != null) {
      "${info.firstName} ${info.lastName}"
    } else {
      info.firstName ?: info.lastName ?: "User"
    }
    val accountsState = hashMapOf<String, Boolean>()
    for(accountType in ServiceType.values().sortBy { it.name() }) {
      val oAuthData = accounts.get(accountType)
      accountsState.put(accountType.name().toLowerCase(), oAuthData != null && oAuthData.isTokenNeedRefresh == false)
    }

    return UserInfoViewModel(name, accountsState)
  }

  GET Path("/info") fun info(): UserInfoViewModel {
    return state.currentUser.getUserInfo()
  }

  GET Path("/detailedInfo") fun detailedInfo(): DetailedUserInfoViewModel {
    val info = state.currentUser.info
    return DetailedUserInfoViewModel(
        firstName = info.firstName ?: "",
        lastName = info.lastName ?: "",
        email = info.email ?: "",
        linkedServices = state.currentUser.accounts.entrySet()
            .filter { !it.value.isTokenNeedRefresh }
            .map { it.key.name().toLowerCase() })
  }

  POST Path("/updateInfo")
  fun updateInfo(info: DetailedUserInfoViewModel) {
    userService.updateUserInfo(state.currentUser, UserInfo(
        firstName = info.firstName,
        lastName = info.lastName,
        email = info.email
    ))
  }

  POST Path("/albums") fun albums(service: String): List<Album> {
    return userService.getAlbums(state.currentUser, parseServiceType(service))
  }

  POST Path("/photos") fun photos(input: ServiceAlbumInput): List<Photo> {
    return userService.getAlbumPhotos(
        state.currentUser, ServiceType.valueOf(input.service.toUpperCase()), input.albumId)
  }

  GET Path("/photo/redirect/{account}/{id}/{size}") fun goToPhoto(Context response: HttpServletResponse,
      PathParam("account") account: String, PathParam("id") id: String, PathParam("size") size: String) {
    val accountType = ServiceType.valueOf(account.toUpperCase())
    val imageSize = ImageSize.valueOf(size)
    response.sendRedirect(userService.getPhotoUrl(state.currentUser, accountType, id, imageSize))
  }

  POST Path("removeService") fun removeService(account: String) =
      userService.removeService(state.currentUser, ServiceType.valueOf(account.toUpperCase()))

  POST Path("deleteAccount") fun deleteAccount(
      Context request: HttpServletRequest, Context response: HttpServletResponse) {
    userService.deleteUser(state.currentUser)
    logout(request, response)
  }

  POST Path("/{service}/verifyOAuth2") fun verifyOAuth2(PathParam("service") service: String, code: String)
      : OperationResponse<UserInfoViewModel> {
    try {
      userService.authorizeOAuth2Service(state.currentUser, code, parseServiceType(service))
      return OperationResponse(data = state.currentUser.getUserInfo(), success = true)
    } catch (e: AuthorizationException){
      return OperationResponse(success = false, errors = listOf(ErrorInfo(message = e.message)))
    }
  }

  GET Path("/{service}/authorizeOAuth") fun authorizeOAuth(
      PathParam("service") service: String,
      Context request: HttpServletRequest,
      Context response: HttpServletResponse) {
    val path = CharMatcher.anyOf("/")!!.trimFrom(request.getRequestURI()!!.replace("/authorize", "/verify"))
    val url ="${request.getScheme()}://${request.getServerName()}:${request.getServerPort()}/$path"
    response.sendRedirect(userService.getOAuthAuthorizationUrl(state.currentUser, url, parseServiceType(service)))
  }

  GET Path("/{service}/verifyOAuth") fun verifyOAuth(
      PathParam("service") service: String,
      Context request: HttpServletRequest,
      Context response: HttpServletResponse) {
    val authCode = request.getParameter(OAuthConstants.TOKEN).toString()
    val verifier = request.getParameter(OAuthConstants.VERIFIER).toString()
    userService.authorizeOAuthService(state.currentUser, authCode, verifier, parseServiceType(service))
    response.sendRedirect("/")
  }

  GET Path("/logout") fun logout(
      Context request: HttpServletRequest, Context response: HttpServletResponse) {
    request.getCookies()?.filter {
      it.getName().equals(AuthenticationResponseFilter.authCookieName)
    }?.forEach {
      it.setPath("/")
      it.setValue("")
      it.setMaxAge(0)
      response.addCookie(it)
    }
    response.sendRedirect("/")
  }
}