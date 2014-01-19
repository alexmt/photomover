package plus2flickr.web

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest

fun HttpServletRequest.findByCookieName(name : String) : Cookie? {
  val cookies : List<Cookie>? = this.getCookies()?.groupBy { cookie -> cookie.getName() }?.get(name)
  return if ( cookies != null && cookies.size() > 0) {
    return cookies.get(0)
  } else {
    null
  }
}
