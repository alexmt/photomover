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
import plus2flickr.domain.OAuthToken
import com.google.api.client.auth.oauth2.TokenResponse
import com.google.inject.Inject

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

  override fun getUserInfo(token: OAuthToken): UserInfo {
    val info = token.toGoogleToken().buildCredential().buildOAuth().userinfo()!!.get()!!.execute()!!
    return UserInfo(
        firstName = info.getGivenName(),
        lastName = info.getFamilyName(),
        email = info.getEmail())
  }
}