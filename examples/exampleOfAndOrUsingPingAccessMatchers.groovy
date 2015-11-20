/*

Sample Groovy scrip to show how to use the PingAccess matchers (rules) to provide an 
example of an OR and AND complex rule. 

This example uses the OAuth Scope and OAuth Attribute rules to check for the values in an
access token. This could be just as easily done with other rules for example the Web 
Session Attribute rule.

*/

exc?.log.debug "Start: Check Scopes, Groups, and Department"

allOf(
  hasScope("list_users"),
  // AND
  anyOf(
    allOf(
      hasAttribute("groups","cn=Contractor,ou=groups,dc=wal-ping,dc=com"),
      // AND
      hasAttribute("department","Finance")
    ),
    // OR
    hasAttribute("groups","cn=Manager,ou=groups,dc=wal-ping,dc=com")
  )
)