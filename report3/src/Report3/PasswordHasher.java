package Report3;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

final class PasswordHasher {
	private static final String PREFIX = "pbkdf2$sha256$";
	private static final int DEFAULT_ITERATIONS = 120_000;
	private static final int SALT_BYTES = 16;
	private static final int KEY_BITS = 256;
	private static final SecureRandom RNG = new SecureRandom();

	private PasswordHasher() {
	}

	static String hash(String password) {
		if (password == null) throw new IllegalArgumentException("password is null");
		byte[] salt = new byte[SALT_BYTES];
		RNG.nextBytes(salt);
		byte[] derived = pbkdf2(password.toCharArray(), salt, DEFAULT_ITERATIONS, KEY_BITS);
		return PREFIX
				+ DEFAULT_ITERATIONS
				+ "$"
				+ Base64.getEncoder().encodeToString(salt)
				+ "$"
				+ Base64.getEncoder().encodeToString(derived);
	}

	static boolean verify(String password, String storedRecord) {
		if (password == null || storedRecord == null) return false;
		String record = storedRecord.trim();
		if (!record.startsWith(PREFIX)) {
			// Legacy plaintext support
			return record.equals(password);
		}

		String[] parts = record.split("\\$");
		// Expected: pbkdf2$sha256$<iterations>$<saltB64>$<hashB64>
		if (parts.length != 5) return false;
		int iterations;
		try {
			iterations = Integer.parseInt(parts[2]);
		} catch (NumberFormatException e) {
			return false;
		}
		byte[] salt;
		byte[] expected;
		try {
			salt = Base64.getDecoder().decode(parts[3]);
			expected = Base64.getDecoder().decode(parts[4]);
		} catch (IllegalArgumentException e) {
			return false;
		}

		byte[] actual;
		try {
			actual = pbkdf2(password.toCharArray(), salt, iterations, expected.length * 8);
		} catch (RuntimeException e) {
			return false;
		}
		return MessageDigest.isEqual(expected, actual);
	}

	static boolean isLegacyPlaintextRecord(String storedRecord) {
		if (storedRecord == null) return true;
		return !storedRecord.trim().startsWith(PREFIX);
	}

	private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyBits) {
		try {
			PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyBits);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			return skf.generateSecret(spec).getEncoded();
		} catch (Exception e) {
			throw new RuntimeException("PBKDF2 failed", e);
		}
	}
}
