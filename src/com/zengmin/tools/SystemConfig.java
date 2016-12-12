package com.zengmin.tools;

/**
 * 系统配置
 * @author pangkc
 *
 */
public abstract class SystemConfig {

    public static String TOKEN;

    static {
        try {
            TOKEN = SpringPropertyResourceReader.getProperty("system.config.token");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
