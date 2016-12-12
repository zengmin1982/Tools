package com.zengmin.tools;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.StringValueResolver;

/**
 * 配置文件管理工具
 * 
 * @author xufeng
 */
public abstract class ConfigUtil {
    private static Logger LOG = LoggerFactory.getLogger(ConfigUtil.class);
	private static Map<String, Properties> _s_configs = new ConcurrentHashMap<String, Properties>();
    private static Map<Class, Properties> _c_configs = new ConcurrentHashMap<Class, Properties>();
    private static List<StringValueResolver> _resolvers = new ArrayList<StringValueResolver>();

    public static String getConfig(String key) {
        String value = null;
        if (StringUtil.isNotEmpty(key)) {
            String expr = key.startsWith("${") && key.endsWith("}") ? key : "${" + key + "}";
            for (StringValueResolver resolver : _resolvers) {
                value = resolver.resolveStringValue(expr);
                if (value != null && !value.equals(expr)) {
                    return value;
                }
            }
        }
        return value;
    }

    public static String getConfig(String namespace, String key) {
        return getConfigs(namespace).getProperty(key);
    }

    public static String getConfig(String namespace, String key, String def) {
        return getConfigs(namespace).getProperty(key, def);
    }

    public static String getConfig(Class clz, String key) {
        return getConfigs(clz).getProperty(key);
    }

    public static String getConfig(Class clz, String key, String def) {
        return getConfigs(clz).getProperty(key, def);
    }

    public static void setConfig(String namespace, String key, String value) {
        getConfigs(namespace).setProperty(key, value);
    }

    public static void setConfig(Class clz, String key, String value) {
        getConfigs(clz).setProperty(key, value);
    }

    public static Properties getConfigs(String namespace) {
        Properties prop = _s_configs.get(namespace);
        if (prop == null) {
            prop = loadConfig(namespace);
            _s_configs.put(namespace, prop);
        }
        return prop;
    }

    public static void addStringValueResolver(StringValueResolver resolver) {
        _resolvers.add(resolver);
    }

    private static Properties loadConfig(String namespace) {
        Properties prop = new Properties();
        loadConfig(prop, namespace, false);
        loadConfig(prop, namespace, true);
        return prop;
    }

    private static boolean loadConfig(Properties prop, String namespace, boolean isLocal) {
        boolean isLoaded = false;
        InputStream is = null;
        try {
            String path = (isLocal ? System.getProperty("CONFIG_PATH", "classpath:conf") : "classpath:conf") + "/" + namespace;
            // URL url = ConfigUtil.class.getClassLoader().getResource(path + ".properties");
            DefaultResourceLoader loader = new DefaultResourceLoader();
            Resource res = loader.getResource(path + ".properties");
            if (res != null && res.exists() && res.isReadable()) {
                is = res.getInputStream();
                if (is != null) {
                    prop.load(is);
                    isLoaded = true;
                }
            }
        } catch (Exception e) {
            LOG.error("ERROR IN loadConfig:" + namespace, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {}
            }
        }
        return isLoaded;
    }

    public static Properties getConfigs(Class clz) {
        Properties prop = _c_configs.get(clz);
        if (prop == null) {
            prop = loadConfig(clz);
            _c_configs.put(clz, prop);
        }
        return prop;
    }

    private static Properties loadConfig(Class clz) {
        Properties prop = new Properties();
        InputStream is = null;
        try {
            URL url = clz.getResource(clz.getSimpleName() + ".properties");
            if (url != null) {
                is = url.openStream();
                if (is != null) {
                    prop.load(is);
                }
            }
        } catch (Exception e) {
            LOG.error("ERROR IN loadConfig:" + clz.getName() + ".properties", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {}
            }
        }
        return prop;
    }
}
