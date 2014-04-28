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
import com.flickr4java.flickr.photos.Size
import plus2flickr.thirdparty.Photo
import com.flickr4java.flickr.FlickrException
import plus2flickr.thirdparty.InvalidTokenException
import plus2flickr.thirdparty.AlbumInfo
import com.flickr4java.flickr.photosets.Photoset
import com.flickr4java.flickr.photos.Photo as FlickrPhoto

data class FlickrAppSettings(var apiKey: String = "", var apiSecret: String = "")

class FlickrService(val appSettings: FlickrAppSettings) : CloudService {

  data class SizeInfo(val label: Int, val postfix: String)

  val imageSizeToFlickrSize = mapOf(
      ImageSize.THUMB to SizeInfo(Size.THUMB, "q"),
      ImageSize.LARGE to SizeInfo(Size.LARGE, "b"))

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

  private fun resolvePhotoUrl(farm: String?, server: String?, id: String?, secret: String?, size: ImageSize): String {
    val postfix = imageSizeToFlickrSize[size]!!.postfix
    return "http://farm$farm.staticflickr.com/$server/${id}_${secret}_${postfix}.jpg"
  }

  private fun FlickrPhoto.getPhotoUrl(photoId: String, size: ImageSize): String {
    return resolvePhotoUrl(getFarm(), getServer(), photoId, getSecret(), size)
  }

  private fun Photoset.getPhotoUrl(photoId: String, size: ImageSize): String {
    return resolvePhotoUrl(getFarm(), getServer(), photoId, getSecret(), size)
  }

  override fun getAlbums(userId: String, token: OAuthToken): List<Album> {
    return token.callFlickr {
      it.getPhotosetsInterface()!!.getList(userId)!!.getPhotosets()!!.map {
        Album(
            id = it.getId()!!,
            name = it.getTitle()!!,
            thumbnailUrl = it.getPhotoUrl(it.getPrimaryPhoto()!!.getId()!!, ImageSize.THUMB))
      }
    }
  }

  override fun getAlbumInfo(userId: String, token: OAuthToken, albumId: String): AlbumInfo {
    return token.callFlickr {
      val info = it.getPhotosetsInterface()!!.getInfo(albumId)!!
      AlbumInfo(name = info.getTitle()!!, photoCount = info.getPhotoCount())
    }
  }

  override fun getAlbumPhotos(userId: String, token: OAuthToken, albumId: String): List<Photo> {
    return token.callFlickr {
      it.getPhotosetsInterface()!!.getPhotos(albumId, 0, 0)!!.map {
        Photo(
            id = it.getId()!!,
            name = it.getTitle()!!,
            thumbUrl = it.getPhotoUrl(it.getId()!!, ImageSize.THUMB),
            largeUrl = it.getPhotoUrl(it.getId()!!, ImageSize.LARGE))
      }
    }
  }

  override fun getPhotoUrl(id: String, size: ImageSize, token: OAuthToken): String {
    return token.callFlickr {
      val photosService = it.getPhotosInterface()!!
      val requiredSize = imageSizeToFlickrSize[size]!!
      val sizes = photosService.getSizes(id)!!
      val imgSize = sizes.filter { it.getLabel() == requiredSize.label }.firstOrNull() ?: sizes.first()
      imgSize.getSource()!!
    }
  }

  override fun authorize(code: String): OAuthToken {
    throw UnsupportedOperationException()
  }

  override fun refreshAccessToken(refreshToken: String): String {
    throw UnsupportedOperationException()
  }
}
