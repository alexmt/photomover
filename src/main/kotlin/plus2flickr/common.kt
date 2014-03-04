package plus2flickr

import plus2flickr.domain.AccountType
import plus2flickr.thirdparty.CloudService

class CloudServiceContainer {
  val accountTypeToService = hashMapOf<AccountType, CloudService>()

  fun register(accountType: AccountType, service: CloudService) {
    accountTypeToService.put(accountType, service)
  }

  fun get(accountType: AccountType): CloudService {
    val service = accountTypeToService[accountType]
    if (service == null) {
      throw IllegalArgumentException("Service '$accountType' is not supported.")
    }
    return service
  }
}