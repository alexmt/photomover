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

class UserService(
    val users: UserRepository,
    val accountToService: Map<AccountType, CloudService>) {

  private fun enrichUserInfo(info: UserInfo, accountInfo: AccountInfo) {
    info.email = info.email ?: accountInfo.email
    info.firstName = info.firstName ?: accountInfo.firstName
    info.lastName = info.lastName ?: accountInfo.lastName
  }

  private fun authorizeCloudService(
      user: User,
      authCode: String,
      accountType: AccountType): User {
    val service = accountToService[accountType]!!
    val token = service.authorize(authCode)
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

  fun authorizeGoogleAccount(user: User, authCode: String): User {
    return authorizeCloudService(user, authCode, AccountType.GOOGLE)
  }

  fun getServiceAlbums(user: User, accountType: AccountType): List<Album> {
    val authData  = user.accounts[accountType]!!
    val service = accountToService[accountType]!!
    return service.getAlbums(authData.id, authData.token)
  }
}
