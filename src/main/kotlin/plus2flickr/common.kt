package plus2flickr

import plus2flickr.domain.ServiceType
import plus2flickr.thirdparty.CloudService
import plus2flickr.thirdparty.UrlResolver
import plus2flickr.thirdparty.ImageSize

class CloudServiceContainer {
  val accountTypeToService = hashMapOf<ServiceType, CloudService>()

  fun register(accountType: ServiceType, service: CloudService) {
    accountTypeToService.put(accountType, service)
  }

  fun get(accountType: ServiceType): CloudService {
    val service = accountTypeToService[accountType]
    if (service == null) {
      throw IllegalArgumentException("Service '$accountType' is not supported.")
    }
    return service
  }
}

class ServiceUrlResolver(val accountType: ServiceType) : UrlResolver {
  override fun getPhotoRedirectUrl(id: String, size: ImageSize) = "/services/user/photo/redirect/$accountType/$id/$size"
}