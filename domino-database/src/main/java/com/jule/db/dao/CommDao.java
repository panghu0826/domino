package com.jule.db.dao;

import com.jule.core.database.DatabaseConfig;
import com.jule.db.common.ClazzUtil;
import com.jule.core.common.log.LoggerUtils;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.tools.schemaframework.SchemaManager;

import javax.persistence.*;
import javax.persistence.metamodel.EntityType;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用 DAO
 * @author ran
 * 
 */
public class CommDao implements IDaoSave,IDaoUpdate,IDaoDel,IDaoGetResultList,IDaoGetSingleResult,IDaoExecNativeSQL 
{
	/** 单例对象 */
	public static final CommDao OBJ = new CommDao();
	/** 实体管理器工厂 */
	private EntityManagerFactory _emf = null;
	
	private CommDao() 
	{
		
	}
	
	/**
	 * 初始化DB配置信息
	 */
	public void initDbConfig()
	{
		//设置jdbc配置项
		Map<String,String> properties=new HashMap<String,String>();
		//String jdbcUrl=MessageFormat.format(appConfigBean.getJdbcUrl(),appConfigBean.getDatabaseName());
		properties.put("javax.persistence.jdbc.url", DatabaseConfig.DATABASE_URL);
		properties.put("javax.persistence.jdbc.user", DatabaseConfig.DATABASE_USER);
		properties.put("javax.persistence.jdbc.password", DatabaseConfig.DATABASE_PASSWORD);
		properties.put("eclipselink.jdbc.exclusive-connection.mode", "Transactional");
		properties.put("eclipselink.jdbc.exclusive-connection.is-lazy", "false");

		LoggerUtils.coreLogger.info("url="+DatabaseConfig.DATABASE_URL);
		LoggerUtils.coreLogger.info("use="+DatabaseConfig.DATABASE_USER);
		LoggerUtils.coreLogger.info("pas="+DatabaseConfig.DATABASE_PASSWORD);


		// 创建实体管理器工厂
		EntityManagerFactory value = null;
		try {
			value = Persistence.createEntityManagerFactory("game_db",properties);
		}catch (Exception e){
			LoggerUtils.error.info("JPA实体工厂创建失败!!! error->"+e);
		}
		LoggerUtils.daoLogger.info("JPA实体工厂创建成功");

		//设置实体工厂
		putEMFAndCreateTables(value);
	}
	
	/**
	 * 设置实体管理器工厂
	 */
	private CommDao putEMFAndCreateTables(EntityManagerFactory value) 
	{
		//断言参数不为空
		//LLAssert.notNull(value);

		if (this._emf != null) 
		{
			//throw new Error("重复设置实体管理器工厂");
			LoggerUtils.error.error("EntityManagerFactory already init");
			return this;
		}

		this._emf = value;
		
		// 他建tables
		try {
			SchemaManager schemaManager = new SchemaManager(JpaHelper.getDatabaseSession(this._emf));
			schemaManager.extendDefaultTables(true);
		} catch (Exception e){
			LoggerUtils.coreLogger.info("JPA扫描并建表失败!!!error->"+e);
		}
		LoggerUtils.daoLogger.info("JPA扫描并建表成功");
		return this;
	}
	
	/**
	 * 检查缺失的字段
	 */
	public void checkEntities(){
		EntityManager entityManager = getEntityManager();
		for (EntityType<?> entity : entityManager.getMetamodel().getEntities()) {
		    final String className = entity.getName();
		    LoggerUtils.daoLogger.info("Trying check entity: " + className);
		    Query q = entityManager.createQuery("from " + className + " c");
		    q.setMaxResults(1).getResultList();
		}
		entityManager.close();
	}
	
	/**
	 * 关闭退出
	 */
	public void closeEMF()
	{
		if(this._emf == null) return;
		
		if(this._emf.isOpen())
		{
			this._emf.close();
		}
	}

	/**
	 * 查找数据库实体
	 * 
	 * @param <TEntity> 
	 * @param clazz
	 * @param id
	 * @return 
	 * 
	 */
	public<TEntity> TEntity find(Class<TEntity> clazz, Object id)
	{
		if (clazz == null || id == null) 
		{
			return null;
		}
		
		String idFieldName = getIdFieldName(clazz);
		StringBuilder where = new StringBuilder("obj.")
			.append(idFieldName)
			.append(" = ");
		if(id instanceof String){
			where.append("'")
				 .append(String.valueOf(id))
				 .append("'");
		}else{
			where.append(String.valueOf(id));
		}
		return getSingleResult(clazz,where.toString());
	}
	
	/** 
	 * 获得EntityManager对象
	 */
	public EntityManager getEntityManager()
	{
		if(_emf == null){
			// 兼容报空的问题
			initDbConfig();
		}
		return _emf.createEntityManager();
	}
	
	/**
	 * 获取标注了 @Id 注解的字段名称
	 * 
	 * @param fromClazz
	 * @return
	 * 
	 */
	static String getIdFieldName(Class<?> fromClazz) 
	{
		if(fromClazz == null) 
		{
			return null;
		}

		//首先从字典里找一下这个类对应的 Id 字段名称,获取 Id 字段名称
		String idFieldName= _idFieldNameMap.get(fromClazz);
		if (idFieldName != null) 
		{
			return idFieldName;
		}
		
		//接下来就要处理在字典中没找到的情况,从类中获取标注了 Id 的字段
		Field idField = ClazzUtil.getField(fromClazz, ( f) -> { return f != null && f.getAnnotation(Id.class) != null; });
		//如果字段为空,则抛出异常!
		if(idField == null) 
		{
			throw new Error(MessageFormat.format("在 {0} 类中没有找到标注了 @Id 注解的字段",fromClazz.getName()));
		}
		//获取字段名称
		idFieldName = idField.getName();
		//添加 Id 字段名称到字典
		_idFieldNameMap.put(fromClazz, idFieldName);
		return idFieldName;
	}

	/**
	 * ID是否自增
	 * @return
	 * 			true - 自增
	 * 			false - 否
	 */
	public static boolean isAutoIncrement(Class<?> fromClazz)
	{
		if (fromClazz == null){
			return false;
		}

		//查找类中的@ID字段
		Field idField = ClazzUtil.getField(fromClazz, (f) -> { return f != null && f.getAnnotation(Id.class) != null; });
		if (idField == null){
			return false;
		}

		//获取字段的注解属性
		GeneratedValue value = idField.getAnnotation(GeneratedValue.class);
		if (value.strategy() == GenerationType.IDENTITY){
			//如果注解属性是自增
			return true;
		}
		return false;
	}
}
