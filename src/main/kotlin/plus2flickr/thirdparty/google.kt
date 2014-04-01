package plus2flickr.thirdparty.google

import plus2flickr.thirdparty.CloudService
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.services.oauth2.Oauth2
import plus2flickr.thirdparty.AuthorizationError
import plus2flickr.thirdparty.AuthorizationException
import com.google.api.client.auth.oauth2.TokenResponseException
import plus2flickr.thirdparty.AccountInfo
import com.google.api.client.auth.oauth2.TokenResponse
import com.google.inject.Inject
import plus2flickr.thirdparty.OAuthToken
import plus2flickr.thirdparty.Album
import com.google.gdata.client.photos.PicasawebService
import com.google.gdata.data.photos.UserFeed
import java.net.URL
import plus2flickr.thirdparty.AuthorizationRequest
import plus2flickr.thirdparty.ImageSize
import plus2flickr.thirdparty.Photo
import com.google.gdata.data.photos.PhotoFeed
import com.google.gdata.data.photos.AlbumFeed
import com.google.gdata.util.ServiceForbiddenException
import plus2flickr.thirdparty.InvalidTokenException
import com.google.api.client.auth.oauth2.RefreshTokenRequest
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants
import com.google.api.client.http.GenericUrl
import com.google.api.client.auth.oauth2.ClientParametersAuthentication

data class GoogleAppSettings(
    var clientId: String = "",
    var clientSecret: String = "",
    var applicationName: String = "",
    var scopes: List<String> = listOf()) {
}

class GoogleService[Inject](
    val transport: HttpTransport,
    val jsonFactory: JsonFactory,
    val settings: GoogleAppSettings) : CloudService {

  private fun OAuthToken.toGoogleToken(): TokenResponse {
    val response = TokenResponse()
    response.setAccessToken(accessToken)
    response.setRefreshToken(refreshToken)
    return response
  }

  private fun OAuthToken.callPicasa<T>(action: (PicasawebService)->T): T {
    val service = PicasawebService(settings.applicationName)
    service.setAuthSubToken(this.accessToken, null)
    try {
      return action(service)
    } catch (ex: ServiceForbiddenException) {
      throw InvalidTokenException(ex)
    }
  }

  private fun TokenResponse.buildCredential(): GoogleCredential = GoogleCredential.Builder()
      .setJsonFactory(jsonFactory)!!
      .setTransport(transport)!!
      .setClientSecrets(settings.clientId, settings.clientSecret)!!
      .build()!!
      .setFromTokenResponse(this)!!

  private fun GoogleCredential.buildOAuth(): Oauth2 = Oauth2.Builder(
      transport, jsonFactory, this).build()!!

  override fun authorize(code: String): OAuthToken {
    try {
      val tokenResponse = GoogleAuthorizationCodeTokenRequest(transport, jsonFactory,
          settings.clientId, settings.clientSecret, code, "postmessage").execute()!!
      val credential = tokenResponse.buildCredential()
      val tokenInfo = credential.buildOAuth().tokeninfo()!!
          .setAccessToken(credential.getAccessToken())!!.execute()!!
      if (tokenInfo.containsKey("error")) {
        throw AuthorizationException(
            AuthorizationError.SERVER_ERROR, tokenInfo.get("error").toString())
      }
      if (!tokenInfo.getIssuedTo().equals(settings.clientId)) {
        throw AuthorizationException(AuthorizationError.INVALID_CLIENT_ID)
      }
      return OAuthToken(
          accessToken = tokenResponse.getAccessToken()!!,
          refreshToken = tokenResponse.getRefreshToken())
    } catch (e: TokenResponseException) {
      throw AuthorizationException(AuthorizationError.SERVER_ERROR, e.toString())
    }
  }

  override fun getAccountInfo(token: OAuthToken): AccountInfo {
    return token.callPicasa {
      val info = token.toGoogleToken().buildCredential().buildOAuth().userinfo()!!.get()!!.execute()!!
      AccountInfo(
          info.getId()!!,
          firstName = info.getGivenName(),
          lastName = info.getFamilyName(),
          email = info.getEmail())
    }
  }

  override fun getAlbums(userId: String, token: OAuthToken): List<Album> {
    return token.callPicasa {
      val feedUrl = URL("https://picasaweb.google.com/data/feed/api/user/$userId?kind=album")
      it.getFeed(feedUrl, javaClass<UserFeed>())!!.getAlbumEntries()!!.map {
        Album(
            id = it.getGphotoId()!!,
            name = it.getTitle()!!.getPlainText()!!,
            thumbnailUrl = it.getMediaGroup()!!.getThumbnails()!!.first!!.getUrl()!! )
      }
    }
  }

  override fun getPhotos(userId: String, token: OAuthToken, albumId: String, size: ImageSize): List<Photo> {
    return token.callPicasa {
      val feedUrl = URL("https://picasaweb.google.com/data/feed/api/user/$userId/albumid/$albumId")
      it.getFeed(feedUrl, javaClass<AlbumFeed>())!!.getPhotoEntries()!!.map {
        Photo(
            url = it.getMediaThumbnails()!!.maxBy { it.getHeight() }!!.getUrl()!!
        )
      }
    }
  }

  override fun refreshAccessToken(refreshToken: String): String {
    val response = RefreshTokenRequest(transport, jsonFactory, GenericUrl(GoogleOAuthConstants.TOKEN_SERVER_URL), refreshToken)
        .setClientAuthentication(ClientParametersAuthentication(settings.clientId, settings.clientSecret))!!
        .execute()!!
    return response.getAccessToken()!!
  }

  override fun authorize(token: String, requestSecret: String, verifier: String): OAuthToken {
    throw UnsupportedOperationException()
  }

  override fun getPhotoUrl(id: String, size: ImageSize, token: OAuthToken): String {
    throw UnsupportedOperationException()
  }

  override fun requestAuthorization(callback: String): AuthorizationRequest {
    throw UnsupportedOperationException()
  }
}
