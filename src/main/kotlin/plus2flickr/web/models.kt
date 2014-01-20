package plus2flickr.web.models

data class UserInfo(var firstName: String = "", var lastName: String = "")

data class OperationResponse<T>(
    var success : Boolean = true,
    var errorMessage: String = "",
    var data : T? = null) {
}
