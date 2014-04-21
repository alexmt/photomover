package plus2flickr.domain

import org.ektorp.support.CouchDbDocument
import plus2flickr.thirdparty.OAuthToken

enum class ServiceType(val id: Int, val isOAuth2: Boolean) {
  GOOGLE: ServiceType(1, true)
  FLICKR: ServiceType(2, false)
}

data class UserInfo(var firstName: String? = null, var lastName: String? = null, var email: String? = null)
data class OAuthData(var id: String = "", var token: OAuthToken = OAuthToken(), var isTokenNeedRefresh: Boolean = true)
data class User(
    var accounts: MutableMap<ServiceType, OAuthData> = hashMapOf(),
    var info: UserInfo = UserInfo(),
    var flickrAuthorizationRequestSecret: String? = null) : CouchDbDocument()

data class OperationError<T>(val error: T, val message: String = "")

class ServiceOperationErrorException<T>(val errors: List<OperationError<T>>) : Exception() {

  class object {
    fun create<T>(vararg errors: OperationError<T>): ServiceOperationErrorException<T> {
      return ServiceOperationErrorException(errors.toList())
    }
  }
}