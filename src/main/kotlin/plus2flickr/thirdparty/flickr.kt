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
import com.google.inject.Inject
import com.flickr4java.flickr.auth.Permission
import com.flickr4java.flickr.RequestContext

data class FlickrAppSettings(var apiKey: String = "", var apiSecret: String = "")

class FlickrService[Inject](val appSettings: FlickrAppSettings) : CloudService {

  private fun OAuthToken.createFlickr(): Flickr {
    val flickr = Flickr(appSettings.apiKey, appSettings.apiSecret, REST())
    val auth = Auth()
    auth.setPermission(Permission.DELETE)
    auth.setToken(this.accessToken)
    auth.setTokenSecret(this.oauth1TokenSecret)
    RequestContext.getRequestContext()!!.setAuth(auth)
    flickr.setAuth(auth)
    return flickr
  }

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

  override fun authorize(token: String, requestSecret: String, verifier: String): OAuthToken {
    val requestToken = Token(token, requestSecret)
    val accessToken = getService().getAccessToken(requestToken, Verifier(verifier))!!
    return OAuthToken(accessToken = accessToken.getToken()!!, oauth1TokenSecret = accessToken.getSecret())
  }

  override fun getAccountInfo(token: OAuthToken): AccountInfo {
    val flickr = token.createFlickr()
    val user = flickr.getTestInterface()!!.login()!!
    val accountInfo = AccountInfo(user.getId()!!)
    val userInfo = flickr.getPeopleInterface()!!.getInfo(user.getId())!!
    val nameParts = userInfo.getRealName()!!.split(" ")
    if (nameParts.size > 0) {
      accountInfo.firstName = nameParts[0]
    }
    if (nameParts.size > 1) {
      accountInfo.lastName = nameParts[1]
    }
    return accountInfo
  }

  override fun getAlbums(userId: String, token: OAuthToken): List<Album> {
    throw UnsupportedOperationException()
  }

  override fun requestAuthorization(callback: String): AuthorizationRequest {
    val service = getService(callback)
    val requestToken = service.getRequestToken()!!
    return AuthorizationRequest(service.getAuthorizationUrl(requestToken)!!, requestToken.getSecret()!!)
  }

  override fun authorize(code: String): OAuthToken {
    throw UnsupportedOperationException()
  }
}