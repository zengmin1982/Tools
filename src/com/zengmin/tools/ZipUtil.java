package com.zengmin.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipUtil {
	private static transient Logger LOG = LoggerFactory.getLogger(CryptUtil.class);
	private static final String CHARSET_UTF8 = "UTF-8";

	public static String compress(String str) {
		String result = null;
		if (StringUtil.isNotEmpty(str)) {
			byte[] data = null;
			try {
				data = str.getBytes(CHARSET_UTF8);
			} catch (Exception e) {
				data = str.getBytes();
			}
			result = Base64.encodeBase64String(compress(data));
		}
		return result;
	}

	public static byte[] compress(byte[] data) {
		byte[] result = null;
		if (data != null && data.length > 0) {
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				GZIPOutputStream gzip = new GZIPOutputStream(out);
				gzip.write(data);
				gzip.close();
				result = out.toByteArray();
			} catch (Exception e) {
				if (LOG.isErrorEnabled()) {
					LOG.error("Error in compress:", e);
				}
			}
		}
		return result;
	}

	public static String uncompress(String str) {
		String result = null;
		if (StringUtil.isNotEmpty(str)) {
			try {
				byte[] data = uncompress(Base64.decodeBase64(str));
				if (data != null) {
					result = new String(data, CHARSET_UTF8);
				}
			} catch (Exception e) {
				if (LOG.isErrorEnabled()) {
					LOG.error("Error in uncompress:", e);
				}
			}
		}
		return result;
	}

	public static byte[] uncompress(byte[] data) {
		byte[] result = null;
		if (data != null && data.length > 0) {
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ByteArrayInputStream in = new ByteArrayInputStream(data);
				GZIPInputStream gzip = new GZIPInputStream(in);
				byte[] buffer = new byte[1024];
				int n = 0;
				while ((n = gzip.read(buffer)) >= 0) {
					out.write(buffer, 0, n);
				}
				gzip.close();
				result = out.toByteArray();
			} catch (Exception e) {
				if (LOG.isErrorEnabled()) {
					LOG.error("Error in uncompress:", e);
				}
			}
		}
		return result;
	}
}