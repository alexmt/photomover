package photomover.guice

import com.google.inject.AbstractModule
import photomover.repositories.UserRepository
import photomover.repositories.couchdb.CouchDbUserRepository
import com.google.inject.Provides
import org.ektorp.CouchDbInstance
import org.ektorp.impl.StdCouchDbInstance
import org.ektorp.http.StdHttpClient
import org.ektorp.CouchDbConnector
import org.ektorp.impl.StdCouchDbConnector
import com.google.inject.Injector
import photomover.couchdb.CouchDbManager

class DbModule(
    val dbName: String = "plus2flickr",
    val url: String = "http://localhost:5984") : AbstractModule() {

  override fun configure() {
    bind(javaClass<UserRepository>())!!.to(javaClass<CouchDbUserRepository>())
  }

  Provides fun provideDbInstance(): CouchDbInstance =
      StdCouchDbInstance(StdHttpClient.Builder().url(url)?.build())

  Provides fun provideDbConnector(dbInstance: CouchDbInstance): CouchDbConnector =
      StdCouchDbConnector(dbName, dbInstance)

  Provides fun provideDbManager(dbInstance: CouchDbInstance, injector: Injector): CouchDbManager {
    val manager = CouchDbManager(dbName, dbInstance, injector)
    manager.addRepClass(javaClass<CouchDbUserRepository>())
    return manager
  }
}