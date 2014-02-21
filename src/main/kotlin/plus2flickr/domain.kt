package plus2flickr.domain

import org.ektorp.support.CouchDbDocument
import plus2flickr.thirdparty.AccountInfo
import plus2flickr.thirdparty.OAuthToken

enum class AccountType(val id: Int) {
  GOOGLE: AccountType(1)
  FLICKR: AccountType(2)
}

data class UserInfo(var firstName: String? = null, var lastName: String? = null, var email: String? = null)
data class OAuthData(var id: String = "", var token: OAuthToken = OAuthToken())
data class User(
    var accounts: MutableMap<AccountType, OAuthData> = hashMapOf(), var info: UserInfo = UserInfo()) : CouchDbDocument()
