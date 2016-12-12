package com.zengmin.tools;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HTTPBase {
	public static Map<String, Object> readContentFromGet(String URL,
			Map<String, String> args, String Cookie) throws IOException {
		// 拼凑get请求的URL字串，使用URLEncoder.encode对特殊和不可见字符进行编码
		String getURL = URL;
		Iterator iter = args.entrySet().iterator();
		if (args.size() > 0) {
			getURL = getURL + "?";
		}
		// 拼接参数，根据arg传入的参数，按key=value的样式拼接参数
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			String val = (String) entry.getValue();
			getURL = getURL + "&" + key + "=" + URLEncoder.encode(val, "utf-8");
		}
		URL getUrl = new URL(getURL);
		// 根据拼凑的URL，打开连接，URL.openConnection()函数会根据
		// URL的类型，返回不同的URLConnection子类的对象，在这里我们的URL是一个http，因此它实际上返回的是HttpURLConnection
		HttpURLConnection connection = (HttpURLConnection) getUrl
				.openConnection();
		// 设置cookie值
		if (Cookie != "") {
			connection.setRequestProperty("Cookie", Cookie);
		}
		// 建立与服务器的连接，并未发送数据
		connection.connect();
		// 发送数据到服务器并使用Reader读取返回的数据
		InputStream reader = connection.getInputStream();
		// 断开连接
		// connection.disconnect();
		Map<String, Object> httpresult = new HashMap();
		httpresult.put("HttpURLConnection", connection);
		httpresult.put("InputStream", reader);

		return httpresult;

	}

	public static Map<String, Object> readContentFromPost(String URL,
			Map<String, String> args, String Cookie) throws IOException {
		// Post请求的url，与get不同的是不需要带参数
		URL postUrl = new URL(URL);
		// 打开连接
		HttpURLConnection connection = (HttpURLConnection) postUrl
				.openConnection();
		// 设置cookie值
		if (Cookie != "") {
			connection.setRequestProperty("Cookie", Cookie);
		}
		// 打开读写属性，默认均为false
		connection.setDoOutput(true);
		connection.setDoInput(true);
		// 设置请求方式，默认为GET
		connection.setRequestMethod("POST");
		// Post 请求不能使用缓存
		connection.setUseCaches(false);
		// URLConnection.setInstanceFollowRedirects 是成员函数，仅作用于当前函数
		connection.setInstanceFollowRedirects(true);
		// 配置连接的Content-type，配置为application/x-
		// www-form-urlencoded的意思是正文是urlencoded编码过的form参数，下面我们可以看到我们对正文内容使用URLEncoder.encode进行编码
		// connection.setRequestProperty("Content-Type",
		// "application/x-www-form-urlencoded");
		// 连接，从postUrl.openConnection()至此的配置必须要在 connect之前完成，
		// 要注意的是connection.getOutputStream()会隐含的进行调用 connect()，所以这里可以省略
		// connection.connect();
		connection.setRequestProperty("Content-type",
				"application/x-www-form-urlencoded;charset=UTF-8");
		DataOutputStream out = new DataOutputStream(
				connection.getOutputStream());
		// 加入参数
		Iterator iter = args.entrySet().iterator();
		String content = "";
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			String val = (String) entry.getValue();
			content = content + key + "=" + URLEncoder.encode(val, "utf-8")
					+ "&";
			// System.out.println(content);
		}
		// System.out.println(content.subSequence(0, (content.length() - 1)));
		content = (String) content.subSequence(0, (content.length() - 1));
		// System.out.println(content);
		out.writeBytes(content);
		out.flush();
		out.close(); // flush and close
		InputStream reader = connection.getInputStream();
		System.out.println(connection.getHeaderFields());
		Map<String, Object> httpresult = new HashMap();
		httpresult.put("HttpURLConnection", connection);
		httpresult.put("InputStream", reader);

		return httpresult;

	}


}
