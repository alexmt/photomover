package photomover.web.resources

import photomover.web.models.GoogleAppSettingsViewModel
import javax.ws.rs.Path
import javax.ws.rs.GET
import photomover.thirdparty.google.GoogleAppSettings
import javax.ws.rs.Produces
import com.google.inject.Inject
import photomover.AppPresentationSettings

Path("/app") Produces("application/json")
class AppResource[Inject](val googleAppSettings: GoogleAppSettings, var presentationSettings: AppPresentationSettings) {

  GET Path("/google/settings") fun googleAppSettings(): GoogleAppSettingsViewModel {
    return GoogleAppSettingsViewModel(
        clientId = googleAppSettings.clientId, scopes = googleAppSettings.scopes)
  }

  GET Path("/settings/presentation") fun presentationSettings() = presentationSettings
}
