package photomover

import photomover.thirdparty.CloudService
import photomover.thirdparty.ImageSize

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

data class AppPresentationSettings(val photosPerPage: Int, val maxPagesCount: Int)