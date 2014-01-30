package plus2flickr.web.models

data class UserInfoViewModel(var name: String = "")

data class GoogleAppSettingsViewModel(var clientId: String, var scopes: List<String>)

data class OperationResponse<T>(
    var success: Boolean = true,
    var errorMessage: String = "",
    var data: T? = null) {
}
