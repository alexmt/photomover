package plus2flickr.services

import plus2flickr.domain.User
import plus2flickr.thirdparty.CloudService
import plus2flickr.domain.ServiceType
import plus2flickr.repositories.UserRepository
import plus2flickr.thirdparty.AccountInfo
import plus2flickr.thirdparty.AuthorizationException
import plus2flickr.thirdparty.AuthorizationError
import plus2flickr.thirdparty.Album
import plus2flickr.domain.OAuthData
import plus2flickr.domain.UserInfo
import plus2flickr.CloudServiceContainer
import plus2flickr.thirdparty.OAuthToken
import com.google.inject.Inject
import plus2flickr.thirdparty.ImageSize
import plus2flickr.thirdparty.Photo
import plus2flickr.thirdparty.InvalidTokenException
import com.google.common.base.Strings

class UserService[Inject](
    val users: UserRepository,
    val servicesContainer: CloudServiceContainer) {

  private fun User.getAuthData(accountType: ServiceType): OAuthData {
    val authData = this.accounts[accountType]
    if (authData == null) {
      throw IllegalArgumentException("User does not have account of type '$accountType'")
    }
    return authData
  }

  private fun User.merge(user: User) {
    accounts.putAll(user.accounts)
    info = user.info
  }

  private fun enrichUserInfo(info: UserInfo, accountInfo: AccountInfo) {
    info.email = info.email ?: accountInfo.email
    info.firstName = info.firstName ?: accountInfo.firstName
    info.lastName = info.lastName ?: accountInfo.lastName
  }

  private fun oauth1Authorizer(token: String, secret: String, verifier: String) =
      {(service: CloudService) -> service.authorize(token, secret, verifier) }

  private fun oauth2Authorizer(code: String) = {(service: CloudService) -> service.authorize(code) }

  private fun callServiceAction<T>(user: User, accountType: ServiceType, action: (CloudService, OAuthData) -> T) : T {
    val authData = user.getAuthData(accountType)
    val service = servicesContainer.get(accountType)
    fun callAction(): T {
      if (authData.isTokenNeedRefresh) {
        throw InvalidTokenException()
      }
      try {
        return action(service, authData)
      } catch (ex: InvalidTokenException) {
        authData.isTokenNeedRefresh = true
        users.update(user)
        throw ex
      }
    }
    try {
      return callAction()
    } catch (ex: InvalidTokenException) {
      if (accountType.isOAuth2 && !Strings.isNullOrEmpty(authData.token.refreshToken)) {
        authData.token.accessToken = service.refreshAccessToken(authData.token.refreshToken!!)
        authData.isTokenNeedRefresh = false
        users.update(user)
        return callAction()
      } else {
        throw ex
      }
    }
  }

  private fun authorizeCloudService(
      user: User,
      authorizer: (service: CloudService)->OAuthToken,
      accountType: ServiceType) {
    val service = servicesContainer.get(accountType)
    val token = authorizer(service)
    val accountInfo = service.getAccountInfo(token)
    val existingAccountInfo = user.accounts.get(accountType)
    if (existingAccountInfo != null && existingAccountInfo.id != accountInfo.id) {
      throw AuthorizationException(AuthorizationError.DUPLICATED_ACCOUNT_TYPE)
    }
    val existingUser = users.findByAccountId(accountInfo.id, accountType)
    if (existingUser != null && existingUser.getId() != user.getId()) {
      user.merge(existingUser)
      users.remove(existingUser)
    }
    user.accounts.put(accountType, OAuthData(accountInfo.id, token, isTokenNeedRefresh = false))
    enrichUserInfo(user.info, accountInfo)
    users.update(user)
  }

  fun findUserById(id: String): User? {
    return if (users.contains(id)) {
      users.get(id)
    } else {
      null
    }
  }

  fun createUser(): User {
    val user = User()
    users.add(user)
    return user
  }

  fun authorizeGoogleAccount(user: User, authCode: String) =
      authorizeCloudService(user, oauth2Authorizer(authCode), ServiceType.GOOGLE)

  fun getFlickrAuthorizationUrl(user: User, callback: String): String {
    val authorizationRequest = servicesContainer.get(ServiceType.FLICKR).requestAuthorization(callback)
    user.flickrAuthorizationRequestSecret = authorizationRequest.secret
    users.update(user)
    return authorizationRequest.url
  }

  fun authorizeFlickrAccount(user: User, token: String, verifier: String) {
    val secret = user.flickrAuthorizationRequestSecret
    if (secret == null) {
      throw IllegalArgumentException("User does not request secret for Flickr authorization")
    }
    user.flickrAuthorizationRequestSecret = null
    authorizeCloudService(user, oauth1Authorizer(token, secret, verifier), ServiceType.FLICKR)
  }

  fun getPhotoUrl(user: User, accountType: ServiceType, photoId: String, size: ImageSize): String {
    return callServiceAction(user, accountType, {
      (service, authData) -> service.getPhotoUrl(photoId, size, authData.token)
    })
  }

  fun getAlbums(user: User, accountType: ServiceType): List<Album> {
    return callServiceAction(user, accountType, {
      (service, authData) -> service.getAlbums(authData.id, authData.token)
    })
  }

  fun getAlbumPhotos(user: User, accountType: ServiceType, albumId: String) : List<Photo> {
    return callServiceAction(user, accountType, {
      (service, authData) -> service.getPhotos(authData.id, authData.token, albumId, ImageSize.THUMB)
    })
  }
}
