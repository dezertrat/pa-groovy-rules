/*

Sample Groovy scrip that extracts information from the User object for an authenticated
user web session. The example shows extracting to attributes the sub and email, which are
assumed to be in the web session.

The attributes are then written to the pingaccess.log for demonstration purposes.

*/
exc?.log.debug "Test User: Start" 
user=exc?.user?.getSubject()
email=exc?.user?.getFirstAttributeValue("email")
exc?.log.debug "User " + "$user"
exc?.log.debug "Email " + "$email"
exc?.log.debug "Test User: End"
pass()
