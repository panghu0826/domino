package com.jule.domino.log.db;

import com.jule.domino.log.utils.ClazzUtil;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * MySQL 实用工具类
 * 
 * @author hjj2019
 * @since 2013/7/20
 * 
 */
public final class MySQLUtil {
	/**
	 * 类默认构造器
	 * 
	 */
	private MySQLUtil() {
	}

	/**
	 * 构建建表语句
	 * 
	 * @param objClazz
	 * @return
	 * 
	 */
	public static String buildCreateTableSQL(Class<?> objClazz) {
		if (objClazz == null) {
			return null;
		}

		// 获取方法数组
		List<Member> ml = ClazzUtil.listMembers(objClazz, Column.class);

		if (ml == null || ml.size() <= 0) {
			return null;
		}

		// 字符串缓冲区
		StringBuffer sb = new StringBuffer();

		sb.append("create table if not exists `$splitTableName$` ( \n");

		for (Member m : ml) {
			// 获取列定义
			String columnDef = getColumnDef(m);

			if (columnDef == null || columnDef.isEmpty()) {
				// 如果列定义为空,
				// 则直接跳过
				continue;
			}

			sb.append(columnDef);
			sb.append(", \n");
		}

		sb.append("primary key ( `ID` ) \n");
		sb.append(") ENGINE=MyISAM DEFAULT CHARSET=utf8;");

		return sb.toString();
	}

	/**
	 * 构建数据列定义
	 * 
	 * @param m
	 * @return
	 * 
	 */
	private static String getColumnDef(Member m) {
		if ((m == null) || 
			(m instanceof AccessibleObject) == false) {
			return null;
		}

		// 强制转型
		AccessibleObject ao = (AccessibleObject)m;
		// 获取 @Transient 注解
		Transient $transient = ao.getAnnotation(Transient.class);
		// 获取 @Column 注解
		Column $column = ao.getAnnotation(Column.class);

		if ($transient != null || $column == null) {
			// 如果被标记为 @Transient 或者没有标记 @Column,
			// 则直接退出!
			return null;
		}

		// 获取列名称
		String name = getColumnName(m);
		String type = getColumnType(m);

		return "`" + name + "` " + type;
	}

	/**
	 * 根据方法对象获取数据列名称
	 * 
	 * @param $member
	 * @return
	 * 
	 */
	private static String getColumnName(Member $member) {
		if (($member == null) || 
			($member instanceof AccessibleObject) == false) {
			// 如果参数对象为空,
			// 则直接退出!
			return null;
		}

		// 强制转型
		AccessibleObject ao = (AccessibleObject)$member;
		// 获取字段定义
		Column $column = ao.getAnnotation(Column.class);

		if ($column == null) {
			// 如果没有 @Column 注解,
			// 则直接退出!
			return null;
		}

		// 获取列名称
		String name = $column.name();

		if (name == null || 
			name.isEmpty()) {
			// 如果列名称没有定义在 @Column 中,
			// 那么直接使用方法名称
			name = trimGet($member.getName());
		}

		return name;
	}

	/**
	 * 去除开头的 get
	 * 
	 * @param methodName
	 * @return
	 * 
	 */
	private static String trimGet(String methodName) {
		if (methodName == null || 
			methodName.isEmpty()) {
			return methodName;
		} else if (methodName.startsWith("get")) {
			methodName = methodName.substring(3);
			String startChar = String.valueOf(methodName.charAt(0));
			return startChar.toLowerCase() + methodName.substring(1);
		} else if (methodName.startsWith("is")) {
			methodName = methodName.substring(2);
			String startChar = String.valueOf(methodName.charAt(0));
			return startChar.toLowerCase() + methodName.substring(1);
		} else {
			return methodName;
		}
	}

	/**
	 * 获取数据列类型
	 * 
	 * @param $member
	 * @return
	 * 
	 */
	private static String getColumnType(Member $member) {
		if (($member == null) || 
			($member instanceof AccessibleObject) == false) {
			// 如果参数对象为空,
			// 则直接退出!
			return null;
		}

		// 强制转型
		AccessibleObject ao = (AccessibleObject)$member;
		// 获取字段定义
		Column $column = ao.getAnnotation(Column.class);

		if ($column == null) {
			// 如果没有 @Column 注解,
			// 则直接退出!
			return null;
		}

		// 获取数据列定义
		String columnDef = $column.columnDefinition();

		if (columnDef != null && 
			columnDef.isEmpty() == false) {
			// 如果数据列定义不为空,
			// 则直接返回!
			return columnDef;
		}

		// 获取返回值类型
		Class<?> defOrRetTypeClazz = ClazzUtil.getDefOrRetType($member);

		// 获取长度定义
		int len = $column.length();
		// 获取 MySQL 类型
		String mysqlType = getMySQLType(defOrRetTypeClazz);

		if (len == 255) {
			if (mysqlType.equalsIgnoreCase("varchar")) {
				return mysqlType + "( 2048 )";
			} else {
				return mysqlType;
			}
		} else {
			return mysqlType + "( " + len + " )";
		}
	}

	/**
	 * JAVA 类型转换为 MySQL 类型
	 * 
	 * @param clazzType
	 * @return
	 * @throws IllegalArgumentException
	 *             if clazz == null
	 * 
	 */
	private static String getMySQLType(Class<?> clazzType) {
		if (clazzType == null) {
			// 如果参数对象为空,
			// 则直接抛出异常!
			throw new IllegalArgumentException("null clazzType");
		}

		if (clazzType.equals(String.class)) {
			// String
			return "varchar";
		} else if (clazzType.equals(Integer.class)
				|| clazzType.equals(Integer.TYPE)) {
			// Integer
			return "int";
		} else if (clazzType.equals(Long.class) || clazzType.equals(Long.TYPE)) {
			// Long
			return "bigint";
		} else if (clazzType.equals(Float.class)
				|| clazzType.equals(Float.TYPE)) {
			// Float
			return "float";
		} else if (clazzType.equals(Double.class)
				|| clazzType.equals(Double.TYPE)) {
			// Double
			return "real";
		} else if (clazzType.equals(Short.class)
				|| clazzType.equals(Short.TYPE)) {
			// Short
			return "int";
		} else if (clazzType.equals(Boolean.class)
				|| clazzType.equals(Boolean.TYPE)) {
			// Boolean
			return "tinyint";
		} else if (clazzType.equals(Character.class)
				|| clazzType.equals(Character.TYPE)) {
			// Character
			return "varchar";
		} else {
			// Unknown
			return "unknown";
		}
	}

	/**
	 * 构建建表语句
	 * 
	 * @param objClazz
	 * @return
	 * 
	 */
	public static String buildInsertIntoSQL(Class<?> objClazz) {
		if (objClazz == null) {
			return null;
		}

		// 获取方法数组
		List<Member> ml = ClazzUtil.listMembers(objClazz, Column.class);

		if (ml == null || 
			ml.size() <= 0) {
			return null;
		}

		// 获取迭代器
		Iterator<Member> it = ml.iterator();

		// 字符串缓冲区, 列和值
		StringBuffer sb_cols = new StringBuffer();
		StringBuffer sb_vals = new StringBuffer();

		while (it.hasNext()) {
			// 获取方法对象
			Member $m = it.next();

			if (($m == null) || 
				($m instanceof AccessibleObject) == false || 
				((AccessibleObject)$m).getAnnotation(Id.class) != null) {
				// 如果方法对象为空,
				// 则直接跳过!
				continue;
			}

			// 获取列定义和方法名
			String colName = getColumnName($m);
			String methodName = trimGet($m.getName());

			// 设置列和值
			sb_cols.append("`" + colName + "`");
			sb_cols.append(it.hasNext() ? ", \n" : "\n");
			sb_vals.append("#" + methodName + "#");
			sb_vals.append(it.hasNext() ? ", \n" : "\n");
		}

		StringBuffer sb_sqls = new StringBuffer();
		sb_sqls.append("insert into `$splitTableName$` ( \n");
		sb_sqls.append(sb_cols);
		sb_sqls.append(") values ( \n");
		sb_sqls.append(sb_vals);
		sb_sqls.append(");");

		return sb_sqls.toString();
	}

	/**
	 * 构建获得log的方法
	 * 
	 * @param objClazz
	 * @return
	 * 
	 */
	public static String getLogClassString(Class<?> objClazz) {

		if (objClazz == null) {
			return null;
		}

		// 获取方法数组
		List<Member> ml = ClazzUtil.listMembers(objClazz, Column.class);

		if (ml == null || 
			ml.size() <= 0) {
			return null;
		}

		// 获取迭代器
		Iterator<Member> it = ml.iterator();

		// 字符串缓冲区, 列和值
		StringBuffer methodGet = new StringBuffer();

		methodGet.append("package com.game.logServer.logtool;\n");
		methodGet.append("\n");
		methodGet.append("import com.game.logServer.logobjs.AbstractLog;\n");
		methodGet.append("import com.game.logServer.logobjs."
				+ objClazz.getSimpleName() + ";\n");

		methodGet.append("public class " + objClazz.getSimpleName()
				+ "Tool implements LogObjectTool {\n");
		methodGet.append("\n");
		methodGet.append("	@Override\n");
		methodGet.append("	public Class<?> getOjbectType() {\n");
		methodGet.append("			return " + objClazz.getSimpleName() + ".class;\n");
		methodGet.append("	}\n");
		methodGet.append("\n");
		methodGet.append("	@Override\n");
		methodGet
				.append("	public String getObjectValues(AbstractLog object) {\n");
		methodGet.append("		" + objClazz.getSimpleName() + " log = ("
				+ objClazz.getSimpleName() + ") object;\n");
		methodGet.append("\n");
		methodGet.append(" String ret = \"(\"; \n");
		
		StringBuffer getNames = new StringBuffer();

		while (it.hasNext()) {
			// 获取方法对象
			Member $m = it.next();

			if (($m == null) || 
				($m instanceof AccessibleObject) == false || 
				((AccessibleObject)$m).getAnnotation(Id.class) != null) {
				// 如果方法对象为空,
				// 则直接跳过!
				continue;
			}

			// 获取列定义和方法名
			if ($m.getName().startsWith("get")) {
				String methodName = trimGet($m.getName());
				getNames.append("`");
				getNames.append(methodName);
				getNames.append("`,");

				// 获取返回值类型
				Class<?> defOrRetTypeClazz = ClazzUtil.getDefOrRetType($m);

				if (defOrRetTypeClazz == String.class) {
					methodGet.append("    ret +=\"'\"+ log." + $m.getName()
							+ "()+\"',\";\n");
				} else {
					methodGet.append("    ret +=\"\"+ log." + $m.getName()
							+ "()+\",\";\n");
				}
			}
			// 设置列和值
		}
		methodGet.append("   ret=ret.substring(0,ret.length()-1);\n");
		methodGet.append("   ret+=\")\";\n");
		methodGet.append("  return ret;");
		methodGet.append("  }\n");
		
		
		methodGet.append("	@Override\n");
		methodGet.append("		public String getObjectNames() {\n");
		String retname=getNames.toString();
		retname=retname.substring(0,retname.length()-1);
		methodGet.append("		String ret=\""+retname+"\";\n");
		methodGet.append("		return ret;\n");
		methodGet.append("	}\n");
		methodGet.append("}\n");
		return methodGet.toString();
	}

	/**
	 * 构建Insert语句
	 * 
	 * @param objClazz
	 * @return
	 * 
	 */
	public static String buildInsertIntoMoreSQL(Class<?> objClazz) {
		if (objClazz == null) {
			return null;
		}

		// 获取方法数组
		List<Member> ml = ClazzUtil.listMembers(objClazz, Column.class);

		if (ml == null || 
			ml.size() <= 0) {
			return null;
		}

		// 获取迭代器
		Iterator<Member> it = ml.iterator();

		// 字符串缓冲区, 列和值
		StringBuffer sb_cols = new StringBuffer();
		// StringBuffer sb_vals = new StringBuffer();

		while (it.hasNext()) {
			// 获取方法对象
			Member $m = it.next();

			if (($m == null) || 
				($m instanceof AccessibleObject) == false || 
				((AccessibleObject)$m).getAnnotation(Id.class) != null) {
				// 如果方法对象为空,
				// 则直接跳过!
				continue;
			}

			// 获取列定义和方法名
			String colName = getColumnName($m);
			// String methodName = trimGet($m.getName());

			// 设置列和值
			sb_cols.append("`" + colName + "`");
			sb_cols.append(it.hasNext() ? ", \n" : "\n");
			// sb_vals.append("#" + methodName + "#");
			// sb_vals.append(it.hasNext() ? ", \n" : "\n");
		}

		StringBuffer sb_sqls = new StringBuffer();
		sb_sqls.append("insert into `$splitTableName$` ( \n");
		sb_sqls.append("$nameValue$");
		sb_sqls.append(") values  \n");
		sb_sqls.append("$columnValue$");
		sb_sqls.append(";");

		return sb_sqls.toString();
	}
	
	
	/**
	 * 构建建表语句
	 * 
	 * @param objClazz
	 * @return
	 * 
	 */
	public static String buildInsertIntoListSQL(Class<?> objClazz) {
		if (objClazz == null) {
			return null;
		}

		// 获取方法数组
		List<Member> ml = ClazzUtil.listMembers(objClazz, Column.class);

		if (ml == null || 
			ml.size() <= 0) {
			return null;
		}

		// 获取迭代器
		Iterator<Member> it = ml.iterator();

		// 字符串缓冲区, 列和值
		StringBuffer sb_cols = new StringBuffer();
		StringBuffer sb_vals = new StringBuffer();

		while (it.hasNext()) {
			// 获取方法对象
			Member $m = it.next();

			if (($m == null) || 
				($m instanceof AccessibleObject) == false || 
				((AccessibleObject)$m).getAnnotation(Id.class) != null) {
				// 如果方法对象为空,
				// 则直接跳过!
				continue;
			}

			// 获取列定义和方法名
			String colName = getColumnName($m);
			String methodName = trimGet($m.getName());

			// 设置列和值
			sb_cols.append("`" + colName + "`");
			sb_cols.append(it.hasNext() ? ", \n" : "\n");
			sb_vals.append("'$values[]." + methodName + "$'");
			sb_vals.append(it.hasNext() ? ", \n" : "\n");
		}

		StringBuffer sb_sqls = new StringBuffer();
		sb_sqls.append(" insert into `$splitTableName$` ( \n");
		sb_sqls.append(sb_cols);
		sb_sqls.append(") values  \n<iterate property=\"values\"  conjunction=\",\">  <![CDATA[ (  \n");
		sb_sqls.append(sb_vals);
		sb_sqls.append(" )]]>   \n</iterate>");

		return sb_sqls.toString();
	}
}
