package plus2flickr.web.models

data class UserInfoViewModel(var name: String = "", var accountsState: Map<String, Boolean> = mapOf()) {
  val isAnonymous: Boolean
    get() = !accountsState.values().any { it }
}

data class GoogleAppSettingsViewModel(var clientId: String, var scopes: List<String>)

data class OperationResponse<T>(
    var success: Boolean = true,
    var errorMessage: String = "",
    var data: T? = null)

