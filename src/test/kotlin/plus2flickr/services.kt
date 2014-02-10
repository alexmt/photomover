package plus2flickr.services

import org.mockito.Mockito.mock
import org.mockito.Mockito.`when` as whenMock
import org.mockito.Mockito.verify

import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import plus2flickr.thirdparty.CloudService
import plus2flickr.domain.User
import plus2flickr.repositories.UserRepository
import org.mockito.Mockito
import plus2flickr.domain.OAuthToken
import plus2flickr.thirdparty.UserInfo
import kotlin.test.assertEquals
import plus2flickr.domain.AccountType
import plus2flickr.domain.OAuthData
import plus2flickr.thirdparty.AuthorizationException
import plus2flickr.thirdparty.AuthorizationError

class UserServiceTest {

  var users: UserRepository? = null
  var userService: UserService? = null
  var googleService: CloudService? = null

  var userInfo = UserInfo()
  var user = User()
  var token = OAuthToken()
  var authCode = ""

  BeforeTest fun setUp() {
    userInfo = UserInfo(email = "test@test.com", firstName = "First Name", lastName = "Last Name")
    user = User()
    token = OAuthToken()
    authCode = "testAuthCode"

    users = mock(javaClass<UserRepository>())
    googleService = mock(javaClass<CloudService>())
    userService = UserService(users!!, googleService!!)
    whenMock(googleService!!.authorize(authCode))!!.thenReturn(token)
    whenMock(googleService!!.getUserInfo(token))!!.thenReturn(userInfo)
  }

  Test fun authorizeGoogleAccount_accountNotLinkedToOther_userIsPersisted() {
    whenMock(users!!.findByAccountId(userInfo.email!!, AccountType.GOOGLE))!!.thenReturn(null)

    val resultUser = userService!!.authorizeGoogleAccount(user, authCode)

    verify(users)!!.update(user)
    assertEquals(user, resultUser)
    assertEquals(resultUser.info, userInfo)
  }

  Test fun authorizeGoogleAccount_accountIsLinkedToExistingUser_currentAnonymousIsDeleted() {
    val existingUser = User()
    existingUser.setId("existing_user_id")
    whenMock(users!!.findByAccountId(userInfo.email!!, AccountType.GOOGLE))!!.thenReturn(existingUser)

    val resultUser = userService!!.authorizeGoogleAccount(user, authCode)

    verify(users)!!.remove(user)
    assertEquals(existingUser, resultUser)
  }

  Test fun authorizeGoogleAccount_accountIsLinkedToExisting_exceptionThrown() {
    user.accounts.put(AccountType.FLICKR, OAuthData())
    val existingUser = User()
    existingUser.setId("existing_user_id")
    whenMock(users!!.findByAccountId(userInfo.email!!, AccountType.GOOGLE))!!.thenReturn(existingUser)

    var error: AuthorizationError? = null
    try {
      userService!!.authorizeGoogleAccount(user, authCode)
    } catch(e: AuthorizationException) {
      error = e.error
    }
    assertEquals(error, AuthorizationError.ACCOUNT_LINKED_TO_OTHER_USER)
  }

  Test fun authorizeGoogleAccount_userHasAccountOfSameType_exceptionThrown() {
    user.accounts.put(AccountType.GOOGLE, OAuthData())
    var error: AuthorizationError? = null
    try {
       userService!!.authorizeGoogleAccount(user, authCode)
    } catch(e: AuthorizationException) {
      error = e.error
    }
    assertEquals(error, AuthorizationError.DUPLICATED_ACCOUNT_TYPE)
  }

}