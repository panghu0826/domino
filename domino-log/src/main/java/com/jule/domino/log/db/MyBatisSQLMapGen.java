package com.jule.domino.log.db;

import com.jule.domino.log.logobjs.AbstractLog;
import com.jule.domino.log.logobjs.__AllLogObjs;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;


/**
 * MyBatis SQLMap 文件自动生成
 *  
 * @author ran
 *
 */
public final class MyBatisSQLMapGen {
	/** 单例对象 */
	public static final MyBatisSQLMapGen OBJ = new MyBatisSQLMapGen();

	/**
	 * 默认构造
	 */
	private MyBatisSQLMapGen() {
	}

	/**
	 * 生成SQLMap 文件
	 * @param fileAbsName
	 */
	public void gen(String fileAbsName){
		if (StringUtils.isEmpty(fileAbsName)) {
			//如果文件名为空
			//直接退出
			return ;
		}

		//写出SQLMap 文件
		writeSQLMapFile(fileAbsName, this.buildSQLMap());
	}

	public void genJava(String path){
		if (StringUtils.isEmpty(path)) {
			//如果文件名为空
			//直接退出
			return ;
		}

		//写出Java 文件
		writeJavaMap(path);
	} 

	private StringBuffer buildSQLMap(){
		// 获取注册类列表
		List<Class<? extends AbstractLog>> cl = __AllLogObjs.getAllClazz();

		if (cl == null || cl.isEmpty()) {
			return null;
		}

		StringBuffer xml = new StringBuffer();
		//XML开始标记
		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		xml.append("\n");
		xml.append("<!DOCTYPE sqlMap PUBLIC \"-//iBATIS.com//DTD SQL Map 2.0//EN\" \"http://www.ibatis.com/dtd/sql-map-2.dtd\">");
		xml.append("\n");
		xml.append("<sqlMap>");
		xml.append("\n");

		for (Class<? extends AbstractLog> c : cl) {
			//获取类名
			String clazzName = c.getSimpleName();

			//获取建表sql 和 数据插入sql
			String sql_create = MySQLUtil.buildCreateTableSQL(c);
			String sql_insert = MySQLUtil.buildInsertIntoSQL(c);
			String sql_insertList = MySQLUtil.buildInsertIntoListSQL(c);

			//建表sql
			xml.append("\n");
			xml.append("<statement id=\"createTable_" +clazzName+ "\" parameterClass=\"java.lang.String\">");
			xml.append("\n");
			xml.append(sql_create);
			xml.append("\n");
			xml.append("</statement>");
			xml.append("\n");
			//插入sql
			xml.append("\n");
			xml.append("<statement id=\"insertInto_" +clazzName+ "\" parameterClass=\"" + c.getName() + "\">");
			xml.append("\n");
			xml.append(sql_insert);
			xml.append("\n");
			xml.append("</statement>");
			xml.append("\n");
			//批量插入sql
			xml.append("\n");
			xml.append("<insert id=\"insertInto_" +clazzName+ "List\" parameterClass=\"java.util.HashMap\">");
			xml.append("\n");
			xml.append(sql_insertList);
			xml.append("\n");
			xml.append("</insert>");
			xml.append("\n");
		}

		// XML 结束标记
		xml.append("\n");
		xml.append("</sqlMap>");
		return xml;
	}

	/**
	 * 写出SQLMap文件
	 * @param fileAbsName
	 * @param buffer
	 */
	private void writeSQLMapFile(String fileAbsName, StringBuffer buffer){
		try {
			// 创建读写流
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileAbsName));

			// 写入文件
			writer.write(buffer.toString());
			// 刷新并关闭
			writer.flush();
			writer.close();
		} catch (Exception e) {
			// 抛出运行时异常
			throw new RuntimeException(e);
		}
	}

	private void writeJavaMap(String path){
		// 获取注册类
		List<Class<? extends AbstractLog>> cl = __AllLogObjs.getAllClazz();

		if (cl == null || cl.isEmpty()) {
			return ;
		}

		try {
			for (Class<? extends AbstractLog> c : cl) {
				//获取类名
				String clazzName = c.getSimpleName();
				String fileName = path + clazzName + "Tool.java";
				// 创建读写流
				BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
				// 写入
				writer.write(MySQLUtil.getLogClassString(c).toString());
				//刷新并关闭
				writer.flush();
				writer.close();
			}
		} catch (Exception e) {
			// 抛出运行时异常
			throw new RuntimeException(e);
		}
	}
}
