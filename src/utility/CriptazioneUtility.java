package utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tomcat.util.codec.binary.Base64;

/**
 * @author nico, eugenio
 *
 */
public class CriptazioneUtility {
	private static MessageDigest md;
	private static final String KEY = "deveesseresedici";
	private static final String VEC = "encryptionIntVec";
	private static final String ALG = "AES/CBC/PKCS5PADDING";

	/**
	 * criptaConMD5 è una funzione che permette di criptare le password in MD5
	 * 
	 * @param password indica la password che deve essere criptata
	 * @return la password criptata se la criptazione è andata a buon fine
	 *         altrimenti restituisce null
	 */
	public static String criptaConMD5(String password) {
		try {
			md = MessageDigest.getInstance("MD5");
			byte[] passwordBytes = password.getBytes();
			md.reset();
			byte[] digested = md.digest(passwordBytes);
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < digested.length; i++) {
				sb.append(Integer.toHexString(0xff & digested[i]));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	
	/**
	 * Metodo che prende il file da codificare in un InputStream, gli applica
	 * crittografia AES (CBC mode) e lo restituisce come stringa in base64
	 * @param file: InputStream contenente il file da codificare
	 * @return String contentente il file criptato in codifica base64
	 */
	public static String codificaFile(InputStream file) {
		String result = null;
		byte[] outputBytes = null;
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int nRead;
			byte[] targetArray = new byte[file.available()];

			while ((nRead = file.read(targetArray, 0, targetArray.length)) != -1) {
				buffer.write(targetArray, 0, nRead);
			}
			
			IvParameterSpec iv = new IvParameterSpec(VEC.getBytes("UTF-8"));
			SecretKeySpec sks = new SecretKeySpec(KEY.getBytes(), "AES");
			SecretKey key = sks;

			Cipher cipher = Cipher.getInstance(ALG);
			cipher.init(Cipher.ENCRYPT_MODE, key, iv);
			outputBytes = cipher.doFinal(targetArray);
			
			result = new String(Base64.encodeBase64(outputBytes), "UTF-8");

		} catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException
				| IllegalBlockSizeException | IOException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

	/**
	 * Metodo che prende una stringa in base64 contenente il file da decriptare, 
	 * lo converte in un array di byte, lo decripta e lo restituisce come stringa in base64.
	 * @param file: String contenente il file da decodificare
	 * @return String contentente il file decriptato in codifica base64
	 */
	public static String decodificaFile(String file) {
		byte[] outputBytes = null;
		String result = null;
		try {
			byte[] inputBytes = Base64.decodeBase64(file);
			
			IvParameterSpec iv = new IvParameterSpec(VEC.getBytes("UTF-8"));
			SecretKeySpec sks = new SecretKeySpec(KEY.getBytes(), "AES");
			SecretKey key = sks;

			Cipher cipher = Cipher.getInstance(ALG);
			cipher.init(Cipher.DECRYPT_MODE, key, iv);
			outputBytes = cipher.doFinal(inputBytes);

			result = new String(Base64.encodeBase64(outputBytes), "UTF-8");

		} catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException
				| IllegalBlockSizeException | UnsupportedEncodingException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

}