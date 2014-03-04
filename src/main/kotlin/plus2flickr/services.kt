package plus2flickr.services

import plus2flickr.domain.User
import plus2flickr.thirdparty.CloudService
import plus2flickr.domain.AccountType
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

class UserService[Inject](
    val users: UserRepository,
    val servicesContainer: CloudServiceContainer) {

  private fun User.getAuthData(accountType: AccountType): OAuthData {
    val authData = this.accounts[accountType]
    if (authData == null) {
      throw IllegalArgumentException("User does not have account of type '$accountType'")
    }
    return authData
  }

  private fun enrichUserInfo(info: UserInfo, accountInfo: AccountInfo) {
    info.email = info.email ?: accountInfo.email
    info.firstName = info.firstName ?: accountInfo.firstName
    info.lastName = info.lastName ?: accountInfo.lastName
  }

  private fun oauth1Authorizer(token: String, secret: String, verifier: String) =
      {(service: CloudService) -> service.authorize(token, secret, verifier) }

  private fun oauth2Authorizer(code: String) = {(service: CloudService) -> service.authorize(code) }

  private fun authorizeCloudService(
      user: User,
      authorizer: (service: CloudService)->OAuthToken,
      accountType: AccountType): User {
    val service = servicesContainer.get(accountType)
    val token = authorizer(service)
    val accountInfo = service.getAccountInfo(token)
    val existingAccountInfo = user.accounts.get(accountType)
    if (existingAccountInfo != null && existingAccountInfo.id != accountInfo.id) {
      throw AuthorizationException(AuthorizationError.DUPLICATED_ACCOUNT_TYPE)
    }
    val existingUser = users.findByAccountId(accountInfo.id, accountType)
    if (existingUser != null && existingUser.getId() != user.getId()) {
      if (user.accounts.empty) {
        users.remove(user)
        return existingUser
      } else {
        throw AuthorizationException(AuthorizationError.ACCOUNT_LINKED_TO_OTHER_USER)
      }
    } else {
      user.accounts.put(accountType, OAuthData(accountInfo.id, token))
      enrichUserInfo(user.info, accountInfo)
      users.update(user)
      return user
    }
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
      authorizeCloudService(user, oauth2Authorizer(authCode), AccountType.GOOGLE)

  fun getFlickrAuthorizationUrl(user: User, callback: String): String {
    val authorizationRequest = servicesContainer.get(AccountType.FLICKR).requestAuthorization(callback)
    user.flickrAuthorizationRequestSecret = authorizationRequest.secret
    users.update(user)
    return authorizationRequest.url
  }

  fun authorizeFlickrAccount(user: User, token: String, verifier: String): User {
    val secret = user.flickrAuthorizationRequestSecret
    if (secret == null) {
      throw IllegalArgumentException("User does not request secret for Flickr authorization")
    }
    user.flickrAuthorizationRequestSecret = null
    return authorizeCloudService(user, oauth1Authorizer(token, secret, verifier), AccountType.FLICKR)
  }

  fun getPhotoUrl(user: User, accountType: AccountType, photoId: String, size: ImageSize): String {
    val authData = user.getAuthData(accountType)
    val service = servicesContainer.get(accountType)
    return service.getPhotoUrl(photoId, size, authData.token)
  }

  fun getServiceAlbums(user: User, accountType: AccountType): List<Album> {
    val authData = user.getAuthData(accountType)
    val service = servicesContainer.get(accountType)
    return service.getAlbums(authData.id, authData.token)
  }
}
