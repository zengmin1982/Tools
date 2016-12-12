package com.zengmin.tools;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

//import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.SafeEncoder;

public class JedisUtil {
	private final static Logger log =LoggerFactory.getLogger(JedisUtil.class);
	/** 缓存生存时间 */
	private final int expire = 60000;
	private static final JedisUtil jedisUtil = new JedisUtil();
	private static JedisPool jedisPool = null;

	private JedisUtil() {

	}

	static {
	    poolInit();
	}
	 private static void initialPool(){
	     try {

             // 创建jedis池配置实例
             JedisPoolConfig config = new JedisPoolConfig();
             // 设置池配置项值
             //最大分配的对象数
             config.setMaxTotal(Integer.valueOf(SpringPropertyResourceReader.getProperty("redis.pool.maxTotal")));
             //最大能够保持idel状态的对象数  
             config.setMaxIdle(Integer.valueOf(SpringPropertyResourceReader.getProperty("redis.pool.maxIdle")));
             //当池内没有返回对象时，最大等待时间  
             config.setMaxWaitMillis(Integer.valueOf(SpringPropertyResourceReader.getProperty("redis.pool.maxWaitMillis")));
             //当调用borrow Object方法时，是否进行有效性检查
             config.setTestOnBorrow(Boolean.valueOf(SpringPropertyResourceReader.getProperty("redis.pool.testOnBorrow")));
             //当调用return Object方法时，是否进行有效性检查 
             config.setTestOnReturn(Boolean.valueOf(SpringPropertyResourceReader.getProperty("redis.pool.testOnReturn")));
             // 根据配置实例化jedis池
             jedisPool = new JedisPool(config, 
            		 SpringPropertyResourceReader.getProperty("redis.ip"),
            		 Integer.valueOf(SpringPropertyResourceReader.getProperty("redis.port")),
            		 Integer.valueOf(SpringPropertyResourceReader.getProperty("redis.timeout")));

         } catch (Exception e) {

             e.printStackTrace();
         }
	 }
	 
	     /**
	     * 在多线程环境同步初始化
	     */
	    private static synchronized void poolInit() {
	        if (jedisPool == null) {  
	            initialPool();
	        }
	    }
	public JedisPool getPool() {
		return jedisPool;
	}

	/**
	 * 从jedis连接池中获取获取jedis对象
	 * 
	 * @return
	 */
	public synchronized static Jedis getJedis() {
	    if (jedisPool == null) {  
            poolInit();
        }
        Jedis jedis = null;
        int timeoutCount = 0;
        while (true) // 如果是网络超时则多试几次
        {
            try {
                jedis = jedisPool.getResource();
                return jedis;
            } catch (Exception e) {
                //异常时,销毁对象
                jedisPool.returnBrokenResource(jedis);
                // 底层原因是SocketTimeoutException，
                //不过redis已经捕捉且抛出JedisConnectionException，不继承于前者
                if (e instanceof JedisConnectionException || e instanceof SocketTimeoutException) {
                    timeoutCount++;
                    log.warn("getJedis timeoutCount={}", timeoutCount);
                    if (timeoutCount > 3) {
                        break;
                    }
                } else {
                    log.error("getJedis error", e);
                    break;
                }
                throw e;
            }
        }
        return null;
	}

	/**
	 * 获取JedisUtil实例
	 * 
	 * @return
	 */
	public static JedisUtil getInstance() {
		return jedisUtil;
	}

	/**
	 * 回收jedis
	 * 
	 * @param jedis
	 */
	public static void returnJedis(Jedis jedis) {
		jedisPool.returnResource(jedis);
	}

	/**
	 * 设置过期时间
	 * 
	 * @author V 2013-4-11
	 * @param key
	 * @param seconds
	 */
	public void expire(String key, int seconds) {
		if (seconds <= 0) {
			return;
		}
		Jedis jedis = null;
		try {
			jedis = getJedis();
			jedis.expire(key, seconds);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			returnJedis(jedis);
		}
	}

	/**
	 * 设置默认过期时间
	 * 
	 * @author V 2013-4-11
	 * @param key
	 */
	public void expire(String key) {
		expire(key, expire);
	}

	// *******************************************Keys*******************************************//
	public static class Keys {

		/**
		 * 清空所有key
		 */
		public static String flushAll() {
			Jedis jedis = null;
			String stata="";
			try {
				jedis = getJedis();
				stata = jedis.flushAll();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				returnJedis(jedis);
			}
			return stata;
		}

		/**
		 * 更改key
		 * 
		 * @param String
		 *            oldkey
		 * @param String
		 *            newkey
		 * @return 状态码
		 * */
		public static String rename(String oldkey, String newkey) {
			return rename(SafeEncoder.encode(oldkey), SafeEncoder.encode(newkey));
		}

		/**
		 * 更改key,仅当新key不存在时才执行
		 * 
		 * @param String
		 *            oldkey
		 * @param String
		 *            newkey
		 * @return 状态码
		 * */
		public static long renamenx(String oldkey, String newkey) {
			Jedis jedis = null;
			long status=0l;
			try {
				jedis = getJedis();
				status = jedis.renamenx(oldkey, newkey);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				returnJedis(jedis);
			}
			return status;
		}

		/**
		 * 更改key
		 * 
		 * @param String
		 *            oldkey
		 * @param String
		 *            newkey
		 * @return 状态码
		 * */
		public static String rename(byte[] oldkey, byte[] newkey) {
			Jedis jedis = null;
			String status ="";
			try {
				jedis = getJedis();
			    status = jedis.rename(oldkey, newkey);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				returnJedis(jedis);
			}
			return status;
		}

		/**
		 * 设置key的过期时间，以秒为单位
		 * 
		 * @param String
		 *            key
		 * @param 时间
		 *            ,已秒为单位
		 * @return 影响的记录数
		 * */
		public static long expired(String key, int seconds) {
			Jedis jedis = null;
			long count=0;
			try {
				jedis = getJedis();
				count = jedis.expire(key, seconds);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				returnJedis(jedis);
			}
			return count;
		}

		/**
		 * 设置key的过期时间,它是距历元（即格林威治标准时间 1970 年 1 月 1 日的 00:00:00，格里高利历）的偏移量。
		 * 
		 * @param String
		 *            key
		 * @param 时间
		 *            ,已秒为单位
		 * @return 影响的记录数
		 * */
		public static long expireAt(String key, long timestamp) {
			Jedis jedis = null;
			long count=0;
			try {
				jedis = getJedis();
			    count = jedis.expireAt(key, timestamp);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				returnJedis(jedis);
			}
			return count;
		}

		/**
		 * 查询key的过期时间
		 * 
		 * @param String
		 *            key
		 * @return 以秒为单位的时间表示
		 * */
		public static long ttl(String key) {
			Jedis jedis = null;
			long len=0;
			try {
				jedis = getJedis();
			    len = jedis.ttl(key);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				returnJedis(jedis);
			}
			return len;
		}

		/**
		 * 取消对key过期时间的设置
		 * 
		 * @param key
		 * @return 影响的记录数
		 * */
		public static long persist(String key) {
			Jedis jedis = null;
			long count=0;
			try {
				jedis = getJedis();
				count = jedis.persist(key);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				returnJedis(jedis);
			}
			return count;
		}

		/**
		 * 删除keys对应的记录,可以是多个key
		 * 
		 * @param String
		 *            ... keys
		 * @return 删除的记录数
		 * */
		public static long del(String... keys) {
			Jedis jedis = null;
			long count=0;
			try {
				jedis = getJedis();
			    count = jedis.del(keys);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				returnJedis(jedis);
			}
			return count;
		}

		/**
		 * 删除keys对应的记录,可以是多个key
		 * 
		 * @param String
		 *            .. keys
		 * @return 删除的记录数
		 * */
		public static long del(byte[]... keys) {
			Jedis jedis = null;
			long count=0;
			try {
				jedis = getJedis();
				count = jedis.del(keys);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				returnJedis(jedis);
			}
			return count;
		}

		/**
		 * 判断key是否存在
		 * 
		 * @param String
		 *            key
		 * @return boolean
		 * */
		public static boolean exists(String key) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis jedis = null;
			boolean exis=false;
			try {
				jedis = getJedis();
				exis = jedis.exists(key);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				returnJedis(jedis);
			}
			return exis;
		}

		/**
		 * 对List,Set,SortSet进行排序,如果集合数据较大应避免使用这个方法
		 * 
		 * @param String
		 *            key
		 * @return List<String> 集合的全部记录
		 * **/
		public static List<String> sort(String key) {
			Jedis jedis = null;
			List<String> list=null;
			try {
				jedis = getJedis();
				list = jedis.sort(key);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				returnJedis(jedis);
			}
			return list;
		}

		/**
		 * 对List,Set,SortSet进行排序或limit
		 * 
		 * @param String
		 *            key
		 * @param SortingParams
		 *            parame 定义排序类型或limit的起止位置.
		 * @return List<String> 全部或部分记录
		 * **/
		public static List<String> sort(String key, SortingParams parame) {
			Jedis jedis = null;
			List<String> list=null;
			try {
				jedis = getJedis();
				list = jedis.sort(key, parame);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				returnJedis(jedis);
			}
			return list;
		}

		/**
		 * 返回指定key存储的类型
		 * 
		 * @param String
		 *            key
		 * @return String string|list|set|zset|hash
		 * **/
		public static String type(String key) {
			Jedis jedis = null;
			String type="";
			try {
				jedis = getJedis();
				type = jedis.type(key);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				returnJedis(jedis);
			}
			return type;
		}

		/**
		 * 查找所有匹配给定的模式的键
		 * 
		 * @param String
		 *            key的表达式,*表示多个，？表示一个
		 * */
		public static Set<String> keys(String pattern) {
			Jedis jedis = null;
			Set<String> set=null;
			try {
				jedis = getJedis();
				set = jedis.keys(pattern);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				returnJedis(jedis);
			}
			return set;
		}
	}

	// *******************************************Sets*******************************************//
	public static class Sets {

		/**
		 * 向Set添加一条记录，如果member已存在返回0,否则返回1
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            member
		 * @return 操作码,0或1
		 * */
		public static long sadd(String key, String member) {
			Jedis jedis = getJedis();
			long s = 0 ;
			try{
				s = jedis.sadd(key, member);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

		public static long sadd(byte[] key, byte[] member) {
			Jedis jedis = getJedis();
			long s = 0 ;
			try{
				s = jedis.sadd(key, member);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

		/**
		 * 获取给定key中元素个数
		 * 
		 * @param String
		 *            key
		 * @return 元素个数
		 * */
		public static long scard(String key) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			long len = 0;
			try{
				len = sjedis.scard(key);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return len;
		}

		/**
		 * 返回从第一组和所有的给定集合之间的差异的成员
		 * 
		 * @param String
		 *            ... keys
		 * @return 差异的成员集合
		 * */
		public static Set<String> sdiff(String... keys) {
			Jedis jedis = getJedis();
			Set<String> set = null;
			try{
				set = jedis.sdiff(keys);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return set;
		}

		/**
		 * 这个命令等于sdiff,但返回的不是结果集,而是将结果集存储在新的集合中，如果目标已存在，则覆盖。
		 * 
		 * @param String
		 *            newkey 新结果集的key
		 * @param String
		 *            ... keys 比较的集合
		 * @return 新集合中的记录数
		 * **/
		public static long sdiffstore(String newkey, String... keys) {
			Jedis jedis = getJedis();
			long s = 0 ;
			try{
				s = jedis.sdiffstore(newkey, keys);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

		/**
		 * 返回给定集合交集的成员,如果其中一个集合为不存在或为空，则返回空Set
		 * 
		 * @param String
		 *            ... keys
		 * @return 交集成员的集合
		 * **/
		public static Set<String> sinter(String... keys) {
			Jedis jedis = getJedis();
			Set<String> set = null ;
			try{
				set = jedis.sinter(keys);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return set;
		}

		/**
		 * 这个命令等于sinter,但返回的不是结果集,而是将结果集存储在新的集合中，如果目标已存在，则覆盖。
		 * 
		 * @param String
		 *            newkey 新结果集的key
		 * @param String
		 *            ... keys 比较的集合
		 * @return 新集合中的记录数
		 * **/
		public static long sinterstore(String newkey, String... keys) {
			Jedis jedis = getJedis();
			long s = 0;
			try{
				s = jedis.sinterstore(newkey, keys);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

		/**
		 * 确定一个给定的值是否存在
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            member 要判断的值
		 * @return 存在返回1，不存在返回0
		 * **/
		public static boolean sismember(String key, String member) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			boolean s = false;
			try{
				s = sjedis.sismember(key, member);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return s;
		}

		/**
		 * 返回集合中的所有成员
		 * 
		 * @param String
		 *            key
		 * @return 成员集合
		 * */
		public static Set<String> smembers(String key) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			Set<String> set = null;
			try{
				set = sjedis.smembers(key);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return set;
		}

		public static Set<byte[]> smembers(byte[] key) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			Set<byte[]> set = null;
			try{
				set = sjedis.smembers(key);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return set;
		}

		/**
		 * 将成员从源集合移出放入目标集合 <br/>
		 * 如果源集合不存在或不包哈指定成员，不进行任何操作，返回0<br/>
		 * 否则该成员从源集合上删除，并添加到目标集合，如果目标集合中成员已存在，则只在源集合进行删除
		 * 
		 * @param String
		 *            srckey 源集合
		 * @param String
		 *            dstkey 目标集合
		 * @param String
		 *            member 源集合中的成员
		 * @return 状态码，1成功，0失败
		 * */
		public static long smove(String srckey, String dstkey, String member) {
			Jedis jedis = getJedis();
			long s = 0;
			try{
				s = jedis.smove(srckey, dstkey, member);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

		/**
		 * 从集合中删除成员
		 * 
		 * @param String
		 *            key
		 * @return 被删除的成员
		 * */
		public static String spop(String key) {
			Jedis jedis = getJedis();
			String s = null;
			try{
				s = jedis.spop(key);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

		/**
		 * 从集合中删除指定成员
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            member 要删除的成员
		 * @return 状态码，成功返回1，成员不存在返回0
		 * */
		public static long srem(String key, String member) {
			Jedis jedis = getJedis();
			long s = 0;
			try{
				s = jedis.srem(key, member);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

		/**
		 * 合并多个集合并返回合并后的结果，合并后的结果集合并不保存<br/>
		 * 
		 * @param String
		 *            ... keys
		 * @return 合并后的结果集合
		 * @see sunionstore
		 * */
		public static Set<String> sunion(String... keys) {
			Jedis jedis = getJedis();
			Set<String> set = null;
			try{
				set = jedis.sunion(keys);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return set;
		}

		/**
		 * 合并多个集合并将合并后的结果集保存在指定的新集合中，如果新集合已经存在则覆盖
		 * 
		 * @param String
		 *            newkey 新集合的key
		 * @param String
		 *            ... keys 要合并的集合
		 * **/
		public static long sunionstore(String newkey, String... keys) {
			Jedis jedis = getJedis();
			long s = 0;
			try{
				s = jedis.sunionstore(newkey, keys);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}
	}

	// *******************************************SortSet*******************************************//
	public static class SortSet {

		/**
		 * 向集合中增加一条记录,如果这个值已存在，这个值对应的权重将被置为新的权重
		 * 
		 * @param String
		 *            key
		 * @param double score 权重
		 * @param String
		 *            member 要加入的值，
		 * @return 状态码 1成功，0已存在member的值
		 * */
		public static long zadd(String key, double score, String member) {
			Jedis jedis = getJedis();
			long s = 0;
			try{
				s = jedis.zadd(key, score, member);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

		public static long zadd(String key, Map<String, Double> scoreMembers) {
			Jedis jedis = getJedis();
			long s = 0;
			try{
				s = jedis.zadd(key, scoreMembers);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

		/**
		 * 获取集合中元素的数量
		 * 
		 * @param String
		 *            key
		 * @return 如果返回0则集合不存在
		 * */
		public static long zcard(String key) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			long len = 0;
			try{
				len = sjedis.zcard(key);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return len;
		}

		/**
		 * 获取指定权重区间内集合的数量
		 * 
		 * @param String
		 *            key
		 * @param double min 最小排序位置
		 * @param double max 最大排序位置
		 * */
		public static long zcount(String key, double min, double max) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			long len = 0;
			try{
				len = sjedis.zcount(key, min, max);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return len;
		}

		/**
		 * 获得set的长度
		 * 
		 * @param key
		 * @return
		 */
		public static long zlength(String key) {
			long len = 0;
			Set<String> set = zrange(key, 0, -1);
			len = set.size();
			return len;
		}

		/**
		 * 权重增加给定值，如果给定的member已存在
		 * 
		 * @param String
		 *            key
		 * @param double score 要增的权重
		 * @param String
		 *            member 要插入的值
		 * @return 增后的权重
		 * */
		public static double zincrby(String key, double score, String member) {
			Jedis jedis = getJedis();
			double s = 0;
			try{
				s = jedis.zincrby(key, score, member);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

		/**
		 * 返回指定位置的集合元素,0为第一个元素，-1为最后一个元素
		 * 
		 * @param String
		 *            key
		 * @param int start 开始位置(包含)
		 * @param int end 结束位置(包含)
		 * @return Set<String>
		 * */
		public static Set<String> zrange(String key, int start, int end) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			Set<String> set = null;
			try{
				set = sjedis.zrange(key, start, end);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return set;
		}

		/**
		 * 返回指定权重区间的元素集合
		 * 
		 * @param String
		 *            key
		 * @param double min 上限权重
		 * @param double max 下限权重
		 * @return Set<String>
		 * */
		public static Set<String> zrangeByScore(String key, double min, double max) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			Set<String> set = null;
			try{
				set = sjedis.zrangeByScore(key, min, max);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return set;
		}

		/**
		 * 获取指定值在集合中的位置，集合排序从低到高
		 * 
		 * @see zrevrank
		 * @param String
		 *            key
		 * @param String
		 *            member
		 * @return long 位置
		 * */
		public static long zrank(String key, String member) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			long index = 0;
			try{
				index = sjedis.zrank(key, member);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return index;
		}

		/**
		 * 获取指定值在集合中的位置，集合排序从高到低
		 * 
		 * @see zrank
		 * @param String
		 *            key
		 * @param String
		 *            member
		 * @return long 位置
		 * */
		public static long zrevrank(String key, String member) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			long index = 0;
			try{
				index = sjedis.zrevrank(key, member);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return index;
		}

		/**
		 * 从集合中删除成员
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            member
		 * @return 返回1成功
		 * */
		public static long zrem(String key, String member) {
			Jedis jedis = getJedis();
			long s = 0;
			try{
				s = jedis.zrem(key, member);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

		/**
		 * 删除
		 * 
		 * @param key
		 * @return
		 */
		public static long zrem(String key) {
			Jedis jedis = getJedis();
			long s = 0;
			try{
				s = jedis.del(key);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

		/**
		 * 删除给定位置区间的元素
		 * 
		 * @param String
		 *            key
		 * @param int start 开始区间，从0开始(包含)
		 * @param int end 结束区间,-1为最后一个元素(包含)
		 * @return 删除的数量
		 * */
		public static long zremrangeByRank(String key, int start, int end) {
			Jedis jedis = getJedis();
			long s = 0;
			try{
				s = jedis.zremrangeByRank(key, start, end);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

		/**
		 * 删除给定权重区间的元素
		 * 
		 * @param String
		 *            key
		 * @param double min 下限权重(包含)
		 * @param double max 上限权重(包含)
		 * @return 删除的数量
		 * */
		public static long zremrangeByScore(String key, double min, double max) {
			Jedis jedis = getJedis();
			long s = 0;
			try{
				s = jedis.zremrangeByScore(key, min, max);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

		/**
		 * 获取给定区间的元素，原始按照权重由高到低排序
		 * 
		 * @param String
		 *            key
		 * @param int start
		 * @param int end
		 * @return Set<String>
		 * */
		public static Set<String> zrevrange(String key, int start, int end) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			Set<String> set = null;
			try{
				set = sjedis.zrevrange(key, start, end);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return set;
		}

		/**
		 * 获取给定值在集合中的权重
		 * 
		 * @param String
		 *            key
		 * @param memeber
		 * @return double 权重
		 * */
		public static double zscore(String key, String memebr) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			Double score = 0.0;
			try{
				score = sjedis.zscore(key, memebr);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			if (score != null)
				return score;
			return 0;
		}
		
		  public static Set<Tuple> zrevrangeWithScores(final String key,final long start, final long end) {
		      Jedis sjedis = getJedis();
		      Set<Tuple> result =null;
		       try {
                   result = sjedis.zrevrangeWithScores(key, start, end);
            } catch (Exception e) {
                e.printStackTrace();
            }finally{
                returnJedis(sjedis);
            }
		       return result;
		  }
	}

	// *******************************************Hash*******************************************//
	public static class Hash {

		/**
		 * 从hash中删除指定的存储
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            fieid 存储的名字
		 * @return 状态码，1成功，0失败
		 * */
		public static long hdel(String key, String fieid) {
			Jedis jedis = getJedis();
			long s = 0;
			try{
				s = jedis.hdel(key, fieid);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

		public static long hdel(String key) {
			Jedis jedis = getJedis();
			long s = 0;
			try{
				s = jedis.del(key);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

		/**
		 * 测试hash中指定的存储是否存在
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            fieid 存储的名字
		 * @return 1存在，0不存在
		 * */
		public static boolean hexists(String key, String fieid) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			boolean s = false;
			try{
				s = sjedis.hexists(key, fieid);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return s;
		}

		/**
		 * 返回hash中指定存储位置的值
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            fieid 存储的名字
		 * @return 存储对应的值
		 * */
		public static String hget(String key, String fieid) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			String s = null;
			try{
				s = sjedis.hget(key, fieid);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return s;
		}

		public static byte[] hget(byte[] key, byte[] fieid) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			byte[] s = null;
			try{
				s = sjedis.hget(key, fieid);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return s;
		}

		/**
		 * 以Map的形式返回hash中的存储和值
		 * 
		 * @param String
		 *            key
		 * @return Map<Strinig,String>
		 * */
		public static Map<String, String> hgetAll(String key) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			Map<String, String> map = null;
			try{
				map = sjedis.hgetAll(key);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return map;
		}

		/**
		 * 添加一个对应关系
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            fieid
		 * @param String
		 *            value
		 * @return 状态码 1成功，0失败，fieid已存在将更新，也返回0
		 * **/
		public static long hset(String key, String fieid, String value) {
			Jedis jedis = getJedis();
			long s = 0;
			try{
				s = jedis.hset(key, fieid, value);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

		public static long hset(String key, String fieid, byte[] value) {
			Jedis jedis = getJedis();
			long s = 0;
			try{
				s = jedis.hset(key.getBytes(), fieid.getBytes(), value);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

		/**
		 * 添加对应关系，只有在fieid不存在时才执行
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            fieid
		 * @param String
		 *            value
		 * @return 状态码 1成功，0失败fieid已存
		 * **/
		public static long hsetnx(String key, String fieid, String value) {
			Jedis jedis = getJedis();
			long s = 0;
			try{
				s = jedis.hsetnx(key, fieid, value);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

		/**
		 * 获取hash中value的集合
		 * 
		 * @param String
		 *            key
		 * @return List<String>
		 * */
		public static List<String> hvals(String key) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			List<String> list = null;
			try{
				list = sjedis.hvals(key);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return list;
		}

		/**
		 * 在指定的存储位置加上指定的数字，存储位置的值必须可转为数字类型
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            fieid 存储位置
		 * @param String
		 *            long value 要增加的值,可以是负数
		 * @return 增加指定数字后，存储位置的值
		 * */
		public static long hincrby(String key, String fieid, long value) {
			Jedis jedis = getJedis();
			long s = 0;
			try{
				s = jedis.hincrBy(key, fieid, value);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

		/**
		 * 返回指定hash中的所有存储名字,类似Map中的keySet方法
		 * 
		 * @param String
		 *            key
		 * @return Set<String> 存储名称的集合
		 * */
		public static Set<String> hkeys(String key) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			Set<String> set = null;
			try{
				set = sjedis.hkeys(key);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return set;
		}

		/**
		 * 获取hash中存储的个数，类似Map中size方法
		 * 
		 * @param String
		 *            key
		 * @return long 存储的个数
		 * */
		public static long hlen(String key) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			long len = 0;
			try{
				len = sjedis.hlen(key);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return len;
		}

		/**
		 * 根据多个key，获取对应的value，返回List,如果指定的key不存在,List对应位置为null
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            ... fieids 存储位置
		 * @return List<String>
		 * */
		public static List<String> hmget(String key, String... fieids) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			List<String> list = null;
			try{
				list = sjedis.hmget(key, fieids);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return list;
		}

		public static List<byte[]> hmget(byte[] key, byte[]... fieids) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			List<byte[]> list = null;
			try{
				list = sjedis.hmget(key, fieids);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return list;
		}

		/**
		 * 添加对应关系，如果对应关系已存在，则覆盖
		 * 
		 * @param Strin
		 *            key
		 * @param Map
		 *            <String,String> 对应关系
		 * @return 状态，成功返回OK
		 * */
		public static String hmset(String key, Map<String, String> map) {
			Jedis jedis = getJedis();
			String s = null;
			try{
				s = jedis.hmset(key, map);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

		/**
		 * 添加对应关系，如果对应关系已存在，则覆盖
		 * 
		 * @param Strin
		 *            key
		 * @param Map
		 *            <String,String> 对应关系
		 * @return 状态，成功返回OK
		 * */
		public static String hmset(byte[] key, Map<byte[], byte[]> map) {
			Jedis jedis = getJedis();
			String s = null;
			try{
				s = jedis.hmset(key, map);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return s;
		}

	}

	// *******************************************Strings*******************************************//
	public static class Strings {
		/**
		 * 根据key获取记录
		 * 
		 * @param String
		 *            key
		 * @return 值
		 * */
		public static String get(String key) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			String value = null;
			try{
				value = sjedis.get(key);
			}catch(Exception e){
				e.printStackTrace();
				throw e;
			}finally{
				returnJedis(sjedis);
			}
			return value;
		}

		/**
		 * 根据key获取记录
		 * 
		 * @param byte[] key
		 * @return 值
		 * */
		public static byte[] get(byte[] key) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			byte[] value = null;
			try{
				value = sjedis.get(key);
			}catch(Exception e){
				e.printStackTrace();
				throw e;
			}finally{
				returnJedis(sjedis);
			}
			return value;
		}

		/**
		 * 添加有过期时间的记录
		 * 
		 * @param String
		 *            key
		 * @param int seconds 过期时间，以秒为单位
		 * @param String
		 *            value
		 * @return String 操作状态
		 * */
		public static String setEx(String key, int seconds, String value) {
			Jedis jedis = getJedis();
			String str = null;
			try{
				str = jedis.setex(key, seconds, value);
			}catch(Exception e){
				e.printStackTrace();
				throw e;
			}finally{
				returnJedis(jedis);
			}
			return str;
		}

		/**
		 * 添加有过期时间的记录
		 * 
		 * @param String
		 *            key
		 * @param int seconds 过期时间，以秒为单位
		 * @param String
		 *            value
		 * @return String 操作状态
		 * */
		public static String setEx(byte[] key, int seconds, byte[] value) {
			Jedis jedis = getJedis();
			String str = null;
			try{
				str = jedis.setex(key, seconds, value);
			}catch(Exception e){
				e.printStackTrace();
				throw e;
			}finally{
				returnJedis(jedis);
			}
			return str;
		}

		/**
		 * 添加一条记录，仅当给定的key不存在时才插入
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            value
		 * @return long 状态码，1插入成功且key不存在，0未插入，key存在
		 * */
		public static long setnx(String key, String value) {
			Jedis jedis = getJedis();
			long str = 0;
			try{
				str = jedis.setnx(key, value);
			}catch(Exception e){
				e.printStackTrace();
				throw e;
			}finally{
				returnJedis(jedis);
			}
			return str;
		}

		/**
		 * 添加记录,如果记录已存在将覆盖原有的value
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            value
		 * @return 状态码
		 * */
		public static String set(String key, String value) {
			return set(SafeEncoder.encode(key), SafeEncoder.encode(value));
		}

		/**
		 * 添加记录,如果记录已存在将覆盖原有的value
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            value
		 * @return 状态码
		 * */
		public static String set(String key, byte[] value) {
			return set(SafeEncoder.encode(key), value);
		}

		/**
		 * 添加记录,如果记录已存在将覆盖原有的value
		 * 
		 * @param byte[] key
		 * @param byte[] value
		 * @return 状态码
		 * */
		public static String set(byte[] key, byte[] value) {
			Jedis jedis = getJedis();
			String status = null;
			try{
				status = jedis.set(key, value);
			}catch(Exception e){
				e.printStackTrace();
				throw e;
			}finally{
				returnJedis(jedis);
			}
			return status;
		}

		/**
		 * 从指定位置开始插入数据，插入的数据会覆盖指定位置以后的数据<br/>
		 * 例:String str1="123456789";<br/>
		 * 对str1操作后setRange(key,4,0000)，str1="123400009";
		 * 
		 * @param String
		 *            key
		 * @param long offset
		 * @param String
		 *            value
		 * @return long value的长度
		 * */
		public static long setRange(String key, long offset, String value) {
			Jedis jedis = getJedis();
			long len = 0;
			try{
				len = jedis.setrange(key, offset, value);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return len;
		}

		/**
		 * 在指定的key中追加value
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            value
		 * @return long 追加后value的长度
		 * **/
		public static long append(String key, String value) {
			Jedis jedis = getJedis();
			long len = 0;
			try{
				len = jedis.append(key, value);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return len;
		}

		/**
		 * 将key对应的value减去指定的值，只有value可以转为数字时该方法才可用
		 * 
		 * @param String
		 *            key
		 * @param long number 要减去的值
		 * @return long 减指定值后的值
		 * */
		public static long decrBy(String key, long number) {
			Jedis jedis = getJedis();
			long len = 0;
			try{
				len = jedis.decrBy(key, number);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return len;
		}

		/**
		 * <b>可以作为获取唯一id的方法</b><br/>
		 * 将key对应的value加上指定的值，只有value可以转为数字时该方法才可用
		 * 
		 * @param String
		 *            key
		 * @param long number 要减去的值
		 * @return long 相加后的值
		 * */
		public static long incrBy(String key, long number) {
			Jedis jedis = getJedis();
			long len = 0;
			try{
				len = jedis.incrBy(key, number);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return len;
		}

		/**
		 * 对指定key对应的value进行截取
		 * 
		 * @param String
		 *            key
		 * @param long startOffset 开始位置(包含)
		 * @param long endOffset 结束位置(包含)
		 * @return String 截取的值
		 * */
		public static String getrange(String key, long startOffset, long endOffset) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			String value = null;
			try{
				value = sjedis.getrange(key, startOffset, endOffset);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return value;
		}

		/**
		 * 获取并设置指定key对应的value<br/>
		 * 如果key存在返回之前的value,否则返回null
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            value
		 * @return String 原始value或null
		 * */
		public static String getSet(String key, String value) {
			Jedis jedis = getJedis();
			String str = null;
			try{
				str = jedis.getSet(key, value);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return str;
		}

		/**
		 * 批量获取记录,如果指定的key不存在返回List的对应位置将是null
		 * 
		 * @param String
		 *            keys
		 * @return List<String> 值得集合
		 * */
		public static List<String> mget(String... keys) {
			Jedis jedis = getJedis();
			List<String> str = null;
			try{
				str = jedis.mget(keys);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return str;
		}

		/**
		 * 批量存储记录
		 * 
		 * @param String
		 *            keysvalues 例:keysvalues="key1","value1","key2","value2";
		 * @return String 状态码
		 * */
		public static String mset(String... keysvalues) {
			Jedis jedis = getJedis();
			String str = null;
			try{
				str = jedis.mset(keysvalues);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return str;
		}

		/**
		 * 获取key对应的值的长度
		 * 
		 * @param String
		 *            key
		 * @return value值得长度
		 * */
		public static long strlen(String key) {
			Jedis jedis = getJedis();
			long len = 0;
			try{
				len = jedis.strlen(key);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return len;
		}
	}

	// *******************************************Lists*******************************************//
	public static class Lists {
		/**
		 * List长度
		 * 
		 * @param String
		 *            key
		 * @return 长度
		 * */
		public static long llen(String key) {
			return llen(SafeEncoder.encode(key));
		}

		/**
		 * List长度
		 * 
		 * @param byte[] key
		 * @return 长度
		 * */
		public static long llen(byte[] key) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			long count = 0;
			try{
				count = sjedis.llen(key);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return count;
		}

		/**
		 * 覆盖操作,将覆盖List中指定位置的值
		 * 
		 * @param byte[] key
		 * @param int index 位置
		 * @param byte[] value 值
		 * @return 状态码
		 * */
		public static String lset(byte[] key, int index, byte[] value) {
			Jedis jedis = getJedis();
			String status = null;
			try{
				status = jedis.lset(key, index, value);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return status;
		}

		/**
		 * 覆盖操作,将覆盖List中指定位置的值
		 * 
		 * @param key
		 * @param int index 位置
		 * @param String
		 *            value 值
		 * @return 状态码
		 * */
		public static String lset(String key, int index, String value) {
			return lset(SafeEncoder.encode(key), index, SafeEncoder.encode(value));
		}

		/**
		 * 在value的相对位置插入记录
		 * 
		 * @param key
		 * @param LIST_POSITION
		 *            前面插入或后面插入
		 * @param String
		 *            pivot 相对位置的内容
		 * @param String
		 *            value 插入的内容
		 * @return 记录总数
		 * */
		public static long linsert(String key, LIST_POSITION where, String pivot, String value) {
			return linsert(SafeEncoder.encode(key), where, SafeEncoder.encode(pivot), SafeEncoder.encode(value));
		}

		/**
		 * 在指定位置插入记录
		 * 
		 * @param String
		 *            key
		 * @param LIST_POSITION
		 *            前面插入或后面插入
		 * @param byte[] pivot 相对位置的内容
		 * @param byte[] value 插入的内容
		 * @return 记录总数
		 * */
		public static long linsert(byte[] key, LIST_POSITION where, byte[] pivot, byte[] value) {
			Jedis jedis = getJedis();
			long count = 0;
			try{
				count = jedis.linsert(key, where, pivot, value);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return count;
		}

		/**
		 * 获取List中指定位置的值
		 * 
		 * @param String
		 *            key
		 * @param int index 位置
		 * @return 值
		 * **/
		public static String lindex(String key, int index) {
			return SafeEncoder.encode(lindex(SafeEncoder.encode(key), index));
		}

		/**
		 * 获取List中指定位置的值
		 * 
		 * @param byte[] key
		 * @param int index 位置
		 * @return 值
		 * **/
		public static byte[] lindex(byte[] key, int index) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			byte[] value = null;
			try{
				value = sjedis.lindex(key, index);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return value;
		}

		/**
		 * 将List中的第一条记录移出List
		 * 
		 * @param String
		 *            key
		 * @return 移出的记录
		 * */
		public static String lpop(String key) {
			return SafeEncoder.encode(lpop(SafeEncoder.encode(key)));
		}

		/**
		 * 将List中的第一条记录移出List
		 * 
		 * @param byte[] key
		 * @return 移出的记录
		 * */
		public static byte[] lpop(byte[] key) {
			Jedis jedis = getJedis();
			byte[] value =null;
			try{
				value = jedis.lpop(key);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return value;
		}

		/**
		 * 将List中最后第一条记录移出List
		 * 
		 * @param byte[] key
		 * @return 移出的记录
		 * */
		public static String rpop(String key) {
			Jedis jedis = getJedis();
			String value = null;
			try{
				value = jedis.rpop(key);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return value;
		}

		/**
		 * 向List尾部追加记录
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            value
		 * @return 记录总数
		 * */
		public static long lpush(String key,String value) {
			return lpush(SafeEncoder.encode(key), SafeEncoder.encode(value));
		}
		
		/**
		 * 向List尾部追加记录
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            value
		 * @return 记录总数
		 * */
		public static long newlpush(String key,String[] strs) {
			Jedis jedis = getJedis();
			long count = 0;
			try{
				count = jedis.lpush(key, strs);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return count;
		}

		/**
		 * 向List头部追加记录
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            value
		 * @return 记录总数
		 * */
		public static long rpush(String key, String value) {
			Jedis jedis = getJedis();
			long count = 0;
			try{
				count = jedis.rpush(key, value);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return count;
		}

		/**
		 * 向List头部追加记录
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            value
		 * @return 记录总数
		 * */
		public static long rpush(byte[] key, byte[] value) {
			Jedis jedis = getJedis();
			long count = 0;
			try{
				count = jedis.rpush(key, value);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return count;
		}

		/**
		 * 向List中追加记录
		 * 
		 * @param byte[] key
		 * @param byte[] value
		 * @return 记录总数
		 * */
		public static long lpush(byte[] key, byte[] value) {
			Jedis jedis = getJedis();
			long count = 0;
			try{
				count = jedis.lpush(key, value);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return count;
		}

		/**
		 * 获取指定范围的记录，可以做为分页使用
		 * 
		 * @param String
		 *            key
		 * @param long start
		 * @param long end
		 * @return List
		 * */
		public static List<String> lrange(String key, long start, long end) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			List<String> list = null;
			try{
				list = sjedis.lrange(key, start, end);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return list;
		}

		/**
		 * 获取指定范围的记录，可以做为分页使用
		 * 
		 * @param byte[] key
		 * @param int start
		 * @param int end 如果为负数，则尾部开始计算
		 * @return List
		 * */
		public static List<byte[]> lrange(byte[] key, int start, int end) {
			// ShardedJedis sjedis = getShardedJedis();
			Jedis sjedis = getJedis();
			List<byte[]> list = null;
			try{
				list = sjedis.lrange(key, start, end);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(sjedis);
			}
			return list;
		}

		/**
		 * 删除List中c条记录，被删除的记录值为value
		 * 
		 * @param byte[] key
		 * @param int c 要删除的数量，如果为负数则从List的尾部检查并删除符合的记录
		 * @param byte[] value 要匹配的值
		 * @return 删除后的List中的记录数
		 * */
		public static long lrem(byte[] key, int c, byte[] value) {
			Jedis jedis = getJedis();
			long count = 0;
			try{
				count = jedis.lrem(key, c, value);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return count;
		}

		/**
		 * 删除List中c条记录，被删除的记录值为value
		 * 
		 * @param String
		 *            key
		 * @param int c 要删除的数量，如果为负数则从List的尾部检查并删除符合的记录
		 * @param String
		 *            value 要匹配的值
		 * @return 删除后的List中的记录数
		 * */
		public static long lrem(String key, int c, String value) {
			return lrem(SafeEncoder.encode(key), c, SafeEncoder.encode(value));
		}

		/**
		 * 算是删除吧，只保留start与end之间的记录
		 * 
		 * @param byte[] key
		 * @param int start 记录的开始位置(0表示第一条记录)
		 * @param int end 记录的结束位置（如果为-1则表示最后一个，-2，-3以此类推）
		 * @return 执行状态码
		 * */
		public static String ltrim(byte[] key, int start, int end) {
			Jedis jedis = getJedis();
			String str = null;
			try{
				str = jedis.ltrim(key, start, end);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				returnJedis(jedis);
			}
			return str;
		}

		/**
		 * 算是删除吧，只保留start与end之间的记录
		 * 
		 * @param String
		 *            key
		 * @param int start 记录的开始位置(0表示第一条记录)
		 * @param int end 记录的结束位置（如果为-1则表示最后一个，-2，-3以此类推）
		 * @return 执行状态码
		 * */
		public static String ltrim(String key, int start, int end) {
			return ltrim(SafeEncoder.encode(key), start, end);
		}
	}

	public static class Transactions {
		/**
		 * 向List尾部追加记录
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            value
		 * @return 记录总数
		 * */
		public static void lpush(Transaction tr, String key, String value) {
			lpush(tr, SafeEncoder.encode(key), SafeEncoder.encode(value));
		}

		/**
		 * 向List中追加记录
		 * 
		 * @param byte[] key
		 * @param byte[] value
		 * @return 记录总数
		 * */
		public static void lpush(Transaction tr, byte[] key, byte[] value) {
			tr.lpush(key, value);
		}

		/**
		 * 添加记录,如果记录已存在将覆盖原有的value
		 * 
		 * @param String
		 *            key
		 * @param String
		 *            value
		 * @return 状态码
		 * */
		public static void set(Transaction tr, String key, String value) {
			set(tr, SafeEncoder.encode(key), SafeEncoder.encode(value));
		}

		/**
		 * 添加记录,如果记录已存在将覆盖原有的value
		 * 
		 * @param byte[] key
		 * @param byte[] value
		 * @return 状态码
		 * */
		public static void set(Transaction tr, byte[] key, byte[] value) {
			tr.set(key, value);
		}

		/**
		 * 算是删除吧，只保留start与end之间的记录
		 * 
		 * @param String
		 *            key
		 * @param int start 记录的开始位置(0表示第一条记录)
		 * @param int end 记录的结束位置（如果为-1则表示最后一个，-2，-3以此类推）
		 * @return 执行状态码
		 * */
		public static void ltrim(Transaction tr, String key, int start, int end) {
			ltrim(tr, SafeEncoder.encode(key), start, end);
		}

		/**
		 * 算是删除吧，只保留start与end之间的记录
		 * 
		 * @param byte[] key
		 * @param int start 记录的开始位置(0表示第一条记录)
		 * @param int end 记录的结束位置（如果为-1则表示最后一个，-2，-3以此类推）
		 * @return 执行状态码
		 * */
		public static void ltrim(Transaction tr, byte[] key, int start, int end) {
			tr.ltrim(key, start, end);
		}
	}

	public static class RedisLock {
		/**
		 * 获取锁 如果锁可用 立即返回true， 否则返回false
		 * 
		 * @return
		 */
		public static boolean tryLock(String key, int expireTime) {
			return tryLock(key, expireTime, 0L, null);
		}

		/**
		 * 锁在给定的等待时间内空闲，则获取锁成功 返回true， 否则返回false
		 * 
		 * @param key
		 * @param timeout
		 * @param unit
		 * @return
		 */
		public static boolean tryLock(String key, int expireTime, long timeout, TimeUnit unit) {
			Jedis jedis = null;
			try {
				jedis = JedisUtil.getJedis();
				long nano = System.nanoTime();
				do {
					long timenum = JedisUtil.Keys.ttl(key);
					if (-1 == timenum && key.equals(JedisUtil.Strings.get(key))) {
						JedisUtil.Keys.del(key);
					}
					Long i = jedis.setnx(key, key);
					if (i == 1) {
						jedis.expire(key, expireTime);
						return Boolean.TRUE;
					} else { // 存在锁
						if (log.isDebugEnabled()) {
							String desc = jedis.get(key);
							log.debug("key: " + key + " locked by another business：" + desc);
						}
					}
					if (timeout == 0) {
						break;
					}
					Thread.sleep(300);
				} while ((System.nanoTime() - nano) < unit.toNanos(timeout));
				return Boolean.FALSE;
			} catch (JedisConnectionException je) {
				log.error(je.getMessage(), je);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				returnJedis(jedis);
			}
			return Boolean.FALSE;
		}

		/**
		 * 释放锁
		 * 
		 * @param key
		 */
		public static void unLock(String key) {
			Jedis jedis = null;
			try {
				jedis = JedisUtil.getJedis();
				jedis.del(key);
				log.debug("release lock, keys :" + key);
			} catch (JedisConnectionException je) {
				log.error(je.getMessage(), je);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				returnJedis(jedis);
			}
		}
	}

}
