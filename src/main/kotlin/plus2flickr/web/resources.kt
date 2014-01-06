package plus2flickr.web.resources

import javax.ws.rs.Produces
import javax.ws.rs.Path
import plus2flickr.web.models.AccountInfo
import javax.ws.rs.GET

Path("/account") Produces("application/json") class Account {

  GET Path("/info") fun info(): AccountInfo {
    return AccountInfo()
  }
}
