package plus2flickr

import org.kohsuke.args4j.Option
import com.flickr4java.flickr.Flickr
import com.flickr4java.flickr.REST
import com.flickr4java.flickr.auth.Permission
import org.scribe.model.Verifier
import com.flickr4java.flickr.RequestContext
import com.flickr4java.flickr.photosets.Photoset
import java.util.HashMap
import java.io.File
import com.flickr4java.flickr.uploader.UploadMetaData
import com.flickr4java.flickr.FlickrException

data class UploadOptions() {
  Option("-s") var source: String? = null
  Option("-appKey") var appKey: String? = null
  Option("-appSecret") var appSecret: String? = null
  Option("-public") var isPublic: Boolean = false
  Option("-maxSizeMb") var maxSizeMb: Int = 50
}

fun authorize(flickr: Flickr) {
  val authInterface = flickr.getAuthInterface()!!
  val token = authInterface.getRequestToken()
  val url = authInterface.getAuthorizationUrl(token, Permission.WRITE)
  println("Follow this URL to authorise yourself on Flickr")
  println(url)
  println("Paste in the token it gives you:")
  print(">>")
  val tokenKey = readLine()
  val requestToken = authInterface.getAccessToken(token, Verifier(tokenKey))
  println("Authentication success")
  val auth = authInterface.checkToken(requestToken)!!
  flickr.setAuth(auth)
  RequestContext.getRequestContext()!!.setAuth(auth)
}

fun upload(options: UploadOptions) {
  val flickr = Flickr(options.appKey, options.appSecret, REST())
  authorize(flickr)
  val photosetsInteface = flickr.getPhotosetsInterface()!!
  val photoSets = photosetsInteface.getList(flickr.getAuth()!!.getUser()!!.getId())!!
  var setByTitle = HashMap<String, Photoset>()
  for (set in photoSets.getPhotosets()!!) {
    setByTitle.put(set.getTitle()!!, set)
  }
  val uploader = flickr.getUploader()!!
  println("Start photo upload")
  val maxSizeBytes = options.maxSizeMb * 1024 * 1024
  for (dir in File(options.source!!).listFiles { it.isDirectory() }!!) {
    print("${dir.name}...")
    var set = setByTitle[dir.name]
    for (file in dir.listFiles({ it.length() < maxSizeBytes
    })!!) {
      val metaData = UploadMetaData()
      metaData.setPublicFlag(options.isPublic)
      metaData.setAsync(false)
      metaData.setTitle(file.getName())
      try {
        val photoId = uploader.upload(file, metaData)
        if (set == null) {
          set = photosetsInteface.create(dir.name, null, photoId)!!
        } else {
          photosetsInteface.addPhoto(set!!.getId(), photoId)
        }
      } catch (ex: FlickrException) {
        println()
        println("Cannot upload '$file':")
        print(ex.getErrorMessage())
      } catch (ex: Exception) {
        println()
        println("Unknown error while uploading '$file':")
        print(ex)
      }
      print(".")
    }
    println("done")
  }
}
