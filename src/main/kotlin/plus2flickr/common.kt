package plus2flickr

import plus2flickr.thirdparty.CloudService
import plus2flickr.thirdparty.UrlResolver
import plus2flickr.thirdparty.ImageSize

class CloudServiceContainer {

  private val codeToService = hashMapOf<String, CloudService>()

  fun register(serviceCode: String, service: CloudService) {
    codeToService.put(serviceCode, service)
  }

  fun get(code: String): CloudService {
    val service = codeToService[code]
    if (service == null) {
      throw IllegalArgumentException("Service '$code' is not supported.")
    }
    return service
  }
  val serviceCodes: List<String>
      get() = codeToService.keySet().toList()
}

class ServiceUrlResolver(val serviceCode: String) : UrlResolver {
  override fun getPhotoRedirectUrl(id: String, size: ImageSize) = "/services/user/photo/redirect/$serviceCode/$id/$size"
}