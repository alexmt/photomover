package photomover.thirdparty.google

import photomover.thirdparty.CloudService
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.services.oauth2.Oauth2
import photomover.thirdparty.AuthorizationError
import photomover.thirdparty.AuthorizationException
import com.google.api.client.auth.oauth2.TokenResponseException
import photomover.thirdparty.AccountInfo
import com.google.api.client.auth.oauth2.TokenResponse
import com.google.inject.Inject
import photomover.thirdparty.OAuthToken
import photomover.thirdparty.Album
import com.google.gdata.client.photos.PicasawebService
import com.google.gdata.data.photos.UserFeed
import java.net.URL
import photomover.thirdparty.AuthorizationRequest
import photomover.thirdparty.ImageSize
import photomover.thirdparty.Photo
import com.google.gdata.data.photos.PhotoFeed
import com.google.gdata.data.photos.AlbumFeed
import com.google.gdata.util.ServiceForbiddenException
import photomover.thirdparty.InvalidTokenException
import com.google.api.client.auth.oauth2.RefreshTokenRequest
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants
import com.google.api.client.http.GenericUrl
import com.google.api.client.auth.oauth2.ClientParametersAuthentication
import com.google.gdata.data.photos.PhotoEntry
import photomover.thirdparty.AlbumInfo

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

  private fun PhotoEntry.getPhotoUrl(size: ImageSize): String =
      when (size) {
        ImageSize.THUMB -> this.getMediaThumbnails()!!.maxBy { it.getHeight() }!!.getUrl()!!
        ImageSize.LARGE -> this.getMediaContents()!!.maxBy { it.getHeight() }!!.getUrl()!!
        else -> throw IllegalArgumentException("Size '$size' is not supported.")
      }

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

  override fun getAlbumInfo(userId: String, token: OAuthToken, albumId: String): AlbumInfo {
    return token.callPicasa {
      val feedUrl = URL("https://picasaweb.google.com/data/feed/api/user/$userId/albumid/$albumId")
      val info = it.getFeed(feedUrl, javaClass<AlbumFeed>())!!
      AlbumInfo(name = info.getTitle()!!.getPlainText()!!, photoCount = info.getPhotoEntries()!!.size)
    }
  }

  override fun getAlbumPhotos(userId: String, token: OAuthToken, albumId: String): List<Photo> {
    return token.callPicasa {
      val feedUrl = URL("https://picasaweb.google.com/data/feed/api/user/$userId/albumid/$albumId")
      it.getFeed(feedUrl, javaClass<AlbumFeed>())!!.getPhotoEntries()!!.map {
        Photo(
            id = it.getId()!!,
            name = it.getTitle()?.getPlainText() ?: "",
            thumbUrl = it.getPhotoUrl(ImageSize.THUMB),
            largeUrl = it.getPhotoUrl(ImageSize.LARGE)
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
