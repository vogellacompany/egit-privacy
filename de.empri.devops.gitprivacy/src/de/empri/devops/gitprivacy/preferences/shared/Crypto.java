package de.empri.devops.gitprivacy.preferences.shared;


public class Crypto {

	public static String generateKey() {

		// TODO Auto-generated method stub
		return "";
	}

}

//package de.empri.devops.gitprivacy.preferences.shared;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import com.goterl.lazycode.lazysodium.SodiumJava;
//import com.goterl.lazycode.lazysodium.interfaces.Scrypt;
//import com.goterl.lazycode.lazysodium.interfaces.SecretBox;
//import com.goterl.lazycode.lazysodium.utils.Base64MessageEncoder;
//import com.goterl.lazycode.lazysodium.utils.Key;
//
///**
// * Utility class that encrypts and decrypts Strings using symmetric key
// * encryption.
// *
// */
//public class Crypto {
//
//	private static SecretBox.Native secretBox;
//
//	private static Base64MessageEncoder encoder;
//
//	private static LazySodiumJava lazySodium;
//
//	private static Scrypt.Native scrypt;
//
//	private static final SodiumJava sodiumJava = new SodiumJava();
//
//	private List<Key> cryptoKeys = new ArrayList<>();
//
//	private Key currentKey;
//
//	static {
//		encoder = new Base64MessageEncoder();
//		lazySodium = new LazySodiumJava(sodiumJava, encoder);
//		secretBox = lazySodium;
//		scrypt = lazySodium;
//	}
//
//	/**
//	 *
//	 */
//	public Crypto() {
//	}
//
//	/**
//	 * @param allKeys
//	 */
//	public Crypto(List<String> allKeys) {
//		cryptoKeys = allKeys.stream().map(Key::fromBase64String)
//				.collect(Collectors.toList());
//		currentKey = cryptoKeys.get(0);
//	}
//
//	/**
//	 * @param password
//	 * @param salt
//	 * @return key derived from password and salt
//	 */
//	public static String deriveKey(String password, String salt) {
//		return encoder.encode(buildKey(password, salt).getAsBytes());
//	}
//
//	private static Key buildKey(String password, String salt) {
//		byte[] pwHash = new byte[SecretBox.KEYBYTES];
//		byte[] pass = password.getBytes();
//		boolean pwHashWorked = scrypt.cryptoPwHashScryptSalsa208Sha256(pwHash,
//				pwHash.length, pass, pass.length, encoder.decode(salt),
//				Scrypt.SCRYPTSALSA208SHA256_OPSLIMIT_INTERACTIVE,
//				Scrypt.SCRYPTSALSA208SHA256_MEMLIMIT_INTERACTIVE);
//		if (!pwHashWorked) {
//			System.out.println("pwHash failed"); //$NON-NLS-1$
//		}
//		return Key.fromBytes(pwHash);
//	}
//
//	/**
//	 * @param toEncrypt
//	 *            String to encrypt
//	 * @return encrypted message
//	 */
//	public String encrypt(String toEncrypt) {
//		byte[] nonceBytes = lazySodium.nonce(SecretBox.NONCEBYTES);
//
//		byte[] date = toEncrypt.getBytes();
//		byte[] cipherBytes = new byte[SecretBox.MACBYTES + date.length];
//		boolean encryptionWorked = secretBox.cryptoSecretBoxEasy(cipherBytes,
//				date, date.length, nonceBytes, currentKey.getAsBytes());
//		if (!encryptionWorked) {
//			return "ERROR"; //$NON-NLS-1$
//		}
//		byte[] noncePlusCipherBytes = Arrays.copyOf(nonceBytes,
//				nonceBytes.length + cipherBytes.length);
//		System.arraycopy(cipherBytes, 0, noncePlusCipherBytes,
//				nonceBytes.length, cipherBytes.length);
//
//		return encoder.encode(noncePlusCipherBytes);
//	}
//
//	/**
//	 * @param encrypted message
//	 * @return decrypted message
//	 */
//	public String decrypt(String encrypted) {
//		byte[] noncePlusCipherBytes = encoder.decode(encrypted);
//		byte[] nonceBytes = new byte[SecretBox.NONCEBYTES];
//		System.arraycopy(noncePlusCipherBytes, 0, nonceBytes, 0,
//				SecretBox.NONCEBYTES);
//		byte[] cipherBytes = new byte[noncePlusCipherBytes.length
//				- SecretBox.NONCEBYTES];
//		System.arraycopy(noncePlusCipherBytes, SecretBox.NONCEBYTES,
//				cipherBytes,
//				0, noncePlusCipherBytes.length - SecretBox.NONCEBYTES);
//		byte[] decrypted = new byte[cipherBytes.length - SecretBox.MACBYTES];
//		boolean decryptionWorked = false;
//		for (Key key : cryptoKeys) {
//			decryptionWorked = secretBox.cryptoSecretBoxOpenEasy(decrypted,
//					cipherBytes, cipherBytes.length, nonceBytes,
//					key.getAsBytes());
//			if (decryptionWorked) {
//				break;
//			}
//		}
//		if (!decryptionWorked) {
//			return ""; //$NON-NLS-1$
//		}
//
//		return new String(decrypted);
//	}
//
//	/**
//	 * @return encryption key
//	 */
//	public static String generateKey() {
//		byte[] key = new byte[SecretBox.KEYBYTES];
//		secretBox.cryptoSecretBoxKeygen(key);
//		return encoder.encode(key);
//	}
//
//	/**
//	 * @return randomly generated salt, base64 encoded
//	 */
//	public static String generateSalt() {
//		return encoder.encode(lazySodium.randomBytesBuf(
//				Math.toIntExact(Scrypt.SCRYPTSALSA208SHA256_SALT_BYTES)));
//	}
//
//}