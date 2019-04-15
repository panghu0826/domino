package com.jule.domino.log.config;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * 应用程序配置项，在jdbc.properties文件中定义
 * 
 * @author ran
 */
public class AppConfig {
	
	/** 属性字典 */
	private static Properties _props = null;
	
	/** 服务器 IP */
	private static final String PROPS_JDBC_DRIVER = "DB.jdbcDriver";
	
	private static final String PROPS_JDBC_URL = "DB.connectionString";
	
	private static final String PROPS_JDBC_NAME = "DB.userName";
	
	private static final String PROPS_JDBC_PWD = "DB.passWord";
	
	private static final String PROPS_SQL_COMMIT = "DB.insertCount";
	
	private static final String PROPS_SERVER_IP = "server.IP";
	
	private static final String PROPS_SERVER_PORT = "server.port";
	
	private static final String PROPS_SQL_COMMIT_TIME = "DB.insertTime";
	
	public AppConfig() {
	}
	
	/**
	 * 加载属性字典
	 * 
	 * @param fileName 
	 * 
	 */
	public static void loadProps(String fileName) {
		try {
			//文件地址
			//String path = getUrl(fileName).getPath();
			// 创建输入流
			InputStream input = new BufferedInputStream(new FileInputStream(fileName));
			// 创建属性对象
			_props = new Properties();
			_props.load(input);
		} catch (Exception ex) {
			// 抛出运行时异常
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * 获取文件目录
	 * @param fileName
	 * @return
	 */
	public static URL getUrl(String fileName){
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		return classLoader.getResource(fileName);
	}
	
	/**
	 * 获取属性字典
	 * 
	 * @return 
	 * 
	 */
	public static Properties getProps() {
		return _props;
	}
	
	public static String getJdbcDriver() {
		return _props.getProperty(PROPS_JDBC_DRIVER);
	}
	
	public static String getJdbcUrl() {
		return _props.getProperty(PROPS_JDBC_URL);
	}
	
	public static String getJdbcName() {
		return _props.getProperty(PROPS_JDBC_NAME);
	}
	
	public static String getJdbcPwd() {
		return _props.getProperty(PROPS_JDBC_PWD);
	}
	
	public static int getSqlCommit() {
		return Integer.parseInt(_props.getProperty(PROPS_SQL_COMMIT));
	}
	
	public static String getServerIp() {
		return _props.getProperty(PROPS_SERVER_IP);
	}
	
	public static int getServerPort() {
		return Integer.parseInt(_props.getProperty(PROPS_SERVER_PORT));
	}
	
	public static int getCommitTime() {
		return Integer.parseInt(_props.getProperty(PROPS_SQL_COMMIT_TIME));
	}
}
