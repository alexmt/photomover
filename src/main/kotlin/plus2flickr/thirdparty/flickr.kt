package plus2flickr.thirdparty.flickr

import plus2flickr.thirdparty.CloudService
import plus2flickr.thirdparty.OAuthToken
import plus2flickr.thirdparty.AccountInfo
import plus2flickr.thirdparty.Album
import org.scribe.builder.api.FlickrApi
import org.scribe.builder.ServiceBuilder
import org.scribe.oauth.OAuthService
import com.google.common.base.Strings
import plus2flickr.thirdparty.AuthorizationRequest
import org.scribe.model.Token
import org.scribe.model.Verifier
import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.REST
import com.flickr4java.flickr.auth.Auth
import com.flickr4java.flickr.auth.Permission
import com.flickr4java.flickr.RequestContext
import plus2flickr.thirdparty.ImageSize
import plus2flickr.thirdparty.UrlResolver
import com.flickr4java.flickr.photos.Size
import plus2flickr.thirdparty.Photo
import com.flickr4java.flickr.FlickrException
import plus2flickr.thirdparty.InvalidTokenException

data class FlickrAppSettings(var apiKey: String = "", var apiSecret: String = "")

class FlickrService(val appSettings: FlickrAppSettings, val urlResolver: UrlResolver) : CloudService {

  val imageSizeToFlickrSize = mapOf(
      ImageSize.THUMB to Size.THUMB,
      ImageSize.LARGE to Size.LARGE)

  private fun getService(callback: String = "") : OAuthService {
    val builder = ServiceBuilder()
        .provider(javaClass<FlickrApi>())!!
        .apiKey(appSettings.apiKey)!!
        .apiSecret(appSettings.apiSecret)!!
    if (!Strings.isNullOrEmpty(callback)) {
      builder.callback(callback)!!
    }
    return builder.build()!!
  }

  private fun OAuthToken.callFlickr<T>(action: (Flickr)->T): T {
    try {
      val flickr = Flickr(appSettings.apiKey, appSettings.apiSecret, REST())
      val auth = Auth()
      auth.setPermission(Permission.DELETE)
      auth.setToken(this.accessToken)
      auth.setTokenSecret(this.oauth1TokenSecret)
      RequestContext.getRequestContext()!!.setAuth(auth)
      flickr.setAuth(auth)
      return action(flickr)
    } catch (ex: FlickrException) {
      if (ex.getErrorCode() == "98") {
        throw InvalidTokenException(ex)
      }
      throw ex
    }
  }

  override fun requestAuthorization(callback: String): AuthorizationRequest {
    val service = getService(callback)
    val requestToken = service.getRequestToken()!!
    val authorizationUrl = service.getAuthorizationUrl(requestToken)!! + "&perms=delete"
    return AuthorizationRequest(authorizationUrl, requestToken.getSecret()!!)
  }

  override fun authorize(code: String): OAuthToken {
    throw UnsupportedOperationException()
  }

  override fun authorize(token: String, requestSecret: String, verifier: String): OAuthToken {
    val requestToken = Token(token, requestSecret)
    val accessToken = getService().getAccessToken(requestToken, Verifier(verifier))!!
    return OAuthToken(accessToken = accessToken.getToken()!!, oauth1TokenSecret = accessToken.getSecret())
  }

  override fun getAccountInfo(token: OAuthToken): AccountInfo {
    return token.callFlickr {
      val user = it.getTestInterface()!!.login()!!
      val accountInfo = AccountInfo(user.getId()!!)
      val userInfo = it.getPeopleInterface()!!.getInfo(user.getId())!!
      val nameParts = userInfo.getRealName()!!.split(" ")
      if (nameParts.size > 0) {
        accountInfo.firstName = nameParts[0]
      }
      if (nameParts.size > 1) {
        accountInfo.lastName = nameParts[1]
      }
      accountInfo
    }
  }

  override fun getAlbums(userId: String, token: OAuthToken): List<Album> {
    return token.callFlickr {
      it.getPhotosetsInterface()!!.getList(userId)!!.getPhotosets()!!.map {
        Album(
            id = it.getId()!!,
            name = it.getTitle()!!,
            thumbnailUrl = urlResolver.getPhotoRedirectUrl(it.getPrimaryPhoto()!!.getId()!!, ImageSize.THUMB))
      }
    }
  }

  override fun getPhotos(userId: String, token: OAuthToken, albumId: String, size: ImageSize): List<Photo> {
    return token.callFlickr {
      it.getPhotosetsInterface()!!.getPhotos(albumId, 0, 0)!!.map {
        Photo(
            id = it.getId()!!,
            name = it.getTitle()!!,
            url = urlResolver.getPhotoRedirectUrl(it.getId()!!, size))
      }
    }
  }

  override fun getPhotoUrl(id: String, size: ImageSize, token: OAuthToken): String {
    return token.callFlickr {
      val photosService = it.getPhotosInterface()!!
      val requiredSize = imageSizeToFlickrSize[size]
      val sizes = photosService.getSizes(id)!!
      val imgSize = sizes.filter { it.getLabel() == requiredSize }.firstOrNull() ?: sizes.first()
      imgSize.getSource()!!
    }
  }
}