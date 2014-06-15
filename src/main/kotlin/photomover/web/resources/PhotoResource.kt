package photomover.web.resources

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import photomover.thirdparty.Album
import photomover.thirdparty.Photo
import javax.ws.rs.core.Context
import javax.servlet.http.HttpServletResponse
import photomover.thirdparty.ImageSize
import photomover.services.UserService
import photomover.web.RequestState
import javax.inject.Inject
import javax.ws.rs.Produces
import photomover.thirdparty.AlbumInfo
import photomover.thirdparty.Page
import javax.ws.rs.QueryParam
import photomover.AppPresentationSettings

Path("/photos") Produces("application/json")
class PhotoResource[Inject](
    val userService: UserService, val state: RequestState, var presentationSettings: AppPresentationSettings) {

  GET Path("/{service}/albums") fun albums(PathParam("service") service: String): List<Album> {
    return userService.getAlbums(state.currentUser, service)
  }

  GET Path("/{service}/albums/{albumId}/photos") fun albumPhotos(
      PathParam("service") service: String,
      PathParam("albumId") albumId: String,
      QueryParam("page") page: String?): List<Photo> {
    return userService.getAlbumPhotos(
        state.currentUser, service, albumId, Page(Integer.parseInt(page ?: "0"), presentationSettings.photosPerPage))
  }

  GET Path("/{service}/albums/{albumId}/info") fun albumInfo(
      PathParam("service") service: String, PathParam("albumId") albumId: String): AlbumInfo {
    return userService.getAlbumInfo(state.currentUser, service, albumId)
  }

  GET Path("/{service}/photo/{id}/{size}/redirect") fun goToPhoto(
      Context response: HttpServletResponse,
      PathParam("service") service: String,
      PathParam("id") id: String,
      PathParam("size") size: String) {
    val imageSize = ImageSize.valueOf(size)
    response.sendRedirect(userService.getPhotoUrl(state.currentUser, service, id, imageSize))
  }

}