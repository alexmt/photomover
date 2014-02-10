package plus2flickr.services

import plus2flickr.domain.User
import plus2flickr.thirdparty.CloudService
import plus2flickr.domain.AccountType
import plus2flickr.domain.OAuthData
import com.google.inject.Inject
import plus2flickr.repositories.UserRepository
import plus2flickr.thirdparty.UserInfo
import plus2flickr.domain.OAuthToken
import plus2flickr.thirdparty.AuthorizationException
import plus2flickr.thirdparty.AuthorizationError

class UserService[Inject](val users: UserRepository, val google: CloudService) {

  private fun authorizeCloudService(
      user: User,
      authCode: String,
      accountType: AccountType,
      service: CloudService,
      accountIdRetriever: (UserInfo)->String): User {
    val token = service.authorize(authCode)
    val userInfo = service.getUserInfo(token)
    val accountId = accountIdRetriever(userInfo)
    val existingAccountInfo = user.accounts.get(accountType)
    if (existingAccountInfo != null && existingAccountInfo.id != accountId) {
      throw AuthorizationException(AuthorizationError.DUPLICATED_ACCOUNT_TYPE)
    }
    val existingUser = users.findByAccountId(accountId, accountType)
    if (existingUser != null && existingUser.getId() != user.getId()) {
      if (user.accounts.empty) {
        users.remove(user)
        return existingUser
      } else {
        throw AuthorizationException(AuthorizationError.ACCOUNT_LINKED_TO_OTHER_USER)
      }
    } else {
      user.accounts.put(accountType, OAuthData(accountId, token))
      user.enrichUserInfo(userInfo)
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

  fun authorizeGoogleAccount(user: User, authCode: String): User {
    return authorizeCloudService(user, authCode, AccountType.GOOGLE, google, { it.email!! })
  }
}
