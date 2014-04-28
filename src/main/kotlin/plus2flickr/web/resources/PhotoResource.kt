package plus2flickr.web.resources

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import plus2flickr.thirdparty.Album
import plus2flickr.thirdparty.Photo
import javax.ws.rs.core.Context
import javax.servlet.http.HttpServletResponse
import plus2flickr.thirdparty.ImageSize
import plus2flickr.services.UserService
import plus2flickr.web.RequestState
import javax.inject.Inject
import javax.ws.rs.Produces
import plus2flickr.thirdparty.AlbumInfo

Path("/photos") Produces("application/json")
class PhotoResource[Inject](val userService: UserService, val state: RequestState) {

  GET Path("/{service}/albums") fun albums(PathParam("service") service: String): List<Album> {
    return userService.getAlbums(state.currentUser, service)
  }

  GET Path("/{service}/albums/{albumId}/photos") fun albumPhotos(
      PathParam("service") service: String, PathParam("albumId") albumId: String): List<Photo> {
    return userService.getAlbumPhotos(state.currentUser, service, albumId)
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