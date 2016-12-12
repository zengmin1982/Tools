package com.zengmin.tools;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CryptUtil {
	private static transient Logger log = LoggerFactory.getLogger(CryptUtil.class);
	private static final String ALGORITHM_MD5 = "MD5";
	private static final String ALGORITHM_AES = "AES";
	private static final String TRANSFORMATION_AES = "AES/ECB/PKCS5Padding";
	private static final String PROVIDER_MD5 = "SUN";
	private static final String PROVIDER_AES = "SunJCE";
	private static final String CHARSET_UTF8 = "UTF-8";
	private static final String DEFAULT_AES_KEY = CryptUtil.class.getName();

	public static String encrypt(String... datas) {
		StringBuilder sb = new StringBuilder();
		for (String data : datas) {
			if (data != null) {
				sb.append(data);
			}
		}
		byte[] bytes = encryptMD5(sb.toString());
		return Base64.encodeBase64String(bytes);
	}

	public static String encode(String value) {
		String result = value;
		if (isNotEmpty(value)) {
			try {
				result = URLEncoder.encode(value, CHARSET_UTF8);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}

	public static String decode(String value) {
		String result = value;
		if (isNotEmpty(value)) {
			try {
				result = URLDecoder.decode(value, CHARSET_UTF8);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return result;
	}

	public static String encodeMap(Map<String, Object> paraMap) {
		String param = null;
		if (paraMap != null) {
			StringBuilder sb = new StringBuilder();
			Map<String, Object> sortedParams = new TreeMap<String, Object>(paraMap);
			Set<Entry<String, Object>> paramSet = sortedParams.entrySet();
			boolean hasParam = false;
			for (Entry<String, Object> ent : paramSet) {
				if (isNotEmpty(ent.getValue())) {
					if (hasParam) {
						sb.append("&");
					} else {
						hasParam = true;
					}
					sb.append(ent.getKey()).append("=").append(encodeBase64(ent.getValue().toString()));
				}
			}
			param = encode(encodeBase64(sb.toString()));
		}
		return param;
	}

	public static Map<String, String> decodeMap(String param) throws Exception {
		Map<String, String> result = new HashMap();
		if (isNotEmpty(param)) {
			String originParams = decodeBase64(decode(param));
			String[] pairs = originParams.split("&");
			if (pairs != null && pairs.length > 0) {
				for (String pair : pairs) {
					String[] parampair = pair.split("=", 2);
					if (parampair != null && parampair.length == 2) {
						result.put(parampair[0], decodeBase64(parampair[1]));
					}
				}
			}
		}
		return result;
	}

	public static String encodeBase64(String str) {
		String result = null;
		try {
			byte[] b = str.getBytes(CHARSET_UTF8);
			result = Base64.encodeBase64String(b);
		} catch (UnsupportedEncodingException e) {
		}
		return result;
	}

	public static String decodeBase64(String str) {
		String result = null;
		try {
			result = new String(Base64.decodeBase64(str), CHARSET_UTF8);
		} catch (UnsupportedEncodingException e) {
		}
		return result;
	}

	private static byte[] encryptMD5(String data) {
		byte[] bytes = null;
		try {
			MessageDigest md = MessageDigest.getInstance(ALGORITHM_MD5, PROVIDER_MD5);
			bytes = md.digest(data.getBytes(CHARSET_UTF8));
		} catch (Exception e) {
			log.error("ERROR in encryptMD5:[" + data + "]", e);
		}
		return bytes;
	}

	public static String encryptAes(String data) {
		return encryptAes(DEFAULT_AES_KEY, data);
	}

	public static String encryptAes(String key, String data) {
		String result = null;
		try {
			Cipher cipher = getAesCipher(key, Cipher.ENCRYPT_MODE);
			result = Base64.encodeBase64String(cipher.doFinal(data.getBytes(CHARSET_UTF8)));
		} catch (Exception e) {
			log.error("ERROR in encryptDes:[" + data + "]", e);
		}
		return result;
	}

	public static String decryptAes(String data) {
		return decryptAes(DEFAULT_AES_KEY, data);
	}

	public static String decryptAes(String key, String data) {
		String result = null;
		try {
			Cipher cipher = getAesCipher(key, Cipher.DECRYPT_MODE);
			result = new String(cipher.doFinal(Base64.decodeBase64(data)), CHARSET_UTF8);
		} catch (Exception e) {
			log.warn("ERROR in decryptDes:[" + data + "]", e);
		}
		return result;
	}

	private static Cipher getAesCipher(String key, int mode) throws Exception {
		SecretKeySpec sks = new SecretKeySpec(encryptMD5(key), ALGORITHM_AES);
		Cipher cipher = Cipher.getInstance(TRANSFORMATION_AES, PROVIDER_AES);
		cipher.init(mode, sks);
		return cipher;
	}

	private static boolean isNotEmpty(Object o) {
		return o != null && (o instanceof String ? (String) o : o.toString()).trim().length() > 0;
	}
}
