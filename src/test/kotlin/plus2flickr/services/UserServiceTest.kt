package plus2flickr.services

import org.mockito.Mockito.mock
import org.mockito.Mockito.`when` as whenMock
import org.mockito.Mockito.verify

import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import plus2flickr.thirdparty.CloudService
import plus2flickr.domain.User
import plus2flickr.repositories.UserRepository
import plus2flickr.thirdparty.AccountInfo
import kotlin.test.assertEquals
import plus2flickr.domain.ServiceType
import plus2flickr.thirdparty.AuthorizationException
import plus2flickr.thirdparty.AuthorizationError
import plus2flickr.thirdparty.OAuthToken
import plus2flickr.domain.UserInfo
import plus2flickr.domain.OAuthData
import plus2flickr.CloudServiceContainer
import kotlin.test.assertTrue

class UserServiceTest {

  var users: UserRepository? = null
  var userService: UserService? = null
  var googleService: CloudService? = null
  var flickrService: CloudService? = null

  var accountInfo = AccountInfo("1")
  var token = OAuthToken()
  var authCode = ""
  var verifier = ""

  BeforeTest fun setUp() {
    accountInfo = AccountInfo("1", email = "test@test.com", firstName = "First Name", lastName = "Last Name")
    token = OAuthToken()
    authCode = "testAuthCode"
    verifier = "testVerifier"

    users = mock(javaClass<UserRepository>())
    googleService = mock(javaClass<CloudService>())
    flickrService = mock(javaClass<CloudService>())
    val serviceContainer = CloudServiceContainer()
    serviceContainer.register(ServiceType.GOOGLE, googleService!!)
    serviceContainer.register(ServiceType.FLICKR, flickrService!!)
    userService = UserService(users!!, serviceContainer)
    whenMock(googleService!!.authorize(authCode))!!.thenReturn(token)
    whenMock(googleService!!.getAccountInfo(token))!!.thenReturn(accountInfo)
  }

  Test fun authorizeGoogleAccount_accountNotLinkedToOther_userIsPersisted() {
    val user = User()
    whenMock(users!!.findByAccountId(accountInfo.id, ServiceType.GOOGLE))!!.thenReturn(null)

    userService!!.authorizeGoogleAccount(user, authCode)

    verify(users)!!.update(user)
    assertTrue(user.accounts.containsKey(ServiceType.GOOGLE))
  }

  Test fun authorizeGoogleAccount_accountIsLinkedToExistingUser_accountsAreMerged() {
    val user = User()
    val existingUser = User()
    existingUser.setId("existing_user_id")
    existingUser.info = UserInfo("test", "user")
    existingUser.accounts.put(ServiceType.FLICKR, OAuthData())
    whenMock(users!!.findByAccountId(accountInfo.id, ServiceType.GOOGLE))!!.thenReturn(existingUser)

    userService!!.authorizeGoogleAccount(user, authCode)

    verify(users)!!.remove(existingUser)
    assertTrue(user.accounts.containsKey(ServiceType.FLICKR))
    assertTrue(user.accounts.containsKey(ServiceType.GOOGLE))
    assertEquals(existingUser.info, user.info)
  }

  Test fun authorizeGoogleAccount_userHasAccountOfSameType_exceptionThrown() {
    val user = User()
    user.accounts.put(ServiceType.GOOGLE, OAuthData())
    var error: AuthorizationError? = null
    try {
       userService!!.authorizeGoogleAccount(user, authCode)
    } catch(e: AuthorizationException) {
      error = e.error
    }
    assertEquals(error, AuthorizationError.DUPLICATED_ACCOUNT_TYPE)
  }

  Test fun authorizeFlickrAccount_accountNotLinkedToOther_userIsPersisted() {
    val user = User()
    user.flickrAuthorizationRequestSecret = "test"
    whenMock(flickrService!!.authorize(authCode, "test", verifier))!!.thenReturn(token)
    whenMock(flickrService!!.getAccountInfo(token))!!.thenReturn(accountInfo)
    whenMock(users!!.findByAccountId(accountInfo.id, ServiceType.FLICKR))!!.thenReturn(null)

    userService!!.authorizeFlickrAccount(user, authCode, verifier)

    verify(users)!!.update(user)
    assertTrue(user.accounts.containsKey(ServiceType.FLICKR))
  }
}