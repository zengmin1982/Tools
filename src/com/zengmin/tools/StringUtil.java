package com.zengmin.tools;

import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

/**
 * String util工具类
 * 
 * @author pangkc
 *
 */
public class StringUtil {
    
    private static final String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式  
    private static final String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式  
    private static final String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式  
    private static final String regEx_space = "\\s*|\t|\r|\n";//定义空格回车换行符 
    
    public static final String ENCODE_UTF8 = "utf-8";
      
    
    public static boolean isEmpty(String... str) {
        boolean empty = true;
        if (str != null) {
            for (String s : str) {
                empty = empty && (s == null || s.trim().length() == 0);
            }
        }
        return empty;
    }

    /** 
     * @param htmlStr 
     * @return 
     *  删除Html标签 
     */  
    public static String delHTMLTag(String htmlStr) {  
        Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);  
        Matcher m_script = p_script.matcher(htmlStr);  
        htmlStr = m_script.replaceAll(""); // 过滤script标签  
  
        Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);  
        Matcher m_style = p_style.matcher(htmlStr);  
        htmlStr = m_style.replaceAll(""); // 过滤style标签  
  
        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);  
        Matcher m_html = p_html.matcher(htmlStr);  
        htmlStr = m_html.replaceAll(""); // 过滤html标签  
  
        Pattern p_space = Pattern.compile(regEx_space, Pattern.CASE_INSENSITIVE);  
        Matcher m_space = p_space.matcher(htmlStr);  
        htmlStr = m_space.replaceAll(""); // 过滤空格回车标签  
        return htmlStr.trim(); // 返回文本字符串  
    }  
      
    public static String getTextFromHtml(String htmlStr){  
        htmlStr = delHTMLTag(htmlStr);  
        htmlStr = htmlStr.replaceAll("&nbsp;", "");  
        return htmlStr;  
    }  
    /**
     * 空字符串
     */
    public static final String emptyString = "";

    /**
     * 是否是空字符串
     * 
     * @param value
     *            判断源
     * @return true or false
     */
    public static boolean isNullOrEmpty(String value) {
        return null == value || emptyString.equals(value);
    }

    /**
     * 是否是数字
     * 
     * @param str
     *            源
     * @return true or false
     */
    public static boolean isNumber(String str) {
        Pattern p = Pattern.compile("^[0-9]{14}");
        return str == null ? false : p.matcher(str).matches();
    }

    /**
     * 拼接字符串
     * 
     * @param tag
     *            自定义前缀
     * @param data
     *            拼接源数据
     * @param split
     *            分隔符
     * @return {"user", "login"} 和 {某某某， "成功"} 返回 "用户 分隔符某某某分隔符login分隔符成功分隔符"
     */
    public static String contactStr(String[] tag, String[] data, String split) {
        if (tag.length == 0 || data.length == 0 || tag.length != data.length) {
            return emptyString;
        }
        if (StringUtil.isNullOrEmpty(split)) {
            split = emptyString;
        }
        StringBuilder result = new StringBuilder();
        int length = tag.length;
        for (int i = 0; i < length; i++) {
            result.append(tag[i]).append(split).append(data[i]).append(split);
        }
        return result.toString();
    }

    /**
     * 拼接字符串
     * 
     * @param arr
     *            源
     * @param split
     *            分隔符
     * @return {"user", "login"} 和 {某某某， "成功"} 返回 "用户 分隔符某某某分隔符login分隔符成功分隔符"
     */
    public static String contactStr(String[] arr, String split) {
        if (arr == null || arr.length == 0) {
            return emptyString;
        }
        if (StringUtil.isNullOrEmpty(split)) {
            split = emptyString;
        }
        StringBuilder result = new StringBuilder();
        int length = arr.length;
        for (int i = 0; i < length - 1; i++) {
            result.append(arr[i]).append(split);
        }
        result.append(arr[length - 1]).append(arr[length - 1]);
        return result.toString();
    }

    /**
     * 拼接字符串
     * 
     * @param tag
     *            自定义前缀
     * @param data
     *            拼接源数据
     * @param split
     *            分隔符
     * @return {"user", "login"} 和 {某某某， "成功"} 返回 "用户 分隔符某某某分隔符login分隔符成功分隔符"
     */
    public static String contactStr(Set<String> tag, String[] data, String split) {
        if (tag == null || data == null || tag.size() != data.length) {
            return emptyString;
        }
        if (StringUtil.isNullOrEmpty(split)) {
            split = emptyString;
        }
        StringBuilder result = new StringBuilder();
        int length = tag.size();
        Object[] tagObjArr = tag.toArray();
        for (int i = 0; i < length; i++) {
            result.append((String) tagObjArr[i]).append(split).append(data[i]).append(split);
        }
        return result.toString();
    }
    /**
     * 查询关键字匹配
     * 完全匹配 则结果+1
     * @param keys      源关键字
     * @param key       用户输入    匹配对象
     * @param div       分隔符
     * @return 默认为0 表示没有匹配  如果大于1 则说明在一组内出现多个完全匹配
     */
    public static int getcontainskey(String keys, String key, String div){
        int result = 0;
        String[] ss = keys.split(div);

        for (int i=0; i<ss.length; i++){
            if (ss[i].equals(key)) result++;
        }

        return result;
    }
    public static boolean isNotEmpty(String... str) {
        boolean notEmpty = false;
        if (str != null) {
            for (String s : str) {
                notEmpty = notEmpty || (s != null && s.trim().length() > 0);
            }
        }
        return notEmpty;
    }
    public static String nvl(Object o) {
        return nvl(o, "");
    }

    public static String nvl(Object o, String def) {
        return o == null ? def : o instanceof String ? (String) o : o.toString();
    }
    public static String join(String[] strs, String sep) {
        StringBuffer sb = new StringBuffer();
        if (strs != null && strs.length > 0) {
            sb.append(strs[0]);
            if (strs.length > 1) {
                for (int i = 1; i < strs.length; i++) {
                    sb.append(sep).append(strs[i]);
                }
            }
        }
        return sb.toString();
    }
	public static String base64Decode(String content){
		return new String(Base64.decode(content));
	}
	public static String base64Encode(String content){
		return new String(Base64.encode(content.getBytes()));
	}
	/**
	 * 字符串编码
	 * @param content
	 * @param charset
	 * @return
	 */
	public static String encodeString(String content,String charset){
		String result ="";
		try {
			result = new String(content.getBytes(),charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

}
