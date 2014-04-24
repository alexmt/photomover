package plus2flickr.services

import plus2flickr.domain.User
import plus2flickr.thirdparty.CloudService
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
import plus2flickr.domain.ServiceOperationErrorException
import plus2flickr.domain.OperationError
import com.google.common.base.Preconditions

enum class UserServiceError {
  CANNOT_REMOVE_LAST_SERVICE
}

class UserService[Inject](val users: UserRepository, val servicesContainer: CloudServiceContainer) {

  private fun User.getAuthData(serviceCode: String): OAuthData {
    val authData = this.accounts[serviceCode]
    if (authData == null) {
      throw IllegalArgumentException("User does not have account of type '$serviceCode'")
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

  private fun callServiceAction<T>(user: User, serviceCode: String, action: (CloudService, OAuthData) -> T) : T {
    val authData = user.getAuthData(serviceCode)
    val service = servicesContainer.get(serviceCode)
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
      if (!Strings.isNullOrEmpty(authData.token.refreshToken)) {
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
      user: User, authorizer: (service: CloudService)->OAuthToken, serviceCode: String) {
    val service = servicesContainer.get(serviceCode)
    val token = authorizer(service)
    val accountInfo = service.getAccountInfo(token)
    val existingAccountInfo = user.accounts.get(serviceCode)
    if (existingAccountInfo != null && existingAccountInfo.id != accountInfo.id) {
      throw AuthorizationException(AuthorizationError.DUPLICATED_ACCOUNT_TYPE)
    }
    val existingUser = users.findByAccountId(accountInfo.id, serviceCode)
    if (existingUser != null && existingUser.getId() != user.getId()) {
      user.merge(existingUser)
      users.remove(existingUser)
    }
    user.accounts.put(serviceCode, OAuthData(accountInfo.id, token, isTokenNeedRefresh = false))
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

  fun deleteUser(user: User) {
    users.remove(user)
  }

  fun updateUserInfo(user: User, userInfo: UserInfo) {
    user.info = userInfo
    users.update(user)
  }

  fun removeService(user: User, serviceCode: String) {
    if (user.accounts.containsKey(serviceCode) ) {
      if (user.accounts.size == 1) {
        throw ServiceOperationErrorException.create(OperationError(UserServiceError.CANNOT_REMOVE_LAST_SERVICE))
      } else {
        user.accounts.remove(serviceCode)
        users.update(user)
      }
    }
  }

  fun authorizeOAuth2Service(user: User, authCode: String, serviceCode: String) {
    authorizeCloudService(user, oauth2Authorizer(authCode), serviceCode)
  }

  fun getOAuthAuthorizationUrl(user: User, callback: String, serviceCode: String): String {
    val authorizationRequest = servicesContainer.get(serviceCode).requestAuthorization(callback)
    user.oauthRequestSecret.put(serviceCode, authorizationRequest.secret)
    users.update(user)
    return authorizationRequest.url
  }

  fun authorizeOAuthService(user: User, token: String, verifier: String, serviceCode: String) {
    val secret = user.oauthRequestSecret.remove(serviceCode)
    if (secret == null) {
      throw IllegalArgumentException("User does not request secret for $serviceCode authorization")
    }
    authorizeCloudService(user, oauth1Authorizer(token, secret, verifier), serviceCode)
  }

  fun getPhotoUrl(user: User, serviceCode: String, photoId: String, size: ImageSize): String {
    return callServiceAction(user, serviceCode, {
      (service, authData) -> service.getPhotoUrl(photoId, size, authData.token)
    })
  }

  fun getAlbums(user: User, serviceCode: String): List<Album> {
    return callServiceAction(user, serviceCode, {
      (service, authData) -> service.getAlbums(authData.id, authData.token)
    })
  }

  fun getAlbumPhotos(user: User, serviceCode: String, albumId: String) : List<Photo> {
    return callServiceAction(user, serviceCode, {
      (service, authData) -> service.getPhotos(authData.id, authData.token, albumId, ImageSize.THUMB)
    })
  }
}
