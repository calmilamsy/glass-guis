package net.glasslauncher.guis.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class Crypt {
   @Environment(EnvType.CLIENT)
   public static SecretKey generateSecretKey() throws CryptException {
      try {
         KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
         keyGenerator.init(128);
         return keyGenerator.generateKey();
      } catch (Exception var1) {
         throw new CryptException(var1);
      }
   }

   public static KeyPair generateKeyPair() throws CryptException {
      try {
         KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
         keyPairGenerator.initialize(1024);
         return keyPairGenerator.generateKeyPair();
      } catch (Exception var1) {
         throw new CryptException(var1);
      }
   }

   public static byte[] digestData(String string, PublicKey publicKey, SecretKey secretKey) throws CryptException {
      try {
         return digestData(string.getBytes("ISO_8859_1"), secretKey.getEncoded(), publicKey.getEncoded());
      } catch (Exception var4) {
         throw new CryptException(var4);
      }
   }

   private static byte[] digestData(byte[]... bs) throws Exception {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
      byte[][] var2 = bs;
      int var3 = bs.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         byte[] cs = var2[var4];
         messageDigest.update(cs);
      }

      return messageDigest.digest();
   }

   @Environment(EnvType.CLIENT)
   public static PublicKey byteToPublicKey(byte[] bs) throws CryptException {
      try {
         EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(bs);
         KeyFactory keyFactory = KeyFactory.getInstance("RSA");
         return keyFactory.generatePublic(encodedKeySpec);
      } catch (Exception var3) {
         throw new CryptException(var3);
      }
   }

   public static SecretKey decryptByteToSecretKey(PrivateKey privateKey, byte[] bs) throws CryptException {
      byte[] cs = decryptUsingKey(privateKey, bs);

      try {
         return new SecretKeySpec(cs, "AES");
      } catch (Exception var4) {
         throw new CryptException(var4);
      }
   }

   @Environment(EnvType.CLIENT)
   public static byte[] encryptUsingKey(Key key, byte[] bs) throws CryptException {
      return cipherData(1, key, bs);
   }

   public static byte[] decryptUsingKey(Key key, byte[] bs) throws CryptException {
      return cipherData(2, key, bs);
   }

   private static byte[] cipherData(int i, Key key, byte[] bs) throws CryptException {
      try {
         return setupCipher(i, key.getAlgorithm(), key).doFinal(bs);
      } catch (Exception var4) {
         throw new CryptException(var4);
      }
   }

   private static Cipher setupCipher(int i, String string, Key key) throws Exception {
      Cipher cipher = Cipher.getInstance(string);
      cipher.init(i, key);
      return cipher;
   }

   public static Cipher getCipher(int i, Key key) throws CryptException {
      try {
         Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
         cipher.init(i, key, new IvParameterSpec(key.getEncoded()));
         return cipher;
      } catch (Exception var3) {
         throw new CryptException(var3);
      }
   }
}
