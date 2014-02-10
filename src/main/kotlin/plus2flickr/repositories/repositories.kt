package plus2flickr.repositories

import plus2flickr.domain.User
import org.ektorp.support.CouchDbRepositorySupport
import org.ektorp.support.View
import org.ektorp.CouchDbConnector
import plus2flickr.domain.AccountType
import org.ektorp.ComplexKey
import com.google.inject.Inject
import org.ektorp.support.GenericRepository

trait UserRepository : GenericRepository<User> {
  fun findByAccountId(accountId: String, accountType: AccountType): User?
}