package com.jule.db.proxy;

import com.jule.db.common.IdWorker;
import com.jule.core.common.log.LoggerUtils;
import com.jule.db.dao.BaseDbBean;
import com.jule.db.dao.CommDao;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库层操作代理类
 * @author ran
 */
public class DBProxy extends IProxy 
{
	@Override
	public<T extends BaseDbBean> int insert( T entity)
	{
		// 处理id不为数字形的，即字符串
		long id = -1l;
		String idStr = String.valueOf(entity.getId());
		if(StringUtils.isNumeric(idStr)){
			id = Long.valueOf(idStr);
		}

		if (id == 0 && !CommDao.isAutoIncrement(entity.getClass()))
		{
			try {
				entity.setId(IdWorker.getInstance().nextId());
			} catch (Exception e) {
				LoggerUtils.error.error("entity idWorker error:",e);
			}
		}
		boolean isOk = CommDao.OBJ.save(entity);
		return isOk ? 1 : 0;
	}
	
	@Override
	public<T extends BaseDbBean> int insertAll(List<T> entityList)
	{
		// 处理id不为数字形的，即字符串
		entityList.forEach(entity->{
			long id = -1l;
			String idStr = String.valueOf(entity.getId());
			if(StringUtils.isNumeric(idStr)){
				id = Long.valueOf(idStr);
			}
			
			if (id == 0 && !CommDao.isAutoIncrement(entity.getClass()))
			{
				try {
					entity.setId(IdWorker.getInstance().nextId());
				} catch (Exception e) {
					LoggerUtils.error.error("entity idWorker error:",e);
				}
			}
		});
		
		boolean isOk = CommDao.OBJ.saveAll(entityList);
		return isOk ? 1 : 0;
	}

	@Override
	public <T extends BaseDbBean> int update(T entity,Class<T> clazz)
	{
		boolean isOk = CommDao.OBJ.update(entity,clazz);
		return isOk ? 1 : 0;
	}
	
	@Override
	public <T extends BaseDbBean> int updateAll(List<T> entityList,Class<T> clazz)
	{
		boolean isOk = CommDao.OBJ.updateAll(entityList,clazz);
		return isOk ? 1 : 0;
	}

	@Override
	public <T extends BaseDbBean> int delete(T entity)
	{
		boolean isOk = CommDao.OBJ.del(entity);
		return isOk ? 1 : 0;
	}
	
	@Override
	public <T extends BaseDbBean> int deleteAll(List<?> idList,Class<T> clazz)
	{
		boolean isOk = CommDao.OBJ.delAll(idList, clazz);
		return isOk ? 1 : 0;
	}
	
	@Override
	public <T extends BaseDbBean> int delete(Object id,Class<T> clazz){
		boolean isOk = CommDao.OBJ.del(id,clazz);
		return isOk ? 1 : 0;
	}

	@Override
	public <T extends BaseDbBean> T get(Object id,Class<T> clazz)
	{
		return CommDao.OBJ.find(clazz,id);
	}

	@Override
	public <T extends BaseDbBean> List<T> findAllByRoleId(long roleId, Class<T> clazz)
	{
		Map<String, Object> params = new HashMap<>();
		params.put("uid", roleId);
		List<T> back = CommDao.OBJ.getResultList(clazz,"obj.uid =:uid",params);
		return back;
	}

	@Override
	public <TEntity> List<TEntity> getResultList(String jpqlWhere, Map<String, Object> paramsMap
			,Class<TEntity> clazz) 
	{
		return CommDao.OBJ.getResultList(clazz,jpqlWhere, paramsMap);
	}

	@Override
	public int getIntResult(String sql, Map<String, Object> paramsMap) 
	{
		return CommDao.OBJ.execNativeSQL(sql, paramsMap);
	}
	
	@Override
	public List<?> queryNativeSQL(String sql, Map<String, Object> paramsMap) 
	{
		return CommDao.OBJ.queryNativeSQL(sql, paramsMap);
	}

	@Override
	public <TEntity> TEntity getSingleResult(String jpqlWhere, Map<String, Object> paramsMap
			,Class<TEntity> clazz) 
	{
		return CommDao.OBJ.getSingleResult(clazz,jpqlWhere, paramsMap);
	}

	@Override
	public <TEntity> List<TEntity> getResultListLimit(String jpqlWhere, Map<String, Object> paramsMap
			, int start,int count,Class<TEntity> clazz) 
	{
		return CommDao.OBJ.getResultList(clazz,jpqlWhere, paramsMap,start,count);
	}

	@Override
	public <TEntity> Object getIntByJpaSqlResult(String jpaSQL, Class<TEntity> clazz) 
	{
		return CommDao.OBJ.execJpaSQL(jpaSQL, clazz);
	}

	@Override
	public int executeUpdate(String sql, Map<String, Object> paramsMap)
	{
		return CommDao.OBJ.executeUpdate(sql, paramsMap);
	}
	
	@Override
	public int executeNativeUpdate(String sql, Map<String,Object> paramsMap) {
		return CommDao.OBJ.executeNativeUpdate(sql, paramsMap);
	};

	@Override
	@Deprecated
	public <TEntity> int getfindAllCount(String jpqlWhere,Class<TEntity> clazz) 
	{
		return CommDao.OBJ.getResultList(clazz,jpqlWhere, null).size();
	}

	@Override
	@Deprecated
	public <T extends BaseDbBean> int insertCacheId(T entity) {
		return 0;
	}

	@Override
	@Deprecated
	public <T extends BaseDbBean> T getAndCacheId(Object id,Class<T> clazz) {
		return null;
	}
}
