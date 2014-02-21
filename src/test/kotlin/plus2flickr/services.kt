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
import plus2flickr.domain.AccountType
import plus2flickr.thirdparty.AuthorizationException
import plus2flickr.thirdparty.AuthorizationError
import plus2flickr.thirdparty.OAuthToken
import plus2flickr.domain.UserInfo
import plus2flickr.domain.OAuthData

class UserServiceTest {

  var users: UserRepository? = null
  var userService: UserService? = null
  var googleService: CloudService? = null

  var accountInfo = AccountInfo("1")
  var user = User()
  var token = OAuthToken()
  var authCode = ""

  BeforeTest fun setUp() {
    accountInfo = AccountInfo("1", email = "test@test.com", firstName = "First Name", lastName = "Last Name")
    user = User()
    token = OAuthToken()
    authCode = "testAuthCode"

    users = mock(javaClass<UserRepository>())
    googleService = mock(javaClass<CloudService>())
    userService = UserService(users!!, mapOf(AccountType.GOOGLE to googleService!!))
    whenMock(googleService!!.authorize(authCode))!!.thenReturn(token)
    whenMock(googleService!!.getAccountInfo(token))!!.thenReturn(accountInfo)
  }

  Test fun authorizeGoogleAccount_accountNotLinkedToOther_userIsPersisted() {
    whenMock(users!!.findByAccountId(accountInfo.id, AccountType.GOOGLE))!!.thenReturn(null)

    val resultUser = userService!!.authorizeGoogleAccount(user, authCode)

    verify(users)!!.update(user)
    assertEquals(user, resultUser)
    assertEquals(UserInfo(accountInfo.firstName, accountInfo.lastName, accountInfo.email), resultUser.info)
  }

  Test fun authorizeGoogleAccount_accountIsLinkedToExistingUser_currentAnonymousIsDeleted() {
    val existingUser = User()
    existingUser.setId("existing_user_id")
    whenMock(users!!.findByAccountId(accountInfo.id, AccountType.GOOGLE))!!.thenReturn(existingUser)

    val resultUser = userService!!.authorizeGoogleAccount(user, authCode)

    verify(users)!!.remove(user)
    assertEquals(existingUser, resultUser)
  }

  Test fun authorizeGoogleAccount_accountIsLinkedToExisting_exceptionThrown() {
    user.accounts.put(AccountType.FLICKR, OAuthData())
    val existingUser = User()
    existingUser.setId("existing_user_id")
    whenMock(users!!.findByAccountId(accountInfo.id, AccountType.GOOGLE))!!.thenReturn(existingUser)

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