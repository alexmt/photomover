package plus2flickr.services

import com.google.inject.Inject
import org.ektorp.CouchDbConnector
import org.ektorp.support.CouchDbRepositorySupport
import plus2flickr.domain.User


class UserRepository[Inject](db: CouchDbConnector)
: CouchDbRepositorySupport<User>(javaClass<User>(), db) {
}

class UserService[Inject](val users: UserRepository) {

  fun findUserById(id: String): User? {
    return if (users.contains(id)) {
      users.get(id)
    } else {
      null
    }
  }

  fun createUser(): User {
    val user = User(firstName = "Anonymous")
    users.add(user)
    return user
  }

  fun setGoogleTokenForUser(user: User, authCode: String) {
    
  }
}
