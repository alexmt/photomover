package plus2flickr.thirdparty.flickr

import plus2flickr.thirdparty.CloudService
import plus2flickr.thirdparty.OAuthToken
import plus2flickr.thirdparty.AccountInfo
import plus2flickr.thirdparty.Album
import com.google.inject.Inject
import org.scribe.builder.api.FlickrApi
import org.scribe.builder.ServiceBuilder
import org.scribe.oauth.OAuthService
import com.google.common.base.Strings
import plus2flickr.thirdparty.AuthorizationRequest
import org.scribe.model.Token
import org.scribe.model.Verifier
import org.scribe.model.OAuthRequest
import org.scribe.model.Verb
import java.io.InputStream
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.annotate.JsonIgnoreProperties
import org.codehaus.jackson.annotate.JsonProperty

data class FlickrAppSettings(var apiKey: String = "", var apiSecret: String = "")
data class FlickrString(JsonProperty("_content") var content: String = "")
JsonIgnoreProperties (ignoreUnknown=true) data class FlickrAccount(var id: String = "")
JsonIgnoreProperties(ignoreUnknown=true) data class Person(
    JsonProperty("id") var id: String = "", JsonProperty("realname") var realName: FlickrString = FlickrString())

class FlickrService[Inject](val appSettings: FlickrAppSettings) : CloudService {

  private fun call<T>(
      clazz: Class<T>, method: String,
      accessToken: OAuthToken,
      node: String? = null, params: Map<String, String> = mapOf()): T {
    val request = OAuthRequest(Verb.GET, "http://ycpi.api.flickr.com/services/rest");
    request.addQuerystringParameter("format", "json");
    request.addQuerystringParameter("nojsoncallback", "1");
    request.addQuerystringParameter("method", method);
    for (param in params.entrySet()) {
      request.addQuerystringParameter(param.getKey(), param.getValue());
    }
    getService().signRequest(Token(accessToken.accessToken, accessToken.oauth1TokenSecret), request)
    return request.send()!!.getStream()!!.use {
      var responseText = it.reader().readText()
      val mapper = ObjectMapper()
      if (!Strings.isNullOrEmpty(node)) {
        val treeNode = mapper.readTree(responseText)!!.get(node)
        responseText = treeNode!!.toString()!!
      }
      ObjectMapper().readValue(responseText, clazz)!!
    }
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
    val account = call(javaClass<FlickrAccount>(), "flickr.test.login", token, node = "user")
    val person = call(
        javaClass<Person>(), "flickr.people.getInfo", token, node = "person", params = mapOf("user_id" to account.id))
    val info = AccountInfo(person.id)
    val nameParts = person.realName.content.split(" ")
    if (nameParts.size > 0) {
      info.firstName = nameParts[0]
    }
    if (nameParts.size > 1) {
      info.lastName = nameParts[1]
    }
    return info
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