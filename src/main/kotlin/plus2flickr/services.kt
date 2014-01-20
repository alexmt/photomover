package plus2flickr.services

import org.ektorp.CouchDbConnector
import org.ektorp.support.CouchDbRepositorySupport
import plus2flickr.domain.User
import plus2flickr.thirdparty.CloudService
import plus2flickr.domain.AccountType
import plus2flickr.domain.OAuthData
import com.google.inject.Inject

class UserRepository[Inject](db: CouchDbConnector)
: CouchDbRepositorySupport<User>(javaClass<User>(), db) {
}

class UserService[Inject](val users: UserRepository, val google: CloudService) {

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

  fun authorizeGoogleAccount(user: User, authCode: String) {
    val token = google.authorize(authCode)
    user.accounts.put(AccountType.GOOGLE, OAuthData(token))
    user.enrichUserInfo(google.getUserInfo(token))
    users.update(user)
  }
}
