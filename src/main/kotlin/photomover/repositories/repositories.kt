package photomover.repositories

import photomover.domain.User
import org.ektorp.support.View
import org.ektorp.CouchDbConnector
import org.ektorp.ComplexKey
import com.google.inject.Inject

trait UserRepository : Repository<User> {
  fun findByAccountId(accountId: String, serviceCode: String): User?
}

trait Repository<T> {
  fun add(t: T)
  fun update(t: T)
  fun remove(t: T)
  fun get(id: String): T
  fun getAll(): List<T>
  fun contains(id: String): Boolean
}
