package plus2flickr.thirdparty

class AuthorizationException(val error: AuthorizationError, val message: String = "") : Exception()

enum class AuthorizationError {
  SERVER_ERROR
  INVALID_CLIENT_ID
  INVALID_AUTHORIZATION_CODE
  ACCOUNT_LINKED_TO_OTHER_USER
  DUPLICATED_ACCOUNT_TYPE
}

data class AccountInfo(
    var id: String, var firstName: String? = null, var lastName: String? = null, var email: String? = null)
data class OAuthToken(
    var accessToken: String = "", var refreshToken: String? = null, var oauth1TokenSecret: String? = null)
data class Album(var name: String = "", var thumbnailUrl: String = "")
data class AuthorizationRequest(val url : String, val secret: String)

trait OAuth1Service {
  fun authorize(token: String, requestSecret: String, verifier: String): OAuthToken
  fun requestAuthorization(callback: String): AuthorizationRequest
}

trait OAuth2Service {
  fun authorize(code: String): OAuthToken
}

trait CloudService : OAuth1Service, OAuth2Service {
  fun getAccountInfo(token: OAuthToken): AccountInfo
  fun getAlbums(userId: String, token: OAuthToken): List<Album>
}
