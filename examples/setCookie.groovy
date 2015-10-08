/*
This script is an example of how to set cookies in a Groovy script in PingAccess. The cookie will be sent on the response that is returned by the PA to the browser. This script was written and tested on PA 3.1, using information from the SDK docs.
 
You can also set additional properties of the cookie by using the other methods in the cookie object, see the SDK javadocs for more details.
*/

List cookies = new java.util.ArrayList()
String value = "value test cookie"
String name = "testCookie"
com.pingidentity.pa.sdk.http.SetCookie cookie = new com.pingidentity.pa.sdk.http.SetCookie()
cookie.name(name)
cookie.value(value)
cookies?.add(cookie.toHeaderField())
exc?.response?.header?.setSetCookie(cookies)
anything()