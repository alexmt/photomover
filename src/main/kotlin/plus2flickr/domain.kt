package plus2flickr.domain

import org.ektorp.support.CouchDbDocument
import plus2flickr.thirdparty.UserInfo

enum class AccountType(val id: Int) {
  GOOGLE: AccountType(1)
  FLICKR: AccountType(2)
}

data class OAuthToken(var accessToken: String = "", var refreshToken: String? = null) {
}

data class OAuthData(var id: String = "", var token: OAuthToken = OAuthToken()) {
}

data class User(
    var accounts: MutableMap<AccountType, OAuthData> = hashMapOf(),
    var info: UserInfo = UserInfo()) : CouchDbDocument() {

  fun enrichUserInfo(userInfo: UserInfo) {
    info.email = info.email ?: userInfo.email
    info.firstName = info.firstName ?: userInfo.firstName
    info.lastName = info.lastName ?: userInfo.lastName
  }
}
