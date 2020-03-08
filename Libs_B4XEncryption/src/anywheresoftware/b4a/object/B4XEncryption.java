
/*
 * Copyright 2010 - 2020 Anywhere Software (www.b4x.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 package anywheresoftware.b4a.object;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;

/**
 * B4XCipher uses AES with a random salt and random initialization vector to encrypt the data.
 * The salt and IV are stored in the returned data. This encryption method is compatible with B4J B4XCipher and B4i Cipher objects.
 * Note that on Android 4.3 and below the password should only include ASCII characters.
 */
@ShortName("B4XCipher")
@Version(1.00f)
public class B4XEncryption {

	/**
	 * Decrypts the given data with the given password.
	 */
	public byte[] Decrypt(byte[] Data, String Password) throws InvalidKeyException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		byte[] salt = new byte[8];
		byte[] iv = new byte[16];
		System.arraycopy(Data, 0, salt, 0, 8);
		System.arraycopy(Data, 8, iv, 0, 16);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec =
            new PBEKeySpec(Password.toCharArray(),salt, 1024, 128);
        SecretKey tmp = factory.generateSecret(keySpec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        Cipher d = Cipher.getInstance("AES/CBC/PKCS5Padding");
        d.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        byte[] t = new byte[Data.length - 24];
        System.arraycopy(Data, 24, t, 0, t.length);
        return d.doFinal(t);
	}
	/**
	 * Encrypts the given data with the given password.
	 */
	public byte[] Encrypt(byte[] Data, String Password) throws InvalidKeyException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		SecureRandom rnd = new SecureRandom();
		byte[] salt = new byte[8];
		rnd.nextBytes(salt);
		byte[] iv = new byte[16];
		rnd.nextBytes(iv);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec =
            new PBEKeySpec(Password.toCharArray(),salt, 1024, 128);
        SecretKey tmp = factory.generateSecret(keySpec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        Cipher d = Cipher.getInstance("AES/CBC/PKCS5Padding");
        d.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(iv));
        byte[] enc =  d.doFinal(Data);
        final byte[] plain = new byte[enc.length + 24];
		System.arraycopy(salt, 0, plain, 0, 8);
		System.arraycopy(iv, 0, plain, 8, 16);
		System.arraycopy(enc, 0, plain, 24, enc.length);
		return plain;
        
	}
}
