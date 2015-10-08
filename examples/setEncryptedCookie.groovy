/*

Sample groovy script that generates a proprietary encrypted cookie and adds it to the request to the PingAccess site,
can be used as an example of protecting applications configured with a proprietary cookie while these applications
are migrated to a better solution.

This example creates an AES encrypted and base64 encoded value of the plainText variable and sends it as a cookie
named encryptedCookie.

*/

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

String iv = "ABCDEFGHIJKLMNOP";
String secret = "MyPa55word123456";
String plainText = "0123456789ABCDEF0123456789ABCDEF";

Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding","SunJCE");
SecretKeySpec key = new SecretKeySpec(secret.getBytes("UTF-8"), "AES");
cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv.getBytes("UTF-8")));
byte[] encrBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
byte[] encoded = Base64.encodeBase64(encrBytes);

String cookieVal = new String(encoded);
def header = exc?.request?.header;
header?.setCookie("encryptedCookie=" + cookieVal);
anything()