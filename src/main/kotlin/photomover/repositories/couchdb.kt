package photomover.repositories.couchdb

import com.google.inject.Inject
import org.ektorp.CouchDbConnector
import org.ektorp.support.CouchDbRepositorySupport
import photomover.domain.User
import photomover.repositories.UserRepository
import org.ektorp.ComplexKey
import org.ektorp.support.View

class CouchDbUserRepository[Inject](db: CouchDbConnector)
: CouchDbRepositorySupport<User>(javaClass<User>(), db, false), UserRepository {

  View(name = "byAccountId",
       map = "function(doc) { for(service in doc.accounts) { emit([service, doc.accounts[service].id], doc); } }")
  override fun findByAccountId(accountId: String, serviceCode: String): User? {
    return queryView("byAccountId", ComplexKey.of(serviceCode, accountId))!!.firstOrNull()
  }
}