package com.jule.domino.log.logobjs;

import com.jule.domino.log.utils.ClazzUtil;
import com.jule.domino.log.utils.MyLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 所有的日志对象
 * 
 * @author ran
 * @since 2016/7/21
 * 
 */
public final class __AllLogObjs {
	/** 日志类列表 */
	private static List<Class<? extends AbstractLog>> _cl = null;

	/**
	 * 类默认构造器
	 * 
	 */
	private __AllLogObjs() {
	}

	/**
	 * 获取所有的日志对象
	 * 
	 * @return
	 * 
	 */
	public synchronized static List<Class<? extends AbstractLog>> getAllClazz() {
		if (_cl != null) {
			return _cl;
		}

		// 创建类列表
		_cl = new ArrayList<>();

		// 添加日志类
		_cl.addAll(scanpackage());

		return _cl;
	}

	/**
	 * 扫描指定包
	 * @return
	 */
	private static List<Class<? extends AbstractLog>> scanpackage(){
		String pack = "com.jule.domino.log.logobjs.impl";
		MyLog.OBJ.info("扫描日志对象目录-->"+pack);

		List<Class<? extends AbstractLog>> list = new ArrayList<>();

		//获取包下的所有对象
		Set<Class<?>> clazzes = ClazzUtil.getClasses(pack);
		if (clazzes == null){
			return list;
		}

		//遍历判断是否是AbstractLog子类
		for (Class<?> clazz : clazzes){
			if (clazz == null){
				continue;
			}
			MyLog.OBJ.info("装载日志类 -- > " +clazz.getName());
			list.add((Class<? extends AbstractLog>) clazz);
		}
		return list;
	}

}
