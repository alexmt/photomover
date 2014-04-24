package plus2flickr.integration

import org.testng.annotations.Test
import org.testng.annotations.Guice
import plus2flickr.domain.User
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import plus2flickr.guice.DbModule
import plus2flickr.couchdb.CouchDbManager
import org.testng.annotations.BeforeTest
import plus2flickr.repositories.UserRepository
import com.google.inject.Inject
import plus2flickr.domain.OAuthData

Guice(modules = array(javaClass<DbModule>()))
class CouchDbUserRepositoryTest[Inject](val users: UserRepository, val dbManager: CouchDbManager) {

  val google = "google"
  val flickr = "flickr"

  BeforeTest fun setUp() {
    dbManager.recreateDb()
  }

  Test fun findUserByAccountId() {
    val googleUser = User(accounts = hashMapOf(google to OAuthData("test@gmail.com")))
    val flickrUser = User(accounts = hashMapOf(flickr to OAuthData("test@yahoo.com")))
    users.add(googleUser)
    users.add(flickrUser)

    assertEquals(2, users.getAll()!!.size)

    var foundUGoogleUser = users.findByAccountId("test@gmail.com", google)
    assertNotNull(foundUGoogleUser)
    assertEquals(foundUGoogleUser!!.getId(), googleUser.getId())

    var foundFlickrUser = users.findByAccountId("test@yahoo.com", flickr)
    assertNotNull(foundFlickrUser)
    assertEquals(foundFlickrUser!!.getId(), flickrUser.getId())
  }
}