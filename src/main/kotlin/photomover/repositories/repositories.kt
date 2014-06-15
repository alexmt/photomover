package photomover.repositories

import photomover.domain.User
import org.ektorp.support.CouchDbRepositorySupport
import org.ektorp.support.View
import org.ektorp.CouchDbConnector
import org.ektorp.ComplexKey
import com.google.inject.Inject
import org.ektorp.support.GenericRepository

trait UserRepository : GenericRepository<User> {
  fun findByAccountId(accountId: String, serviceCode: String): User?
}