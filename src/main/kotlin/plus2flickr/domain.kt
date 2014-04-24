package plus2flickr.domain

import org.ektorp.support.CouchDbDocument
import plus2flickr.thirdparty.OAuthToken

data class UserInfo(var firstName: String? = null, var lastName: String? = null, var email: String? = null)
data class OAuthData(var id: String = "", var token: OAuthToken = OAuthToken(), var isTokenNeedRefresh: Boolean = true)
data class User(
    var info: UserInfo = UserInfo(),
    /**
     * Contains OAuth 1.0/2.0 authentication key per service type.
     */
    var accounts: MutableMap<String, OAuthData> = hashMapOf(),
    /**
     * Contains OAuth 1.0 authorization request secrets per service type.
     */
    var oauthRequestSecret: MutableMap<String, String> = hashMapOf()) : CouchDbDocument()

data class OperationError<T>(val error: T, val message: String = "")

class ServiceOperationErrorException<T>(val errors: List<OperationError<T>>) : Exception() {

  class object {
    fun create<T>(vararg errors: OperationError<T>): ServiceOperationErrorException<T> {
      return ServiceOperationErrorException(errors.toList())
    }
  }
}