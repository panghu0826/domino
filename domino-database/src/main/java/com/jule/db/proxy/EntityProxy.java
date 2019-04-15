package com.jule.db.proxy;

import com.jule.db.dao.BaseDbBean;
import java.util.List;
import java.util.Map;


/**
 * 实体代理接口
 * @author ran
 *
 */
public class EntityProxy extends IProxy
{
	public final static IProxy OBJ = EntityProxy.buildProxyLayer();

	private static IProxy entityProxy = null;

	private static IProxy cacheProxy = null;

	private static IProxy dbProxy = null;

	private EntityProxy()
	{
	}

	// 组织数据代理层
	private static IProxy buildProxyLayer()
	{
		// 第一层为mem代理层
		cacheProxy = new CacheProxy();

		// 第二层是db代理层
		dbProxy = new DBProxy();

		// 建立层次关系
		cacheProxy.nextProxyLayer = dbProxy;

		return cacheProxy;
	}
	
	public static IProxy getMemProxy() 
	{
		return cacheProxy;
	}

	public static void setMemProxy(IProxy memProxy) 
	{
		EntityProxy.cacheProxy = memProxy;
	}

	public static IProxy getDbProxy() 
	{
		return dbProxy;
	}

	public static void setDbProxy(IProxy dbProxy)
	{
		EntityProxy.dbProxy = dbProxy;
	}

	@Override
	public <T extends BaseDbBean> int insert( T entity)
	{
		int dbInsert = entityProxy.insert(entity);
		return dbInsert;
	}
	
	@Override
	public <T extends BaseDbBean> int insertAll(List<T> entityList)
	{
		int dbInsert = entityProxy.insertAll(entityList);
		return dbInsert;
	}
	
	@Override
	public <T extends BaseDbBean> int insertCacheId(T entity) {
		int dbInsert = entityProxy.insertCacheId(entity);
		return dbInsert;
	}
	
	@Override
	public <T extends BaseDbBean> int update(T entity,Class<T> clazz)
	{
		int dbUpdate = entityProxy.update(entity,clazz);
		return dbUpdate;
	}
	
	@Override
	public <T extends BaseDbBean> int updateAll(List<T> entityList,Class<T> clazz)
	{
		int dbUpdate = entityProxy.updateAll(entityList,clazz);
		return dbUpdate;
	}

	@Override
	public <T extends BaseDbBean> int delete(T entity)
	{
		int dbDelete = entityProxy.delete(entity);
		return dbDelete;
	}
	
	@Override
	public <T extends BaseDbBean> int delete(Object id,Class<T> clazz) {
		return entityProxy.delete(id, clazz);
	}
	
	@Override
	public <T extends BaseDbBean> int deleteAll(List<?> idList, Class<T> clazz) {
		return entityProxy.delete(idList, clazz);
	}

	@Override
	public <T extends BaseDbBean> T get(Object id, Class<T> clazz)
	{
		T entity = entityProxy.get(id,clazz);
		return entity;
	}
	
	@Override
	public <T extends BaseDbBean> T getAndCacheId(Object id,Class<T> clazz) {
		T entity = entityProxy.getAndCacheId(id,clazz);
		return entity;
	}

	@Override
	public <T extends BaseDbBean> List<T> findAllByRoleId(long roleId,Class<T> clazz)
	{
		List<T> datas = entityProxy.findAllByRoleId(roleId,clazz);
		return datas;
	}

	@Override
	public <TEntity> List<TEntity> getResultList(String jpqlWhere, Map<String, Object> paramsMap
			,Class<TEntity> clazz)
	{
		return dbProxy.getResultList(jpqlWhere,paramsMap,clazz);
	}

	@Override
	public int getIntResult(String sql, Map<String, Object> paramsMap) 
	{
		return dbProxy.getIntResult(sql, paramsMap);
	}
	
	@Override
	public List<?> queryNativeSQL(String sql, Map<String, Object> paramsMap) 
	{
		return dbProxy.queryNativeSQL(sql, paramsMap);
	}

	@Override
	public <TEntity> TEntity getSingleResult(String jpqlWhere, Map<String, Object> paramsMap
			,Class<TEntity> clazz)
	{
		return dbProxy.getSingleResult(jpqlWhere,paramsMap,clazz);
	}

	@Override
	public <TEntity> List<TEntity> getResultListLimit(String jpqlWhere, Map<String, Object> paramsMap
			, int start,int count,Class<TEntity> clazz)
	{
		return dbProxy.getResultListLimit(jpqlWhere, paramsMap,start,count,clazz);
	}

	@Override
	public <TEntity> Object getIntByJpaSqlResult(String jpaSQL,Class<TEntity> clazz)
	{
		return dbProxy.getIntByJpaSqlResult(jpaSQL, clazz);
	}

	@Override
	public int executeUpdate(String sql, Map<String, Object> paramsMap) 
	{
		return dbProxy.executeUpdate(sql, paramsMap);
	}
	
	@Override
	public int executeNativeUpdate(String sql, Map<String,Object> paramsMap) {
		return dbProxy.executeNativeUpdate(sql, paramsMap);
	};

	@Override
	@Deprecated
	public <TEntity> int getfindAllCount(String jpqlWhere,Class<TEntity> clazz)
	{
		return entityProxy.getfindAllCount(jpqlWhere,clazz);
	}
	
}
