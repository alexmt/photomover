package plus2flickr.thirdparty

class AuthorizationException(val error: AuthorizationError, val message: String = "") : Exception()
class InvalidTokenException(cause: Throwable? = null): Exception(cause)

enum class AuthorizationError {
  SERVER_ERROR
  INVALID_CLIENT_ID
  INVALID_AUTHORIZATION_CODE
  DUPLICATED_ACCOUNT_TYPE
}

data class AccountInfo(
    var id: String, var firstName: String? = null, var lastName: String? = null, var email: String? = null)
data class OAuthToken(
    var accessToken: String = "", var refreshToken: String? = null, var oauth1TokenSecret: String? = null)
data class Album(var id: String = "", var name: String = "", var thumbnailUrl: String = "")
data class Photo(var id: String = "", var name: String = "", var thumbUrl: String = "", var largeUrl: String = "")
data class AuthorizationRequest(val url : String, val secret: String)

enum class ImageSize {
  THUMB
  LARGE
}

trait UrlResolver {
  fun getPhotoRedirectUrl(id: String, size: ImageSize): String
}

trait OAuth1Service {
  fun authorize(token: String, requestSecret: String, verifier: String): OAuthToken
  fun requestAuthorization(callback: String): AuthorizationRequest
}

trait OAuth2Service {
  fun authorize(code: String): OAuthToken
  fun refreshAccessToken(refreshToken: String): String
}

trait CloudService : OAuth1Service, OAuth2Service {
  fun getAccountInfo(token: OAuthToken): AccountInfo
  fun getAlbums(userId: String, token: OAuthToken): List<Album>
  fun getPhotos(userId: String, token: OAuthToken, albumId: String): List<Photo>
  fun getPhotoUrl(id: String, size: ImageSize, token: OAuthToken): String
}
