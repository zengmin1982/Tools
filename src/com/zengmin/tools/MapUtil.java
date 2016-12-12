package com.zengmin.tools;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Map操作工具类
 * 
 * @author xufeng
 */
public abstract class MapUtil {
	/**
	 * 根据key，从Map里面获取字符串（自动类型转换）
	 * 
	 * @param m Map对象
	 * @param key 键
	 * @return 字符串
	 */
	public static String getString(Map m, Object key) {
		String value = null;
		if (m != null && !m.isEmpty() && key != null) {
			Object o = m.get(key);
			if (o != null) {
				if (o instanceof String) {
					value = (String) o;
				} else if (o instanceof String[]) {
					String[] ao = (String[]) o;
					if (ao.length > 0) {
						value = ao[0];
					}
				} else if (o instanceof byte[]) {
					value = new String((byte[]) o);
				} else {
					value = o.toString();
				}
			}
		}
		return value;
	}

	/**
	 * 根据key，从Map里面获取字符串（自动类型转换）
	 * 
	 * @param m Map对象
	 * @param key 键
	 * @param def 如果不存在，则返回此默认值
	 * @return 字符串
	 */
	public static String getString(Map m, Object key, String def) {
		String value = getString(m, key);
		if (value == null || value.trim().length() == 0) {
			value = def;
		}
		return value;
	}

	/**
	 * 根据key，从Map里面获取字符串数组（自动类型转换）
	 * 
	 * @param m Map对象
	 * @param key 键
	 * @return 字符串数组
	 */
	public static String[] getStringArray(Map m, Object key) {
		String[] value = null;
		if (m != null && !m.isEmpty() && key != null) {
			Object o = m.get(key);
			if (o != null) {
				if (o instanceof String[]) {
					value = (String[]) o;
				} else if (o instanceof String) {
					value = new String[] { (String) o };
				} else {
					value = new String[] { o.toString() };
				}
			}
		}
		return value;
	}

	/**
	 * 根据key，从Map里面获取Double对象（自动类型转换）
	 * 
	 * @param m Map对象
	 * @param key 键
	 * @param def 如果不存在，则返回此默认值
	 * @return Double对象
	 */
	public static Double getDouble(Map m, Object key, double def) {
		Double value = getDouble(m, key);
		return value == null ? def : value;
	}

	/**
	 * 根据key，从Map里面获取Double对象（自动类型转换）
	 * 
	 * @param m Map对象
	 * @param key 键
	 * @return Double对象
	 */
	public static Double getDouble(Map m, Object key) {
		Double value = null;
		if (m != null && !m.isEmpty() && key != null) {
			Object o = m.get(key);
			if (o != null) {
				if (o instanceof Number) {
					value = ((Number) o).doubleValue();
				} else if (o instanceof String) {
					String ao = (String) o;
					try {
						value = Double.parseDouble(ao);
					} catch (NumberFormatException e) {
					}
				} else {
					String ao = o.toString();
					try {
						value = Double.valueOf(ao);
					} catch (NumberFormatException e) {
					}
				}
			}
		}
		return value;
	}

	/**
	 * 根据key，从Map里面获取Long对象（自动类型转换）
	 * 
	 * @param m Map对象
	 * @param key 键
	 * @param def 如果不存在，则返回此默认值
	 * @return Long对象
	 */
	public static Long getLong(Map m, Object key, long def) {
		Long value = getLong(m, key);
		return value == null ? def : value;
	}

	/**
	 * 根据key，从Map里面获取Long对象（自动类型转换）
	 * 
	 * @param m Map对象
	 * @param key 键
	 * @return Long对象
	 */
	public static Long getLong(Map m, Object key) {
		Long value = null;
		if (m != null && !m.isEmpty() && key != null) {
			Object o = m.get(key);
			if (o != null) {
				if (o instanceof Number) {
					value = ((Number) o).longValue();
				} else if (o instanceof String) {
					String ao = (String) o;
					try {
						value = Long.parseLong(ao);
					} catch (NumberFormatException e) {
					}
				} else {
					String ao = o.toString();
					try {
						value = Double.valueOf(ao).longValue();
					} catch (NumberFormatException e) {
					}
				}
			}
		}
		return value;
	}

	/**
	 * 根据key，从Map里面获取整数（自动类型转换）
	 * 
	 * @param m Map对象
	 * @param key 键
	 * @return 整数
	 */
	public static int getInt(Map m, Object key) {
		return getInt(m, key, 0);
	}

	/**
	 * 根据key，从Map里面获取整数（自动类型转换）
	 * 
	 * @param m Map对象
	 * @param key 键
	 * @param def 如果不存在，则返回此默认值
	 * @return 整数
	 */
	public static int getInt(Map m, Object key, int def) {
		Integer value = getInteger(m, key);
		return value == null ? def : value;
	}

	/**
	 * 根据key，从Map里面获取整数（自动类型转换）
	 * 
	 * @param m Map对象
	 * @param key 键
	 * @param def 如果不存在，则返回此默认值
	 * @return 整数
	 */
	public static Integer getInteger(Map m, Object key) {
		Integer value = null;
		if (m != null && !m.isEmpty() && key != null) {
			Object o = m.get(key);
			if (o != null) {
				if (o instanceof Number) {
					value = ((Number) o).intValue();
				} else if (o instanceof String) {
					String ao = (String) o;
					try {
						value = Integer.parseInt(ao);
					} catch (NumberFormatException e) {
					}
				} else if (o instanceof Boolean) {
					value = ((Boolean) o) ? 1 : 0;
				} else {
					String ao = o.toString();
					try {
						value = Double.valueOf(ao).intValue();
					} catch (NumberFormatException e) {
					}
				}
			}
		}
		return value;
	}

	/**
	 * 根据key，从Map里面获取整数（自动类型转换）
	 * 
	 * @param m Map对象
	 * @param key 键
	 * @param def 如果不存在，则返回此默认值
	 * @return 整数
	 */
	public static Short getShort(Map m, Object key) {
		Short value = null;
		if (m != null && !m.isEmpty() && key != null) {
			Object o = m.get(key);
			if (o != null) {
				if (o instanceof Number) {
					value = ((Number) o).shortValue();
				} else if (o instanceof String) {
					String ao = (String) o;
					try {
						value = Short.parseShort(ao);
					} catch (NumberFormatException e) {
					}
				} else if (o instanceof Boolean) {
					value = ((Boolean) o) ? (short) 1 : (short) 0;
				} else {
					String ao = o.toString();
					try {
						value = Double.valueOf(ao).shortValue();
					} catch (NumberFormatException e) {
					}
				}
			}
		}
		return value;
	}

	/**
	 * 根据key，从Map里面获取二进制数组（自动类型转换）
	 * 
	 * @param m Map对象
	 * @param key 键
	 * @return 二进制数组
	 */
	public static byte[] getByteArray(Map m, Object key) {
		byte[] data = null;
		if (m != null && !m.isEmpty() && key != null) {
			Object o = m.get(key);
			if (o != null) {
				if (o instanceof byte[]) {
					data = (byte[]) o;
				} else if (o instanceof Collection) {
					Collection c = (Collection) o;
					data = new byte[c.size()];
					int index = 0;
					for (Object n : c) {
						data[index++] = n instanceof Number ? ((Number) n).byteValue() : 0;
					}
				} else if (o instanceof String) {
					data = ((String) o).getBytes();
				} else {
					data = o.toString().getBytes();
				}
			}
		}
		return data;
	}

	/**
	 * 根据key，从Map里面获取Boolean对象（自动类型转换）
	 * 
	 * @param m Map对象
	 * @param key 键
	 * @return Boolean对象
	 */
	public static Boolean getBoolean(Map m, Object key) {
		Boolean result = null;
		if (m != null && !m.isEmpty() && key != null) {
			Object o = m.get(key);
			if (o != null) {
				if (o instanceof Boolean) {
					result = ((Boolean) o);
				} else if (o instanceof String) {
					result = ((String) o).equalsIgnoreCase(Boolean.TRUE.toString());
				} else {
					result = o.toString().equalsIgnoreCase(Boolean.TRUE.toString());
				}
			}
		}
		return result;
	}

	/**
	 * 根据key，从Map里面获取boolean对象（自动类型转换）
	 * 
	 * @param m Map对象
	 * @param key 键
	 * @param def 如果不存在，则返回此默认值
	 * @return boolean对象
	 */
	public static boolean getBoolean(Map m, Object key, boolean def) {
		Boolean result = getBoolean(m, key);
		return result == null ? def : result;
	}

	/**
	 * 根据key，从Map里面获取Boolean对象，并转化成0/1（自动类型转换）
	 * 
	 * @param m Map对象
	 * @param key 键
	 * @return false:0 true:1
	 */
	public static Integer getBooleanInteger(Map m, Object key) {
		Boolean value = getBoolean(m, key);
		return value == null ? null : (value ? 1 : 0);
	}

	/**
	 * 根据key，从Map里面获取Boolean对象，并转化成0/1（指定true值的形式）
	 * 
	 * @param m Map对象
	 * @param key 键
	 * @return false:0 true:1
	 */
	public static Integer getBooleanInteger(Map m, Object key, String trueValue) {
		Integer result = null;
		if (m != null && !m.isEmpty() && key != null) {
			Object o = m.get(key);
			if (o != null) {
				if (o instanceof Boolean) {
					result = ((Boolean) o) ? 1 : 0;
				} else if (o instanceof String) {
					result = ((String) o).equalsIgnoreCase(trueValue) ? 1 : 0;
				} else {
					result = o.toString().equalsIgnoreCase(trueValue) ? 1 : 0;
				}
			}
		}
		return result;
	}

	/**
	 * 根据key，从Map里面获取Date对象，并根据指定格式转化成字符串
	 * 
	 * @param m Map对象
	 * @param key 键
	 * @param fmt 格式
	 * @return 日期字符串
	 */
	public static String getDate(Map m, Object key, String fmt) {
		String result = null;
		if (m != null && !m.isEmpty() && key != null) {
			Object o = m.get(key);
			if (o != null) {
				if (o instanceof Date) {
					result = DateUtil.formatDate((Date) o, fmt);
				} else if (o instanceof String) {
					result = (String) o;
				} else {
					result = o.toString();
				}
			}
		}
		return result;
	}

	/**
	 * 根据key，从Map里面获取数组对象（自动类型转换）
	 * 
	 * @param m Map对象
	 * @param key 键
	 * @return 数组对象
	 */
	public static Object[] getArray(Map m, Object key) {
		Object[] value = null;
		if (m != null && !m.isEmpty() && key != null) {
			Object o = m.get(key);
			if (o != null) {
				if (o instanceof Object[]) {
					value = (Object[]) o;
				} else if (o instanceof Collection) {
					value = ((Collection) o).toArray();
				} else if (o.getClass().isArray()) {
					value = new Object[Array.getLength(o)];
					for (int i = 0; i < value.length; i++) {
						value[i] = Array.get(o, i);
					}
				} else {
					value = new Object[] { o };
				}
			}
		}
		return value;
	}

	/**
	 * 根据指定key列表，从Map里面获取对应的对象并装配成数组对象返回
	 * 
	 * @param m Map对象
	 * @param key 键列表
	 * @return 数组对象
	 */
	public static Object[] getArray(Map m, List keys) {
		Object[] value = null;
		if (m != null && !m.isEmpty() && keys != null && !keys.isEmpty()) {
			int len = keys.size();
			value = new Object[len];
			for (int i = 0; i < len; i++) {
				value[i] = m.get(keys.get(i));
			}
		}
		return value;
	}

	/**
	 * 获取子Map，即根据指定key列表，从Map里面获取对应的对象并装配成Map对象返回
	 * 
	 * @param m Map对象
	 * @param key 键列表
	 * @return 子Map对象
	 */
	public static Map getMap(Map m, List keys) {
		Map value = new LinkedHashMap();
		if (m != null && !m.isEmpty() && keys != null && !keys.isEmpty()) {
			for (Object key : keys) {
				value.put(key, m.get(key));
			}
		}
		return value;
	}

	/**
	 * 获取子Map，即根据指定key列表，从Map里面获取对应的对象并装配成Map对象返回
	 * 
	 * @param m Map对象
	 * @param key 键列表
	 * @return 子Map对象
	 */
	public static Map getMap(Map m, Object[] keys) {
		Map value = new LinkedHashMap();
		if (m != null && !m.isEmpty() && keys != null && keys.length > 0) {
			for (Object key : keys) {
				value.put(key, m.get(key));
			}
		}
		return value;
	}

	/**
	 * 获取子Map，即根据指定key列表，从Map里面获取对应的对象并根据新键值装配成Map对象返回
	 * 
	 * @param m Map对象
	 * @param keysFrom 原键列表
	 * @param keysTo 新键列表
	 * @return 子Map对象
	 */
	public static Map getMap(Map m, Object[] keysFrom, Object[] keysTo) {
		Map value = new LinkedHashMap();
		if (m != null && !m.isEmpty() && keysFrom != null && keysFrom.length > 0 && keysTo != null && keysTo.length == keysFrom.length) {
			for (int i = 0; i < keysFrom.length; i++) {
				value.put(keysTo[i], m.get(keysFrom[i]));
			}
		}
		return value;
	}

	/**
	 * Map之间复制某个Key的值
	 * 
	 * @param src 源Map
	 * @param dest 目标Map
	 * @param keyFrom 源Map的Key
	 * @param keyTo 目标Map的Key
	 * @param ct 数据类型
	 * @param def 默认值
	 */
	public static <T> void copy(Map src, Map dest, String keyFrom, String keyTo, Class<T> ct, T def) {
		T value = relativeGet(src, keyFrom, ct);
		if (value == null) {
			value = def;
		}
		if (value != null) {
			relativeSet(dest, keyTo, value);
		}
	}

	public static <T> void copy(Map src, Map dest, String key, Class<T> ct) {
		copy(src, dest, key, key, ct, null);
	}

	public static <T> void copy(Map src, Map dest, String key, Class<T> ct, T def) {
		copy(src, dest, key, key, ct, def);
	}

	public static <T> void copy(Map src, Map dest, String keyFrom, String keyTo, Class<T> ct) {
		copy(src, dest, keyFrom, keyTo, ct, null);
	}

	/**
	 * 根据指定的key列表，比较两个Map（String方式比较）
	 * 
	 * @param map1 Map对象1
	 * @param map2 Map对象2
	 * @param keys key列表
	 * @return 比较结果
	 */
	public static int compareMap(Map map1, Map map2, String[] keys) {
		int rst = -2;
		if (map1 != null && map2 != null && keys != null && keys.length > 0) {
			for (int i = 0, len = keys.length; i < len; i++) {
				rst = StringUtil.nvl(getString(map1, keys[i])).compareTo(StringUtil.nvl(getString(map2, keys[i])));
				if (rst != 0) {
					break;
				}
			}
		}
		return rst;
	}

	/**
	 * 级联赋值，级间用dot分隔
	 * 
	 * @param map
	 * @param key
	 * @param value
	 */
	public static void relativeSet(Map map, String key, Object value) {
		if (map != null && key != null && value != null) {
			int pos = key.indexOf('.');
			if (pos > 0 && pos < key.length()) {
				String sub = key.substring(0, pos);
				Object so = map.get(sub);
				if (so == null || !(so instanceof Map)) {
					so = new LinkedHashMap();
					map.put(sub, so);
				}
				relativeSet((Map) so, key.substring(pos + 1), value);
			} else {
				map.put(key, value);
			}
		}
	}

	/**
	 * 级联取值，级间用dot分隔
	 * 
	 * @param map
	 * @param key
	 * @param ct
	 * @return
	 */
	public static <T> T relativeGet(Map map, String key, Class<T> ct) {
		T value = null;
		if (map != null && key != null) {
			int pos = key.indexOf('.');
			if (pos > 0 && pos < key.length()) {
				String sub = key.substring(0, pos);
				Map sm = getObject(map, sub, Map.class);
				if (sm != null) {
					value = relativeGet(sm, key.substring(pos + 1), ct);
				}
			} else {
				value = getObject(map, key, ct);
			}
		}
		return value;
	}

	/**
	 * 空判断
	 * 
	 * @param map
	 * @return
	 */
	public static boolean isEmpty(Map map) {
		return map == null || map.isEmpty();
	}

	/**
	 * 非空判断
	 * 
	 * @param map
	 * @return
	 */
	public static boolean isNotEmpty(Map map) {
		return map != null && !map.isEmpty();
	}

	/**
	 * 从Map中取得指定类型的对象（自动类型转换）（Integer类型默认值是0，其它类型无默认值）
	 * 
	 * @param map Map对象
	 * @param key Key
	 * @param ct 数据类型
	 * @return
	 */
	public static <T> T getObject(Map map, Object key, Class<T> ct) {
		T value = null;
		if (map != null && key != null) {
			if (ct != null) {
				if (String.class.equals(ct)) {
					value = (T) getString(map, key);
				} else if (Boolean.class.equals(ct)) {
					value = (T) getBoolean(map, key);
				} else if (Integer.class.equals(ct)) {
					value = (T) (Integer) getInt(map, key);
				} else if (Long.class.equals(ct)) {
					value = (T) getLong(map, key);
				} else if (Double.class.equals(ct)) {
					value = (T) getDouble(map, key);
				} else if (Short.class.equals(ct)) {
					value = (T) getShort(map, key);
				} else {
					Object v = map.get(key);
					if (v != null && ct.isInstance(v)) {
						value = (T) v;
					}
				}
			} else {
				value = (T) map.get(key);
			}
		}
		return value;
	}
}
