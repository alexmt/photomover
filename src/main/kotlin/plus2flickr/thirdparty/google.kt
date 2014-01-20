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
import plus2flickr.thirdparty.UserInfo
import com.google.inject.Inject

data class GoogleAppSettings(
    var clientId: String = "",
    var clientSecret: String = "",
    val applicationName : String = "") {
}

class GoogleService[Inject](
    val transport: HttpTransport,
    val jsonFactory: JsonFactory,
    val settings: GoogleAppSettings) : CloudService {

  override fun authorize(code: String): String {
    try {
      val tokenResponse = GoogleAuthorizationCodeTokenRequest(transport, jsonFactory,
          settings.clientId, settings.clientSecret, code, "postmessage").execute()
      val credential = GoogleCredential.Builder()
          .setJsonFactory(jsonFactory)!!
          .setTransport(transport)!!
          .setClientSecrets(settings.clientId, settings.clientSecret)!!
          .build()!!
          .setFromTokenResponse(tokenResponse)!!

      val oauth2 = Oauth2.Builder(
          transport, jsonFactory, credential).build()!!
      val tokenInfo = oauth2.tokeninfo()!!
          .setAccessToken(credential.getAccessToken())!!.execute()!!
      if (tokenInfo.containsKey("error")) {
        throw AuthorizationException(
            AuthorizationError.SERVER_ERROR, tokenInfo.get("error").toString())
      }
      if (!tokenInfo.getIssuedTo().equals(settings.clientId)) {
        throw AuthorizationException(AuthorizationError.INVALID_CLIENT_ID)
      }
      return tokenResponse.toString()
    } catch (e: TokenResponseException) {
      throw AuthorizationException(AuthorizationError.SERVER_ERROR, e.getDetails().toString())
    }
  }

  override fun getUserInfo(token: String): UserInfo {
    return UserInfo()
  }
}
