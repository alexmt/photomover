package plus2flickr.domain

import org.ektorp.support.CouchDbDocument

enum class AccountType(val id: Int) {
  GOOGLE: AccountType(1)
}

data class OAuthAccount(var token: String) {}

data class User(
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "") : CouchDbDocument() {
  var accounts: MutableMap<AccountType, OAuthAccount> = hashMapOf()
}
