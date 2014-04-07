package plus2flickr.repositories.couchdb

import com.google.inject.Inject
import org.ektorp.CouchDbConnector
import org.ektorp.support.CouchDbRepositorySupport
import plus2flickr.domain.User
import plus2flickr.repositories.UserRepository
import org.ektorp.ComplexKey
import plus2flickr.domain.ServiceType
import org.ektorp.support.View

class CouchDbUserRepository[Inject](db: CouchDbConnector)
: CouchDbRepositorySupport<User>(javaClass<User>(), db, false), UserRepository {

  View(name = "byAccountId",
       map = "function(doc) { for(account in doc.accounts) { emit([account, doc.accounts[account].id], doc); } }")
  override fun findByAccountId(accountId: String, accountType: ServiceType): User? {
    return queryView("byAccountId", ComplexKey.of(accountType, accountId))!!.firstOrNull()
  }
}