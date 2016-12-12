package com.zengmin.tools;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sf.ezmorph.object.DateMorpher;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.util.JSONUtils;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONLibDataFormatSerializer;
import com.alibaba.fastjson.serializer.JSONSerializerMap;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * json操作类
 * 
 * @author pangkc
 */
public class JsonUtil {

	private static final Logger log = Logger.getLogger(JsonUtil.class);

	private static final JSONSerializerMap mapping;

	static {
		mapping = new JSONSerializerMap();
		mapping.put(Date.class, new JSONLibDataFormatSerializer()); // 使用和json-lib兼容的日期输出格式
	}

	private static final SerializerFeature[] features = {
			SerializerFeature.WriteMapNullValue, // 输出空置字段
			SerializerFeature.WriteNullListAsEmpty, // list字段如果为null，输出为[]，而不是null
			SerializerFeature.WriteNullNumberAsZero, // 数值字段如果为null，输出为0，而不是null
			SerializerFeature.WriteNullBooleanAsFalse, // Boolean字段如果为null，输出为false，而不是null
			SerializerFeature.WriteNullStringAsEmpty // 字符类型字段如果为null，输出为""，而不是null
	};
	
	public static <T> T parseObject(String json,Class<T> clazz){
	    T object = JSON.parseObject(json, clazz);
		return object;
	}
	
	public static String toJSONString(Object object){
		return JSON.toJSONString(object);
	}
	/**
	 * json to object
	 * 
	 * @param object
	 * @param clazz
	 * @return
	 */
	public static Object jsonToObject(String str, Class clazz) throws Exception {

		return JSON.parseObject(str, clazz);
	}

	/**
	 * json to object list
	 * 
	 * @param str
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public static List jsonToObjectList(String str, Class clazz)
			throws Exception {

		return JSON.parseArray(str, clazz);
	}

	public static final JsonConfig cfg = new JsonConfig();
	static {
		cfg.registerJsonValueProcessor(java.util.Date.class,
				new JsonDateValueProcessor());
		cfg.registerJsonValueProcessor(java.sql.Date.class,
				new JsonDateValueProcessor());
		cfg.registerJsonValueProcessor(java.sql.Timestamp.class,
				new JsonDateValueProcessor());
	}

	/**
	 * 解析复杂json内容
	 * 
	 * @param str
	 * @return 返回map对象
	 */
	@SuppressWarnings("unchecked")
	public static Map jsonToMap(String str) {

		List<Map> list = JsonUtil.jsonToMapList(str, Constants.BLANK);
		if (list == null || list.size() == 0) {
			return null;
		} else if (list.size() == 1) {
			return list.get(0);
		} else {
			throw new RuntimeException("json is array");
		}
	}

	/**
	 * 解析复杂json内容
	 * 
	 * @param str
	 * @return 返回map集合
	 */
	@SuppressWarnings("unchecked")
	public static List<Map> jsonToMapList(String str) {

		return JsonUtil.jsonToMapList(str, Constants.BLANK);
	}

	/**
	 * 解析复杂json内容
	 * 
	 * @param str
	 * @param replaceNullChara数据为空时用replaceNullChara替换
	 *            ""或者null
	 * @return 返回map集合
	 */
	@SuppressWarnings("unchecked")
	public static List<Map> jsonToMapList(String str, String replaceNullChara) {
		List result = new ArrayList();
		if (str.startsWith("[")) {
			JSONArray array = JSONArray.fromObject(str);
			result = JsonUtil.getMapByJSONArray(array, replaceNullChara);
		} else {
			// 单个json数据
			JSONObject jsonObj = JSONObject.fromObject(str);
			result.add(JsonUtil.getMapByJSONObject(jsonObj, replaceNullChara));
		}
		return result;
	}

	/**
	 * Object 对应的json字符串
	 * 
	 * @param obj
	 * @return
	 */
	public static String javaBeanToJson(Object data) {
		String result = null;
		if (data != null) {
			if (data instanceof Collection || data instanceof Object[]) {
				result = JSONArray.fromObject(data, cfg).toString();
			} else {
				result = JSONObject.fromObject(data, cfg).toString();
			}
		}
		return result == null ? "{}" : result;
	}

	/**
	 * Object 对应的JSONObject
	 * 
	 * @param obj
	 * @return JSONObject
	 */
	public static JSONObject javaBeanToJsonObject(Object data) {
		JSONObject result = null;
		if (data != null) {
			result = JSONObject.fromObject(data, cfg);
		}
		return result == null ? new JSONObject() : result;
	}

	/**
	 * 根据JSONObject 解析json
	 */
	@SuppressWarnings("unchecked")
	private static Map getMapByJSONObject(JSONObject jsonObj,
			String replaceNullChara) {
		Map result = new HashMap();
		Iterator<String> ite = jsonObj.keySet().iterator();
		while (ite.hasNext()) {
			String key = ite.next();
			Object obj = jsonObj.get(key);
			if (obj instanceof JSONObject) {
				JSONObject innerjsonObj = (JSONObject) obj;
				if (innerjsonObj.isNullObject() || innerjsonObj.isEmpty()) {
					result.put(key, replaceNullChara);
				} else {
					Map mapObj = getMapByJSONObject(innerjsonObj,
							replaceNullChara);
					result.put(key, mapObj);
				}
			} else if (obj instanceof JSONArray) {
				List list = getMapByJSONArray((JSONArray) obj, replaceNullChara);
				result.put(key, list);
			} else {
				if (obj == null) {
					result.put(key, replaceNullChara);
				} else {
					result.put(key, obj.toString());
				}
			}
		}
		return result;
	}

	/**
	 * 根据JSONArray 解析json
	 * 
	 * @param jsonArr
	 * @param replaceNullChara
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static List getMapByJSONArray(JSONArray jsonArr,
			String replaceNullChara) {
		List list = new ArrayList();
		for (int i = 0; i < jsonArr.size(); i++) {
			Object obj = jsonArr.get(i);
			if (obj instanceof JSONObject) {
				Map mapObj = getMapByJSONObject(jsonArr.getJSONObject(i),
						replaceNullChara);
				list.add(mapObj);
			} else if (obj instanceof JSONArray) {
				JSONArray innerJsonArr = (JSONArray) obj;
				List innerList = getMapByJSONArray(innerJsonArr,
						replaceNullChara);
				list.add(innerList);
			} else if (obj == null) {
				list.add(replaceNullChara);
			} else {
				list.add(obj.toString());
			}
		}
		return list;
	}

	/**
	 * 从一个JSON字符串得到一个简单java对象，
	 * 
	 * @param object
	 * @param clazz
	 * @return
	 */
	public static Object jsonStringToObject(String jsonString, Class clazz)
			throws Exception {
		JSONObject jsonObject = null;
		setDataFormat2JAVA();
		jsonObject = JSONObject.fromObject(jsonString);
		return JSONObject.toBean(jsonObject, clazz);
	}

	/**
	 * 
	 * @param jsonString
	 * @param clazz
	 * @param map
	 * @return
	 * @throws Exception
	 *             Object
	 */
	public static Object jsonStringToObject(String jsonString, Class clazz,
			Map map) throws Exception {
		JSONObject jsonObject = null;
		setDataFormat2JAVA();
		jsonObject = JSONObject.fromObject(jsonString);
		return JSONObject.toBean(jsonObject, clazz, map);
	}

	/**
	 * 从一个JSON字符串得到一个简单List集合
	 * 
	 * @param object
	 * @param clazz
	 * @return
	 */
	public static List jsonArrToList(String jsonString, Class clazz)
			throws Exception {
		setDataFormat2JAVA();
		JSONArray array = JSONArray.fromObject(jsonString);
		List list = new ArrayList();
		for (Iterator iter = array.iterator(); iter.hasNext();) {
			JSONObject jsonObject = (JSONObject) iter.next();
			list.add(JSONObject.toBean(jsonObject, clazz));
		}
		return list;
	}

	private static void setDataFormat2JAVA() {
		// 设定日期转换格式
		JSONUtils.getMorpherRegistry().registerMorpher(
				new DateMorpher(new String[] { "yyyy-MM-dd",
						"yyyy-MM-dd HH:mm:ss" }));
	}

	private static class JsonDateValueProcessor implements JsonValueProcessor {

		/**
		 * datePattern
		 */
		private String datePattern = "yyyy-MM-dd HH:mm:ss";

		/**
		 * JsonDateValueProcessor
		 */
		public JsonDateValueProcessor() {
			super();
		}

		/**
		 * @param format
		 */
		public JsonDateValueProcessor(String format) {
			super();
			this.datePattern = format;
		}

		/**
		 * @param value
		 * @param jsonConfig
		 * @return Object
		 */
		public Object processArrayValue(Object value, JsonConfig jsonConfig) {
			return process(value);
		}

		/**
		 * @param key
		 * @param value
		 * @param jsonConfig
		 * @return Object
		 */
		public Object processObjectValue(String key, Object value,
				JsonConfig jsonConfig) {
			return process(value);
		}

		/**
		 * process
		 * 
		 * @param value
		 * @return
		 */
		private Object process(Object value) {
			try {
				if (value instanceof Date) {
					SimpleDateFormat sdf = new SimpleDateFormat(datePattern,
							Locale.CHINA);
					return sdf.format((Date) value);
				} else if (value instanceof java.sql.Date) {
					java.sql.Date sqlDate = (java.sql.Date) value;
					Date d = new Date(sqlDate.getTime());
					SimpleDateFormat sdf = new SimpleDateFormat(datePattern,
							Locale.CHINA);
					return sdf.format((Date) d);
				} else if (value instanceof java.sql.Timestamp) {
					java.sql.Timestamp timeStamp = (java.sql.Timestamp) value;
					Date d = new Date(timeStamp.getTime());
					SimpleDateFormat sdf = new SimpleDateFormat(datePattern,
							Locale.CHINA);
					return sdf.format((Date) d);
				}
				return value == null ? "" : value.toString();
			} catch (Exception e) {
				return "";
			}

		}

		/**
		 * @return the datePattern
		 */
		public String getDatePattern() {
			return datePattern;
		}

		/**
		 * @param pDatePattern
		 *            the datePattern to set
		 */
		public void setDatePattern(String pDatePattern) {
			datePattern = pDatePattern;
		}

	}


}
