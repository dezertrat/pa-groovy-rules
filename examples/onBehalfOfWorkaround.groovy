/*
This script can be used to handle a WS-Trust OnBehalfOf scenario.  As PingFederate doesn't support using OnBehalfOf WS-Trust
tokens, this can provide a workaround to accept an OnBehalfOf token and provide some authentication of the calling proxy.

In this scenario, the PingFederate IP STS (/idp/sts.wst, /pf/sts.wst) is configured as a site and has WS-Trust authentication turned on (Server Settings > WS-Trust Settings > Configure WS-Trust Authentication).  I configured it to use BASIC authentication and defined a PingAccess site authenticator with these credentials.

Next configure a Groovy Script policy with the attached script to validate the credentials of the proxy (in the Security header), then if valid, pass through the RST to the PingFederate STS to process and return the appropriate token.

Note: This will only work for the /idp/sts.wst and the /pf/sts.wst endpoints, the SP STS can only accept a single token (so you can modify the script to move the OnBehalfOf / strip the token in the Security header if needed)
*/

// Needed to convert the XML Node to String
import groovy.xml.StreamingMarkupBuilder

// \/ DEBUG ONLY \/ - This is only needed as I use a self-signed cert on my PingAccess box
import javax.net.ssl.HostnameVerifier 
import javax.net.ssl.HttpsURLConnection 
import javax.net.ssl.SSLContext 
import javax.net.ssl.TrustManager 
import javax.net.ssl.X509TrustManager 
def nullTrustManager = [ 
    checkClientTrusted: { chain, authType ->  }, 
    checkServerTrusted: { chain, authType ->  }, 
    getAcceptedIssuers: { null } 
] 
def nullHostnameVerifier = [ 
    verify: { hostname, session -> true } 
] 
SSLContext sc = SSLContext.getInstance("SSL") 
sc.init(null, [nullTrustManager as X509TrustManager] as TrustManager[], null) 
HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory()) 
HttpsURLConnection.setDefaultHostnameVerifier(nullHostnameVerifier as HostnameVerifier)     
// /\ DEBUG ONLY /\ -----------------------------------------------------

// This is our flag to track the result.  If it is true, we can continue processing the request
def ok_to_continue = false

// The validator will test this script with a null body, so need to handle this
if (exc?.request?.body != null) {

  // Parse the body of the request into manageable XML
  def parsedXml = new XmlSlurper().parseText(new String(exc?.request?.body.getContent(), "UTF-8")) 

  // If we have an OBO token, then we need to test the proxy credentials
  if (parsedXml.Body.RequestSecurityToken.children().collect{ it.name() }.contains('OnBehalfOf')) { 

    // To test the proxy credentials, if we remove the OBO we will be left with a regular
    // RST request which PingFederate can handle, so strip the OBO and POST the RST to verify
    // the proxy credentials
    parsedXml.Body.RequestSecurityToken.OnBehalfOf.replaceNode{} 
    def modifiedXml = new StreamingMarkupBuilder().bindNode(parsedXml).toString()

    // We are going to re-send the modified RST through the same PingAccess url as this arrived.
    // This way we don't need to specify BASIC credentials in this script. Although this could be
    // better as we need to guess at protocol and reconstitute the URL from multiple variables
    def sts_url = 'https://' + exc?.request?.header.getHost() + exc?.getRequestURI() 
    try {
      HttpURLConnection conn = sts_url.toURL().openConnection() 
      conn.setDoOutput(true) 
      conn.setRequestProperty("content-type", "application/soap+xml; charset=utf-8")
      conn.outputStream.withWriter { Writer writer -> writer << modifiedXml } 
      // This is the response from the STS, we can parse it if we want or just
      // use responseCode's
      //String resp = conn.inputStream.withReader { Reader reader -> reader.text } 
      if (conn.responseCode == 200) { 
        // Received a 200 OK - the credentials were valid
        ok_to_continue = true
      } else {
        // Received another response code (ie 401, 500) - invalid credentials
      }
    } catch(e) {
      // An exception was caught, probably a 500 - failed to verify credentials
    }
  } else { 
    // No OnBehalfOf token was received - we can continue and let PingFederate handle
    // the single token in the request
    ok_to_continue = true
  } 
}
if (ok_to_continue) { anything() } else { not(anything()) }