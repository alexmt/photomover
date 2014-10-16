package photomover.couchdb

import org.ektorp.DbPath
import org.ektorp.impl.StdCouchDbInstance
import org.ektorp.http.StdHttpClient
import org.ektorp.CouchDbInstance
import org.ektorp.impl.StdCouchDbConnector
import org.ektorp.support.CouchDbRepositorySupport
import com.google.inject.Injector

class CouchDbManager(val dbName: String, val dbInstance: CouchDbInstance, val injector: Injector) {

  private val repositoryClasses = arrayListOf<Class<out CouchDbRepositorySupport<*>>?>()

  val dbPath: DbPath get() = DbPath(dbName)

  fun isDbExists() = dbInstance.checkIfDbExists(dbPath)

  fun deleteDb() = dbInstance.deleteDatabase(dbPath.getPath())

  fun addRepClass(repoClass: Class<out CouchDbRepositorySupport<*>>) {
    repositoryClasses.add(repoClass)
  }

  fun ensureDbExists() {
    if (!isDbExists()) {
      dbInstance.createDatabase(dbPath)
    }
    for (clazz in repositoryClasses) {
      val repoClass = clazz as Class<CouchDbRepositorySupport<out Any?>>
      val repository = injector.getInstance(repoClass)
      repository.initStandardDesignDocument()
    }
  }

  fun recreateDb() {
    if (isDbExists()) {
      deleteDb()
    }
    ensureDbExists()
  }
}

