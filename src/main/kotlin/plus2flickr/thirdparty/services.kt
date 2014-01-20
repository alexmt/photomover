package plus2flickr.thirdparty

import plus2flickr.domain.OAuthToken

trait CloudService {
  fun authorize(code: String): OAuthToken
  fun getUserInfo(token: OAuthToken): UserInfo
}

enum class AuthorizationError {
  SERVER_ERROR
  INVALID_CLIENT_ID
  INVALID_AUTHORIZATION_CODE
}

data class UserInfo(
    var firstName: String? = null, var lastName: String? = null, var email: String? = null) {
}

class AuthorizationException(val error: AuthorizationError, val message: String = "") : Exception() {

}
