package com.zengmin.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.zengmin.tools.InputStreamUtils;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * httprequest util工具类
 * 
 * @author zengmin
 *
 */
public class HTTPRequest {

	private static PoolingHttpClientConnectionManager conManager = new PoolingHttpClientConnectionManager();

	private static CloseableHttpClient httpClient;
	private static CloseableHttpClient httpsClient;
	private static SSLContext sslcontext;

	static {
		conManager.setMaxTotal(3000);
		conManager.setDefaultMaxPerRoute(1000);
		// conManager.setValidateAfterInactivity();
		// client = HttpClientBuilder.create().setConnectionManager(conManager)
		// .build();

		// 创建全局的requestConfig
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000)
				.setConnectionRequestTimeout(8000).build();

		httpClient = HttpClients.custom().setConnectionManager(conManager).setDefaultRequestConfig(requestConfig)
				.setRetryHandler(new DefaultHttpRequestRetryHandler(3, false)).build();

		try {
			sslcontext = SSLContexts.custom().useSSL().build();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			sslcontext.init(null, new X509TrustManager[] { new HTTPRequest.HttpsTrustManager() }, new SecureRandom());
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslcontext,
				SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
		httpsClient = HttpClients.custom().setConnectionManager(conManager).setDefaultRequestConfig(requestConfig)
				.setRetryHandler(new DefaultHttpRequestRetryHandler(3, false)).setSSLSocketFactory(factory).build();

	}

	static class HttpsTrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			// TODO Auto-generated method stub

		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			// TODO Auto-generated method stub

		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[] {};
		}

	}


	/**
	 * @param host
	 *            服务器地址
	 * @param pathandargs
	 *            访问路径及参数String串
	 * @param header
	 *            自定义访问http请求头
	 * @return "header" http响应头，header[]数组， "response" http请求正文， String对象，
	 *         "entity" HttpEntity对象 发送get请求，"responsetime" long 响应时间，
	 *         "httpstatus" int 返回代码
	 */
	public static Map<String, Object> readContentFromGet(String host, String pathandargs, Map<String, String> header)
			throws Exception {
		Map<String, String> args = new HashMap<String, String>();
		String path = pathandargs;
		if (pathandargs.indexOf("?") != -1) {
			path = pathandargs.split("[?]")[0];
			String arg = pathandargs.split("[?]")[1];
			String[] arglist = arg.split("&");
			for (int i = 0; i < arglist.length; i++) {
				args.put(arglist[i].split("=")[0], arglist[i].split("=")[1]);

			}
		}
		return readContentFromGet(host, path, args, header);

	}

	/**
	 * @param host
	 *            服务器地址
	 * @param pathandargs
	 *            访问路径及参数String串
	 * @param header
	 *            自定义访问http请求头
	 * @return "header" http响应头，header[]数组， "response" http请求正文， String对象，
	 *         "entity" HttpEntity对象 发送get请求，"responsetime" long 响应时间，
	 *         "httpstatus" int 返回代码
	 */
	public static Map<String, Object> readContentFromPost(String host, String pathandargs, Map<String, String> header)
			throws Exception {
		Map<String, String> args = new HashMap<String, String>();
		String path = pathandargs;
		if (pathandargs.indexOf("?") != -1) {
			path = pathandargs.split("[?]")[0];
			String arg = pathandargs.split("[?]")[1];
			String[] arglist = arg.split("&");
			for (int i = 0; i < arglist.length; i++) {
				args.put(arglist[i].split("=")[0], arglist[i].split("=")[1]);

			}
		}
		return readContentFromPost(host, path, args, header);

	}

	/**
	 * @param host
	 *            服务器地址
	 * @param pathandargs
	 *            访问路径及参数String串
	 * @param header
	 *            自定义访问https请求头
	 * @return "header" https响应头，header[]数组， "response" https请求正文， String对象，
	 *         "entity" HttpEntity对象 发送get请求，"responsetime" long 响应时间，
	 *         "httpstatus" int 返回代码
	 */
	public static Map<String, Object> readContentFromhttpsGet(String host, String pathandargs,
			Map<String, String> header) throws Exception {
		Map<String, String> args = new HashMap<String, String>();
		String path = pathandargs;
		if (pathandargs.indexOf("?") != -1) {
			path = pathandargs.split("[?]")[0];
			String arg = pathandargs.split("[?]")[1];
			String[] arglist = arg.split("&");
			for (int i = 0; i < arglist.length; i++) {
				args.put(arglist[i].split("=")[0], arglist[i].split("=")[1]);

			}
		}
		return readContentFromhttpsGet(host, path, args, header);

	}

	/**
	 * @param host
	 *            服务器地址
	 * @param pathandargs
	 *            访问路径及参数String串
	 * @param header
	 *            自定义访问https请求头
	 * @return "header" https响应头，header[]数组， "response" https请求正文， String对象，
	 *         "entity" HttpEntity对象 发送get请求，"responsetime" long 响应时间，
	 *         "httpstatus" int 返回代码
	 */
	public static Map<String, Object> readContentFromhttpsPost(String host, String pathandargs,
			Map<String, String> header) throws Exception {
		Map<String, String> args = new HashMap<String, String>();
		String path = pathandargs;
		if (pathandargs.indexOf("?") != -1) {
			path = pathandargs.split("[?]")[0];
			String arg = pathandargs.split("[?]")[1];
			String[] arglist = arg.split("&");
			for (int i = 0; i < arglist.length; i++) {
				args.put(arglist[i].split("=")[0], arglist[i].split("=")[1]);
			}
		}
		return readContentFromhttpsPost(host, path, args, header);

	}

	/**
	 * @param host
	 *            服务器地址
	 * @param path
	 *            访问路径
	 * @param args
	 *            访问参数及参数变量
	 * @param header
	 *            自定义访问http请求头
	 * @return "header" http响应头，header[]数组， "response" http请求正文， String对象，
	 *         "entity" HttpEntity对象 发送get请求，"responsetime" long 响应时间，
	 *         "httpstatus" int 返回代码
	 */
	public static Map<String, Object> readContentFromGet(String host, String path, Map<String, String> args,
			Map<String, String> header) throws Exception {
		Map<String, Object> res = new HashMap<String, Object>();
		// CloseableHttpClient httpClient = getHttpClient();
		URI uri = buildURI("http", host, path, args);
		HttpGet httpGet = new HttpGet(uri);

		try {
			addHeader(httpGet, header);

			// System.out.println(httpGet.getAllHeaders().toString());
			long t1 = System.currentTimeMillis();
			System.out.println("Http start executing");
			CloseableHttpResponse response = httpClient.execute(httpGet);
			System.out.println("Http executing finished");

			long t2 = System.currentTimeMillis();
			res.put("responsetime", t2 - t1);

			try {
				System.out.println("start getResponse");
				getResponse(res, response);
				System.out.println("getResponse finished");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				response.close();
				System.out.println("response is closed");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			httpGet.releaseConnection();
			httpGet.abort();
			System.out.println("http is released");
		}
		System.out.println("return res" + res);
		return res;
	}

	/**
	 * @param host
	 *            服务器地址
	 * @param path
	 *            访问路径
	 * @param args
	 *            访问参数及参数变量
	 * @param header
	 *            自定义访问http请求头
	 * @return "header" http响应头，header[]数组， "response" https请求正文， String对象，
	 *         "entity" HttpEntity对象 发送get请求，"responsetime" long 响应时间，
	 *         "httpstatus" int 返回代码
	 */
	public static Map<String, Object> readContentFromhttpsGet(String host, String path, Map<String, String> args,
			Map<String, String> header) throws Exception {
		Map<String, Object> res = new HashMap<String, Object>();
		// CloseableHttpClient httpClient = (CloseableHttpClient)
		// getHttpsClient();
		URI uri = buildURI("https", host, path, args);
		HttpGet httpGet = new HttpGet(uri);

		try {
			addHeader(httpGet, header);

			// System.out.println(httpGet.getAllHeaders().toString());
			long t1 = System.currentTimeMillis();
			CloseableHttpResponse response = httpsClient.execute(httpGet);
			long t2 = System.currentTimeMillis();
			res.put("responsetime", t2 - t1);
			try {
				getResponse(res, response);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				response.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			httpGet.releaseConnection();
			httpGet.abort();

		}

		return res;
	}

	/**
	 * @param host
	 *            服务器地址
	 * @param path
	 *            访问路径
	 * @param args
	 *            访问参数及参数变量
	 * @param header
	 *            自定义访问http请求头
	 * @return "header" http响应头，header[]数组， "response" http请求正文，String 对象，
	 *         "entity" HttpEntity对象 发送post请求，"responsetime" long 响应时间，
	 *         "httpstatus" int 返回代码
	 */
	public static Map<String, Object> readContentFromPost(String host, String path, Map<String, String> args,
			Map<String, String> header) throws Exception {
		Map<String, Object> res = new HashMap<String, Object>();
		URI uri = buildURI("http", host, path, args);
		HttpPost httpPost = new HttpPost(uri);

		try {
			addHeader(httpPost, header);
			long t1 = System.currentTimeMillis();
			CloseableHttpResponse response = httpClient.execute(httpPost);
			long t2 = System.currentTimeMillis();

			res.put("responsetime", t2 - t1);

			try {
				getResponse(res, response);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				response.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			httpPost.releaseConnection();
			httpPost.abort();
		}
		return res;
	}

	/**
	 * @param host
	 *            服务器地址
	 * @param path
	 *            访问路径
	 * @param args
	 *            访问参数及参数变量
	 * @param header
	 *            自定义访问http请求头
	 * @return "header" http响应头，header[]数组， "response" https请求正文，String 对象，
	 *         "entity" HttpEntity对象 发送post请求，"responsetime" long 响应时间，
	 *         "httpstatus" int 返回代码
	 */
	public static Map<String, Object> readContentFromhttpsPost(String host, String path, Map<String, String> args,
			Map<String, String> header) throws Exception {
		Map<String, Object> res = new HashMap<String, Object>();
		URI uri = buildURI("https", host, path, args);
		HttpPost httpPost = new HttpPost(uri);

		try {
			addHeader(httpPost, header);
			long t1 = System.currentTimeMillis();
			CloseableHttpResponse response = httpsClient.execute(httpPost);
			long t2 = System.currentTimeMillis();

			res.put("responsetime", t2 - t1);

			try {
				getResponse(res, response);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				response.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			httpPost.releaseConnection();
			httpPost.abort();
		}
		return res;
	}

	/**
	 * 读取响应内容
	 * 
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	private static String getBody(HttpEntity entity) throws Exception {
		InputStream inStream = entity.getContent();
		String httpBody = null;
		try {
			Header encoding = entity.getContentEncoding();
			if (encoding != null) {
				httpBody = InputStreamUtils.InputStreamTOString(inStream, encoding.getValue());
			} else {
				httpBody = InputStreamUtils.InputStreamTOString(inStream);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			inStream.close();
		}

		return httpBody;

	}

	/**
	 * 构建uri
	 * 
	 * @param host
	 * @param path
	 * @param args
	 * @return
	 * @throws URISyntaxException
	 */
	private static URI buildURI(String scheme, String host, String path, Map<String, String> args)
			throws URISyntaxException {
		URI uri = null;
		URIBuilder uribd = new URIBuilder();
		uribd.setScheme(scheme);
		uribd.setHost(host);
		uribd.setPath(path);
		if (args != null) {
			for (String key : args.keySet()) {
				uribd.setParameter(key, args.get(key));
			}
		}
		uri = uribd.build();
		return uri;

	}

	/**
	 * 添加header
	 * 
	 * @param httprequest
	 * @param header
	 */
	private static void addHeader(HttpRequestBase httprequest, Map<String, String> header) {
		httprequest.addHeader(
				new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8"));
		httprequest.addHeader(new BasicHeader(HttpHeaders.ACCEPT,
				"text/*;q=0.3, text/html;q=0.7, text/html;level=1,text/html;level=2;q=0.4, */*;q=0.5"));
		if (header != null) {
			for (String name : header.keySet()) {
				httprequest.addHeader(new BasicHeader(name, header.get(name)));
			}
		}

	}

	/**
	 * 解析响应，分析响应头
	 * 
	 * @param res
	 * @param response
	 * @throws Exception
	 */
	private static void getResponse(Map<String, Object> res, CloseableHttpResponse response) throws Exception {
		Header[] headers = response.getAllHeaders();
		res.put("header", headers);
		HttpEntity entity = response.getEntity();
		res.put("entity", entity);
		if (entity != null) {
			res.put("response", getBody(entity));
		}
		int statusCode = response.getStatusLine().getStatusCode();
		res.put("httpstatus", statusCode);
	}

	public static Object[] postFile(Map<String, Object> fileList, String url, Map<String, String> params,
			String encoding) throws Exception {
		HttpPost httpost = new HttpPost(url);

		HttpEntity entity = null;

		List<File> fileToDelete = new ArrayList<File>();
		try {
			MultipartEntity reqEntity = new MultipartEntity();

			for (Map.Entry<String, Object> entry : fileList.entrySet()) {
				String fileName = entry.getKey();
				Object fileObj = entry.getValue();
				FileBody bin = new FileBody((File) fileObj);
				reqEntity.addPart(fileName, bin);
			}

			for (String name : params.keySet()) {
				String value = params.get(name);
				reqEntity.addPart(name, new StringBody(value, Charset.forName(encoding)));
			}
			httpost.setEntity(reqEntity);
			HttpResponse response;
			response = httpClient.execute(httpost);
			int status = response.getStatusLine().getStatusCode();

			entity = response.getEntity();
			String content = EntityUtils.toString(entity, encoding);

			Object[] result = new Object[2];
			result[0] = status;
			result[1] = content;
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (entity != null) {
				try {
					EntityUtils.consume(entity);
				} catch (IOException e) {

				}
			}
			if (fileToDelete.size() > 0) {
				for (File file : fileToDelete) {
					FileUtil.delete(file, true);
				}
			}
			httpost.releaseConnection();
			httpost.abort();
		}
	}

}
