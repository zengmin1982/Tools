package com.zengmin.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

//import com.gw.util.SystemConfig;

public final class Constants {

	public static final String BLANK = "";

	// 常用中文编码
	public static final String ENCODE_UTF8 = "utf-8";
	public static final String ENCODE_GBK = "gbk";
	public static final String ENCODE_GB2312 = "gb2312";
	public static final String ENCODE_ISO88591 = "iso8859-1";
	public static final String DOUHAO = ",";

	// post文件到CND相关设置
	public static final String POST_CND_FILE = "file"; // 上传文件到cdn的文件参数key
	public static final String POST_CND_DES = "destination"; // 上传文件到cdn的文件地址key
	public static final String POST_CND_FILE_XML = "xml"; // 上传文件到cdn的文件xml格式
	public static final String POST_CND_FILE_TYPE = "type"; // 上传文件到cdn的文件格式
	public static final String POST_CND_FILE_LIST = "list"; // 上传文件到cdn的分页数据list1
															// list2..等等文件名称
	public static final String POST_CND_FILE_JSON = "json"; // 上传文件到cdn的json文件
	public static final String POST_TEMP_SERVICE = "receiveResourceAndSaveToTemp.php"; // 上传到cdn临时目录的service
	public static final String POST_COPYTEMP_SERVICE = "copyFromTempToCdn.php"; // 将cdn临时目录copy到正式目录的service
	public static final String POST_CDN_SERVICE = "receiveResourceAndSaveToCdn.php"; // 上传到cdn目录的service

	public static final DateFormat dateFormat = new SimpleDateFormat(
			"yyyyMMddHHmmss");
}
