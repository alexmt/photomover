package plus2flickr.web.resources

import plus2flickr.web.models.GoogleAppSettingsViewModel
import javax.ws.rs.Path
import javax.ws.rs.GET
import plus2flickr.thirdparty.google.GoogleAppSettings
import javax.ws.rs.Produces
import com.google.inject.Inject

Path("/app") Produces("application/json")
class AppResource[Inject](val googleAppSettings: GoogleAppSettings) {

  GET Path("/google/settings") fun googleAppSettings(): GoogleAppSettingsViewModel {
    return GoogleAppSettingsViewModel(
        clientId = googleAppSettings.clientId, scopes = googleAppSettings.scopes)
  }
}
