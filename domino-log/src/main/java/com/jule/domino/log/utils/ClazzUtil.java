package com.jule.domino.log.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 类工具
 * 
 * @author hjj2017
 * @since 2013/7/22
 * 
 */
@Slf4j
public final class ClazzUtil {
	/**
	 * 类默认构造器
	 * 
	 */
	private ClazzUtil() {
	}

	/**
	 * 列表类成员, 包括函数和字段
	 * 
	 * @param objClazz
	 * @return 
	 * 
	 */
	public static List<Member> listMembers(Class<?> objClazz) {
		if (objClazz == null) {
			return null;
		}

		// 类集成关系堆栈
		LinkedList<Class<?>> clazzStack = new LinkedList<>();
		// 当前类
		Class<?> currClazz;

		for (currClazz = objClazz; 
			 currClazz != null; 
			 currClazz = currClazz.getSuperclass()) {
			// 将当前类压入堆栈
			clazzStack.offerFirst(currClazz);
		}

		// 创建方法列表
		List<Member> ml = new ArrayList<>();

		while ((currClazz = clazzStack.pollFirst()) != null) {
			// 获取方法数组
			Member[] mArr = currClazz.getDeclaredMethods();

			for (Member m : mArr) {
				// 添加方法对象到列表
				ml.add(m);
			}

			// 获取字段数组
			mArr = currClazz.getDeclaredFields();

			for (Member m : mArr) {
				// 添加方法对象到列表
				ml.add(m);
			}
		}

		return ml;
	}

	/**
	 * 列表带有指定注解的 get 方法
	 * 
	 * @param objClazz
	 * @param hasAnnotation
	 * @return 
	 * 
	 */
	public static <T extends Annotation> List<Member> listMembers(Class<?> objClazz, Class<T> hasAnnotation) {
		// 列表类方法
		List<Member> ml = listMembers(objClazz);

		if (ml == null || 
			ml.isEmpty()) {
			return null;
		}

		// 创建结果列表
		List<Member> resultList = new ArrayList<>(ml.size());

		for (Member m : ml) {
			// 获取注解
			T $annoObj = null;

			if (m instanceof AccessibleObject) {
				// 获取注解
				$annoObj = ((AccessibleObject)m).getAnnotation(hasAnnotation);
			}
	
			if ($annoObj == null) {
				// 如果注解对象为空, 
				// 则直接跳过!
				continue;
			}

			// 添加方法对象到结果列表
			resultList.add(m);
		}

		return resultList;
	}

	/**
	 * 获取定义或返回类型
	 * 
	 * @param $m
	 * @return 
	 * 
	 */
	public static Class<?> getDefOrRetType(Member $m) {
		// 获取返回值类型
		Class<?> typeClazz = null;

		if ($m instanceof Field) {
			// 如果是字段, 
			// 则获取定义类型
			typeClazz = ((Field)$m).getType();
		} else 
		if ($m instanceof Method) {
			// 如果是函数, 
			// 则获取返回类型
			typeClazz = ((Method)$m).getReturnType();
		}

		return typeClazz;
	}

	/**
	 * 是否为 get 方法 ?
	 * 
	 * @param m
	 * @return 
	 * 
	 */
	public static boolean isGetter(Member m) {
		return ((m != null) && 
			(m instanceof Method) && 
			(m.getName().startsWith("get") || m.getName().startsWith("is"))
		);
	}

	/**
	 * 从包package中获取所有的Class
	 *
	 * @param pack
	 * @return
	 */
	public static Set<Class<?>> getClasses( String pack) {

		// 第一个class类的集合
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		// 是否循环迭代
		boolean recursive = true;
		// 获取包的名字 并进行替换
		String packageName = pack;
		String packageDirName = packageName.replace('.', '/');
		// 定义一个枚举的集合 并进行循环来处理这个目录下的things
		Enumeration<URL> dirs;
		try {
			dirs = Thread.currentThread().getContextClassLoader().getResources(
					packageDirName);
			// 循环迭代下去
			while (dirs.hasMoreElements()) {
				// 获取下一个元素
				URL url = dirs.nextElement();
				// 得到协议的名称
				String protocol = url.getProtocol();
				// 如果是以文件的形式保存在服务器上
				if ("file".equals(protocol)) {
					// 获取包的物理路径
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					// 以文件的方式扫描整个包下的文件 并添加到集合中
					findAndAddClassesInPackageByFile(packageName, filePath,
							recursive, classes);
				} else if ("jar".equals(protocol)) {
					// 如果是jar包文件
					// 定义一个JarFile
					System.err.println("jar类型的扫描");
					JarFile jar;
					try {
						// 获取jar
						jar = ((JarURLConnection) url.openConnection())
								.getJarFile();
						// 从此jar包 得到一个枚举类
						Enumeration<JarEntry> entries = jar.entries();
						// 同样的进行循环迭代
						while (entries.hasMoreElements()) {
							// 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
							JarEntry entry = entries.nextElement();
							String name = entry.getName();
							// 如果是以/开头的
							if (name.charAt(0) == '/') {
								// 获取后面的字符串
								name = name.substring(1);
							}
							// 如果前半部分和定义的包名相同
							if (name.startsWith(packageDirName)) {
								int idx = name.lastIndexOf('/');
								// 如果以"/"结尾 是一个包
								if (idx != -1) {
									// 获取包名 把"/"替换成"."
									packageName = name.substring(0, idx)
											.replace('/', '.');
								}
								// 如果可以迭代下去 并且是一个包
								if ((idx != -1) || recursive) {
									// 如果是一个.class文件 而且不是目录
									if (name.endsWith(".class")
											&& !entry.isDirectory()) {
										// 去掉后面的".class" 获取真正的类名
										String className = name.substring(
												packageName.length() + 1, name
														.length() - 6);
										try {
											// 添加到classes
											classes.add(Class
													.forName(packageName + '.'
															+ className));
										} catch (ClassNotFoundException e) {
											log.error("添加用户自定义视图类错误 找不到此类的.class文件");
										}
									}
								}
							}
						}
					} catch (IOException e) {
						log.error("在扫描用户定义视图时从jar包获取文件出错");
					}
				}
			}
		} catch (IOException e) {
			log.error(e.getMessage(),e);
		}

		return classes;
	}

	/**
	 * 以文件的形式来获取包下的所有Class
	 *
	 * @param packageName
	 * @param packagePath
	 * @param recursive
	 * @param classes
	 */
	public static void findAndAddClassesInPackageByFile(String packageName,
														String packagePath, final boolean recursive, Set<Class<?>> classes) {
		// 获取此包的目录 建立一个File
		File dir = new File(packagePath);
		// 如果不存在或者 也不是目录就直接返回
		if (!dir.exists() || !dir.isDirectory()) {
			// log.warn("用户定义包名 " + packageName + " 下没有任何文件");
			return;
		}
		// 如果存在 就获取包下的所有文件 包括目录
		File[] dirfiles = dir.listFiles(new FileFilter() {
			// 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
			public boolean accept(File file) {
				return (recursive && file.isDirectory())
						|| (file.getName().endsWith(".class"));
			}
		});
		// 循环所有文件
		for (File file : dirfiles) {
			// 如果是目录 则继续扫描
			if (file.isDirectory()) {
				findAndAddClassesInPackageByFile(packageName + "."
								+ file.getName(), file.getAbsolutePath(), recursive,
						classes);
			} else {
				// 如果是java类文件 去掉后面的.class 只留下类名
				String className = file.getName().substring(0,
						file.getName().length() - 6);
				try {
					// 添加到集合中去
					//classes.add(Class.forName(packageName + '.' + className));
					//经过回复同学的提醒，这里用forName有一些不好，会触发static方法，没有使用classLoader的load干净
					classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
				} catch (ClassNotFoundException e) {
					// log.error("添加用户自定义视图类错误 找不到此类的.class文件");
				}
			}
		}
	}

}
