package com.jule.db.proxy;

import com.google.common.collect.Lists;
import com.jule.db.cache.CommMem;
import com.jule.core.common.log.LoggerUtils;
import com.jule.db.dao.BaseDbBean;
import java.util.List;
import java.util.Map;

/**
 * 缓存层操作代理
 * @author ran
 */
public class CacheProxy extends IProxy{
	/* 缓存超时时间 */
	private static int cache_timeout=24*60*60;
	/* id缓存中列表超时时间 */
	private static int id_cache_timeout=30*24*60*60;

	@Override
	public <T extends BaseDbBean> int insert( T entity) {
		if (this.nextProxyLayer == null) {
			return 0;
		}
		// 调用下层操作数据
		int	saveOk = this.nextProxyLayer.insert(entity);
		if(saveOk == 0 ){
			LoggerUtils.daoLogger.error("insert error");
			return saveOk;
		}
		
		// 添加到缓存
		String key = CommMem.OBJ.getDBUniqueKey(entity.getId(),entity.getClass().getSimpleName());
		CommMem.OBJ.getCache().setexMemInfo(key,cache_timeout,entity);
		return 1;
	}
	
	@Override
	public <T extends BaseDbBean> int insertAll(List<T> entityList) {
		if (this.nextProxyLayer == null) {
			return 0;
		}
		// 调用下层操作数据
		int	saveOk = this.nextProxyLayer.insertAll(entityList);
		if(saveOk == 0 ){
			LoggerUtils.daoLogger.error("insertAll error");
			return saveOk;
		}
		
		return 1;
	}
	
	@Override
	public <T extends BaseDbBean> int insertCacheId(T entity) {
		int res = insert(entity);
		
		setCacheKey(entity);
		return res;
	}
	
	private <T extends BaseDbBean> void setCacheKey(T entity){
		String clazzName = entity.getClass().getSimpleName();
		String key = CommMem.OBJ.getDBUniqueKey(entity.getId(),clazzName);
		
		CacheKeyBean keyBean = new CacheKeyBean();
		keyBean.setKeyName(key);
		
		List<CacheKeyBean> list = CommMem.OBJ.getCache()
				.getMemList(CommMem.MEM_ID_PRE + clazzName, CacheKeyBean.class);
		if(list != null){
			for(CacheKeyBean b : list){
				if(key.equals(b.getKeyName())){
					// 缓存列表中已经有了
					return;
				}
			}
		}
		
		list = Lists.newArrayList(keyBean);
		CommMem.OBJ.getCache().putMemList(CommMem.MEM_ID_PRE + clazzName,list
				, CacheKeyBean.class, id_cache_timeout);
	}

	@Override
	public <T extends BaseDbBean> int update(T entity,Class<T> clazz) {
		entity.writeDbBeforeEvent();
		// 调用下层操作数据
		if (this.nextProxyLayer != null) {
			this.nextProxyLayer.update(entity,clazz);
		}

		//更新并设置24小时后超时
		String key = CommMem.OBJ.getDBUniqueKey(entity.getId(),entity.getClass().getSimpleName());
		CommMem.OBJ.getCache().setexMemInfo(key,cache_timeout,entity);
		return 1;
	}
	
	@Override
	public <T extends BaseDbBean> int updateAll(List<T> entityList,Class<T> clazz) {
		// 调用下层操作数据
		if (this.nextProxyLayer != null) {
			this.nextProxyLayer.updateAll(entityList,clazz);
		}

		//清楚缓存
		List<String> keyList = Lists.newArrayList();
		entityList.forEach(newEntity -> 
		{
			String key = CommMem.OBJ.getDBUniqueKey(newEntity.getId(),newEntity.getClass().getSimpleName());
			keyList.add(key);
		});
		
		CommMem.OBJ.getCache().removeCacheByKeyList(keyList);
		return 1;
	}

	@Override
	public <T extends BaseDbBean> int delete(T entity) {
		String key = CommMem.OBJ.getDBUniqueKey(entity.getId(),entity.getClass().getSimpleName());
		CommMem.OBJ.getCache().removeCacheByKey(key);
		
		// 调用下层操作数据
		if (this.nextProxyLayer != null) {
			this.nextProxyLayer.delete(entity);
		}
		return 1;
	}
	
	@Override
	public <T extends BaseDbBean> int delete(Object id,Class<T> clazz) {
		String key = CommMem.OBJ.getDBUniqueKey(id,clazz.getSimpleName());
		CommMem.OBJ.getCache().removeCacheByKey(key);
		
		// 调用下层操作数据
		if (this.nextProxyLayer != null) {
			this.nextProxyLayer.delete(id,clazz);
		}

		return 1;
	}
	
	@Override
	public <T extends BaseDbBean> int deleteAll(List<?> idList,  Class<T> clazz) {
		List<String> keyList = Lists.newArrayList();
		for(Object id : idList){
			String key = CommMem.OBJ.getDBUniqueKey(id,clazz.getSimpleName());
			keyList.add(key);
		}
		CommMem.OBJ.getCache().removeCacheByKeyList(keyList);
		
		// 调用下层操作数据
		if (this.nextProxyLayer != null) {
			this.nextProxyLayer.deleteAll(idList, clazz);
		}

		return 1;
	}

	@Override
	public <T extends BaseDbBean> T get(Object id,Class<T> clazz) {
		String key = CommMem.OBJ.getDBUniqueKey(id,clazz.getSimpleName());
		T back = CommMem.OBJ.getCache().getMemInfo(key,clazz);
		//如果缓存中命中 直接返回
		if(back != null){
			back.readDbAfterEvent();
			return back;
		}
		
		// 没有取道，调用下层代理取
		back = this.nextProxyLayer.get(id,clazz);
		if(back == null){
			LoggerUtils.daoLogger.debug("back is null,key="+key);
			return null;
		}
		
		// 重新添加到缓存
		CommMem.OBJ.getCache().setexMemInfo(key,cache_timeout,back);
		return back;
	}
	
	@Override
	public <T extends BaseDbBean> T getAndCacheId(Object id,Class<T> clazz) {
		T back = get(id,clazz);
		if(back == null){
			return back;
		}
		
		setCacheKey(back);
		return back;
	}

	@Override
	public <T extends BaseDbBean> List<T> findAllByRoleId(long roleId,
			Class<T> clazz) {
		// 调用下层操作数据
		return this.nextProxyLayer.findAllByRoleId(roleId,clazz);
	}

	@Override
	public <TEntity> List<TEntity> getResultList(String jpqlWhere,
			Map<String, Object> paramsMap,Class<TEntity> clazz) {
		// 调用下层操作数据
		return this.nextProxyLayer.getResultList(jpqlWhere,paramsMap,clazz);
	}

	@Override
	public <TEntity> List<TEntity> getResultListLimit(String jpqlWhere,
			Map<String, Object> paramsMap,int start, int count,
			Class<TEntity> clazz) {
		// 调用下层操作数据
		return this.nextProxyLayer.getResultListLimit(jpqlWhere
				,paramsMap,start,count,clazz);
	}

	@Override
	public <TEntity> TEntity getSingleResult(String jpqlWhere,
			Map<String, Object> paramsMap,Class<TEntity> clazz) {
		// 调用下层操作数据
		return this.nextProxyLayer.getSingleResult(jpqlWhere
				,paramsMap,clazz);
	}

	@Override
	public int getIntResult(String sql, Map<String, Object> paramsMap) {
		// 调用下层操作数据
		return this.nextProxyLayer.getIntResult(sql,paramsMap);
	}
	
	@Override
	public List<?> queryNativeSQL(String sql, Map<String, Object> paramsMap) {
		// 调用下层操作数据
		return this.nextProxyLayer.queryNativeSQL(sql,paramsMap);
	}

	@Override
	public int executeUpdate(String sql, Map<String, Object> paramsMap) {
		// 调用下层操作数据
		return this.nextProxyLayer.executeUpdate(sql,paramsMap);
	}
	
	@Override
	public int executeNativeUpdate(String sql, Map<String,Object> paramsMap) {
		return this.nextProxyLayer.executeNativeUpdate(sql, paramsMap);
	};

	@Override
	public <TEntity> Object getIntByJpaSqlResult(String jpaSQL,
			Class<TEntity> clazz) {
		// 调用下层操作数据
		return this.nextProxyLayer.getIntByJpaSqlResult(jpaSQL,clazz);
	}

	@Override
	@Deprecated
	public <TEntity> int getfindAllCount(String jpqlWhere,Class<TEntity> clazz) {
		// 调用下层操作数据
		return this.nextProxyLayer.getfindAllCount(jpqlWhere,clazz);
	}
	
}
