package net.sistemasc.util;

import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Usage: String crypto = SimpleCrypto.encrypt(cleartext) ...
 * String cleartext = SimpleCrypto.decrypt(crypto)
 * 
 * @author ferenc.hechler
 */
public class SimpleCrypto {
  private KeyGenerator kgen;
  private SecureRandom sr;
  private SecretKey skey;
  private byte[] rawKey;
  private static Cipher cipherE;
  private static Cipher cipherD;

  public SimpleCrypto(String _seed) throws Exception {
    // init fields for best performance
    kgen = KeyGenerator.getInstance("AES");
    sr = SecureRandom.getInstance("SHA1PRNG");
    sr.setSeed(_seed.getBytes());
    kgen.init(128, sr); // 192 and 256 bits may not be available
    skey = kgen.generateKey();
    rawKey = skey.getEncoded();
    SecretKeySpec skeySpec = new SecretKeySpec(rawKey, "AES");
    cipherE = Cipher.getInstance("AES");
    cipherE.init(Cipher.ENCRYPT_MODE, skeySpec);
    cipherD = Cipher.getInstance("AES");
    cipherD.init(Cipher.DECRYPT_MODE, skeySpec);
  }

  /**
   * Clear to encrypted text
   * 
   * @param cleartext
   * @return clear text
   * @throws Exception
   */
  public String encrypt(String _cleartext) throws Exception {
    byte[] encrypted = cipherE.doFinal(_cleartext.getBytes());
    return toHex(encrypted);
  }

  /**
   * Encrypted to clear text
   * 
   * @param seed
   * @param encrypted
   * @return
   * @throws Exception
   */
  public String decrypt(String _encrypted) throws Exception {
    byte[] decrypted = cipherD.doFinal(toByte(_encrypted));
    return new String(decrypted);
  }

  public static String toHex(String txt) {
    return toHex(txt.getBytes());
  }

  public static String fromHex(String hex) {
    return new String(toByte(hex));
  }

  public static byte[] toByte(String hexString) {
    int len = hexString.length() / 2;
    byte[] result = new byte[len];
    for (int i = 0; i < len; i++)
      result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16)
          .byteValue();
    return result;
  }

  public static String toHex(byte[] buf) {
    if (buf == null)
      return "";
    StringBuffer result = new StringBuffer(2 * buf.length);
    for (int i = 0; i < buf.length; i++) {
      appendHex(result, buf[i]);
    }
    return result.toString();
  }

  private static void appendHex(StringBuffer sb, byte b) {
    final String HEX = "0123456789ABCDEF";
    sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
  }

}