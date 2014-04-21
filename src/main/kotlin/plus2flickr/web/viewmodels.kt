package plus2flickr.web.models

data class UserInfoViewModel(var name: String = "", var accountsState: Map<String, Boolean> = mapOf()) {
  val isAnonymous: Boolean
    get() = !accountsState.values().any { it }
}

data class DetailedUserInfoViewModel(
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var linkedServices: List<String> = listOf())

data class GoogleAppSettingsViewModel(var clientId: String, var scopes: List<String>)

data class ServiceAlbumInput(var albumId: String = "", var service: String = "")

data class ErrorInfo(var error: String = "", var message: String = "")

open data class OperationResponse<T>(
    var success: Boolean = true,
    var errors: List<ErrorInfo> = listOf(),
    var data: T? = null)

data class NoDataOperationResponse(success: Boolean = true, errors: List<ErrorInfo> = listOf())
  : OperationResponse<Any>(success, errors, null)