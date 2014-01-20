package plus2flickr.thirdparty

trait CloudService {
  fun authorize(code: String): String
  fun getUserInfo(token: String): UserInfo
}

enum class AuthorizationError {
  SERVER_ERROR
  INVALID_CLIENT_ID
  INVALID_AUTHORIZATION_CODE
}

data class UserInfo(val firstName: String = "", val lastName: String = "", val email: String = "") {
}

class AuthorizationException(val error: AuthorizationError, val message: String = "") : Exception() {

}
