package com.jule.db.proxy;

import com.jule.db.dao.BaseDbBean;
import java.util.List;
import java.util.Map;


/**
 * 数据代理接口:其中humanId主要用来区分连接
 * dbId用来作为缓存主键使用
 * @author ran
 *
 */
public abstract class IProxy 
{
	/**
	 * 获取下一层代理层
	 */
	public IProxy nextProxyLayer=null;

	/**
	 * 插入一个entity
	 * 
	 * @param <T>
	 *            数据库操作entity 的类
	 * @param humanId
	 *            角色id
	 * @param entity
	 *            数据库操作entity
	 * @return 返回操作条数
	 */
	abstract public <T extends BaseDbBean> int insert( T entity);
	
	/**
	 * 批量插入entity
	 * 
	 * @param <T>
	 *            数据库操作entity 的类
	 * @param humanId
	 *            角色id
	 * @param entity
	 *            数据库操作entity
	 * @return 返回操作条数
	 */
	abstract public <T extends BaseDbBean> int insertAll(List<T> entity);
	
	/**
	 * 插入一个entity 同时把id缓存在cache列表中
	 * 
	 * @param <T>
	 *            数据库操作entity 的类
	 * @param humanId
	 *            角色id
	 * @param entity
	 *            数据库操作entity
	 * @return 返回操作条数
	 */
	abstract public <T extends BaseDbBean> int insertCacheId(T entity);

	/**
	 * 更新一个entity
	 * 
	 * @param <T>
	 *            数据库操作entity 的类
	 * @param humanId
	 *            角色id
	 * @param entity
	 *            数据库操作entity
	 * @return 返回操作条数
	 * @throws Exception
	 *             向上抛出错误
	 */
	abstract public <T extends BaseDbBean> int update(T entity,Class<T> clazz);
	
	/**
	 * 批量更新entity
	 * 
	 * @param <T>
	 *            数据库操作entity 的类
	 * @param humanId
	 *            角色id
	 * @param entity
	 *            数据库操作entity
	 * @return 返回操作条数
	 * @throws Exception
	 *             向上抛出错误
	 */
	abstract public <T extends BaseDbBean> int updateAll(List<T> entityList,Class<T> clazz);


	/**
	 * 删除一个entity
	 * 
	 * @param <T>
	 *            数据库操作entity 的类
	 * @param humanId
	 *            角色id
	 * @param entity
	 *            数据库操作entity
	 * @return 返回操作条数
	 * @throws Exception
	 *             向上抛出错误
	 */
	abstract public <T extends BaseDbBean> int delete(T entity);
	
	/**
	 * 根据指定id删除一个entity
	 * 
	 * @param id
	 *            主键id
	 * @param clazz
	 *            数据库操作entity 的类
	 * @return
	 */
	abstract public <T extends BaseDbBean> int delete(Object id,Class<T> clazz);
	
	/**
	 * 批量指定id的entity
	 * 
	 * @param idList
	 *            主键id列表
	 * @param clazz
	 *            数据库操作entity 的类
	 * @return
	 */
	abstract public <T extends BaseDbBean> int deleteAll(List<?> idList,Class<T> clazz);

	/**
	 * 根据id获取一个单条数据
	 * 
	 * @param <T>
	 *            数据库操作entity 的类
	 * @param humanId
	 *            角色id
	 * @param dbId
	 *            数据唯一id
	 * @param clazz
	 *            数据库操作entity 的类
	 * @return 一个entity
	 */
	abstract public <T extends BaseDbBean> T get(Object id,Class<T> clazz);
	
	/**
	 * 根据id获取一个单条数据 同时缓存id到cache列表中
	 * 
	 * @param <T>
	 *            数据库操作entity 的类
	 * @param humanId
	 *            角色id
	 * @param dbId
	 *            数据唯一id
	 * @param clazz
	 *            数据库操作entity 的类
	 * @return 一个entity
	 */
	abstract public <T extends BaseDbBean> T getAndCacheId(Object id,Class<T> clazz);

	/**
	 * 根据用户id此人所以数据
	 * <p>
	 * 	此entity对象必须包含有字段“roleId”
	 * </p>
	 * 
	 * @param <T>
	 *            数据库操作entity 的类
	 * @param humanId
	 *            角色id
	 * @param clazz
	 *            数据库操作entity 的类
	 * @return 一个entity
	 * @throws Exception
	 *             向上抛出错误
	 */
	abstract public <T extends BaseDbBean> List<T> findAllByRoleId(long roleId,Class<T> clazz);
	
	/**
	 * 根据复杂sql得到列表:不会缓存数据直接数据库中获取数据
	 * @param clazz
	 * @param jpqlWhere
	 * @param paramsMap
	 * @return
	 */
	abstract public <TEntity> List<TEntity> getResultList(String jpqlWhere,Map<String, Object> paramsMap
			,Class<TEntity> clazz);
	
	
	/**
	 * 根据复杂sql得到列表,并且分段
	 * @param clazz
	 * @param jpqlWhere
	 * @param paramsMap
	 * @return
	 */
	abstract public <TEntity> List<TEntity> getResultListLimit(String jpqlWhere,
			Map<String, Object> paramsMap,int start,int count,Class<TEntity> clazz);
			
	/**
	 * 根据复杂sql得到单个数据:不会缓存数据直接数据库中获取数据
	 * @param clazz
	 * @param jpqlWhere
	 * @param paramsMap
	 * @return
	 */
	abstract public <TEntity> TEntity getSingleResult(String jpqlWhere,Map<String, Object> paramsMap,Class<TEntity> clazz);
	
	/**
	 * 
	 * @param jpqlWhere
	 * @param paramsMap
	 * @return
	 */
	abstract public int getIntResult(String sql,Map<String, Object> paramsMap);
	
	/**
	 * 
	 * @param jpqlWhere
	 * @param paramsMap
	 * @return
	 */
	abstract public List<?> queryNativeSQL(String sql,Map<String, Object> paramsMap);
	
	
	
	/**
	 * executing a native jpql statement
	 * @param jpqlWhere
	 * @param paramsMap
	 * @return
	 */
	abstract public int executeUpdate(String sql,Map<String,Object> paramsMap);
	
	/**
	 * executing a native SQL statement, e.g., for update or delete.
	 * @param sqlWhere
	 * @param paramsMap
	 * @return
	 */
	abstract public int executeNativeUpdate(String sql, Map<String, Object> paramsMap);
	
	/**
	 * 
	 * @param jpqlWhere
	 * @param paramsMap
	 * @return
	 */
	abstract public <TEntity> Object getIntByJpaSqlResult(String jpaSQL,Class<TEntity> clazz);
	
	/**
	 * 获取数据库表的数量:直接数据库查询
	 * @param clazz
	 * @param whereSql
	 * @return
	 */
	abstract public <TEntity> int getfindAllCount(String jpqlWhere,Class<TEntity> clazz);
}

