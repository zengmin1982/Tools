package com.zengmin.tools;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

public class RsaHelper {
	private static transient Logger log = LoggerFactory.getLogger(RsaHelper.class);
	private static final String ALGORITHM_RSA = "RSA";
	private static final String TRANSFORMATION_RSA = "RSA/ECB/PKCS1Padding";
	private static final String PROVIDER_RSA = "SunRsaSign";
	private static final String CHARSET_UTF8 = "UTF-8";
	private static final int BLOCK_SIZE = 1024;
	private static final int MAX_ENCRYPT_BLOCK = 117;
	private static final int MAX_DECRYPT_BLOCK = 128;
	private static RsaHelper _instance = null;
    private File PK_FILE = null;
	private String PK_VERSION = null;
	private PrivateKey PRIVATE_KEY = null;
	private PublicKey PUBLIC_KEY = null;
	private String PUBLIC_KEY_STR = null;
	private boolean isLoaded = false;

	private RsaHelper() {
        try {
            DefaultResourceLoader loader = new DefaultResourceLoader();
            Resource res = loader.getResource(ConfigUtil.getConfig("${CONFIG_PATH}"));
            PK_FILE = new File(res.getFile(), "rsa.key");
        } catch (Exception e) {
            PK_FILE = new File("rsa.key");
        }
	}

	public static RsaHelper getInstance() {
		if (_instance == null) {
			synchronized (ALGORITHM_RSA) {
				if (_instance == null) {
					RsaHelper h = new RsaHelper();
					if (!h.isLoaded) {
						h._load();
					}
					if (!h.isLoaded) {
						h.generate();
						h._load();
					}
					if (h.isLoaded) {
						_instance = h;
					}
				}
			}
		}
		return _instance;
	}

	private void _load() {
		if (!isLoaded) {
			DataInputStream dis = null;
			try {
				dis = new DataInputStream(new FileInputStream(PK_FILE));
				long ms = dis.readLong();
				long ls = dis.readLong();
				int pklen = dis.readInt();
				byte[] pk = new byte[pklen];
				dis.read(pk, 0, pklen);
				int puklen = dis.readInt();
				byte[] puk = new byte[puklen];
				dis.read(puk, 0, puklen);
				PK_VERSION = new UUID(ms, ls).toString().replace("-", "").toUpperCase();
				KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_RSA, PROVIDER_RSA);
				PRIVATE_KEY = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(pk));
				PUBLIC_KEY = keyFactory.generatePublic(new X509EncodedKeySpec(puk));
				PUBLIC_KEY_STR = Base64.encodeBase64String(puk);
				isLoaded = true;
			} catch (Exception e) {
			} finally {
				if (dis != null) {
					try {
						dis.close();
					} catch (Exception e) {
					}
				}
			}
		}
	}

	private void generate() {
		isLoaded = false;
		DataOutputStream dos = null;
		try {
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			secureRandom.setSeed(System.currentTimeMillis());
			KeyPairGenerator kgp = KeyPairGenerator.getInstance(ALGORITHM_RSA, PROVIDER_RSA);
			kgp.initialize(BLOCK_SIZE, secureRandom);
			KeyPair kp = kgp.generateKeyPair();
			byte[] pk = kp.getPrivate().getEncoded();
			byte[] puk = kp.getPublic().getEncoded();
			UUID uuid = UUID.randomUUID();
			dos = new DataOutputStream(new FileOutputStream(PK_FILE));
			dos.writeLong(uuid.getMostSignificantBits());
			dos.writeLong(uuid.getLeastSignificantBits());
			dos.writeInt(pk.length);
			dos.write(pk);
			dos.writeInt(puk.length);
			dos.write(puk);
			dos.flush();
		} catch (Exception e) {
			log.error("Error in generate public key and private key.", e);
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public String getVersion() {
		return PK_VERSION;
	}

	public String getPublicKey() {
		return PUBLIC_KEY_STR;
	}

	public String encrypt(String data) {
		String result = "";
		try {
			result = Base64.encodeBase64String(encrypt(PUBLIC_KEY, data.getBytes(CHARSET_UTF8)));
		} catch (Exception e) {
			log.error("ERROR in encrypt:[" + data + "]", e);
		}
		return result;
	}

	public byte[] encrypt(PublicKey key, byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance(TRANSFORMATION_RSA);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int inputLen = data.length;
		int offSet = 0;
		byte[] cache = null;
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
				cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(data, offSet, inputLen - offSet);
				offSet += inputLen - offSet;
			}
			out.write(cache, 0, cache.length);
			offSet += MAX_ENCRYPT_BLOCK;
		}
		byte[] encryptedData = out.toByteArray();
		out.close();
		return encryptedData;
	}

	public String decrypt(String data) {
		String result = "";
		try {
			result = new String(decrypt(PRIVATE_KEY, Base64.decodeBase64(data)), CHARSET_UTF8);
		} catch (Exception e) {
			log.error("ERROR in decrypt:[" + data + "]", e);
		}
		return result;
	}

	public byte[] decrypt(PrivateKey key, byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance(TRANSFORMATION_RSA);
		cipher.init(Cipher.DECRYPT_MODE, key);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int inputLen = data.length;
		int offSet = 0;
		byte[] cache = null;
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
				cache = cipher.doFinal(data, offSet, MAX_DECRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(data, offSet, inputLen - offSet);
				offSet += inputLen - offSet;
			}
			out.write(cache, 0, cache.length);
			offSet += MAX_DECRYPT_BLOCK;
		}
		byte[] decryptedData = out.toByteArray();
		out.close();
		return decryptedData;
	}
}
