package com.jule.db.dao;


import com.jule.core.common.log.LoggerUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * 获取结果列表
 * @author Thinker
 */
interface IDaoGetResultList 
{
	/** 选取数据列表 */
	String JPQL_SELECTFROM = "select obj from {0} obj where {1}";

	/**
	 * 获取结果列表
	 * @param clazz
	 * @param jpqlWhere JPQL 查询语句中 where 后面的语句, 注意 : where 语句中需要使用 "obj." 前缀! 例如 : obj._userName
	 * @param paramsMap 
	 * @param start
	 * @param count
	 * @return 
	 * 
	 */
	@SuppressWarnings("unchecked")
	default<TEntity> List<TEntity> getResultList( Class<TEntity> clazz, String jpqlWhere,
                                                  Map<String, Object> paramsMap, int start, int count )
	{
		//如果参数对象为空,则直接退出!
		if(clazz == null || count <= 0) 
		{
			return null;
		}
		
		//如果查询条件为空,那么给一个永远为真的条件 ...
		if(jpqlWhere == null) 
		{
			jpqlWhere = "0 = 0";
		}

		//获取 HQL 查询
		String jpql = jpql = MessageFormat.format(JPQL_SELECTFROM, clazz.getName(), jpqlWhere);
		//获取实体管理器
		EntityManager em = CommDao.OBJ.getEntityManager();
		//如果实体管理器为空,则直接退出!
		if(em == null) 
		{
			return null;
		}
			
		List<TEntity> objList = null;
		try
		{
			//创建查询
			Query q = em.createQuery(jpql);
			q.setHint("eclipselink.read-only", "true");
			q.setFirstResult(start).setMaxResults(count);

			if (paramsMap != null && paramsMap.isEmpty() == false)
			{
				paramsMap.entrySet().forEach(entry -> 
				{
					//如果进入点为空,则直接退出!
					if(entry == null) 
					{
						return;
					}
					//设置参数
					q.setParameter(entry.getKey(), entry.getValue());
				});
			}
			objList = q.getResultList();
//			tranx.commit();
			for(TEntity retEntity:objList)
			{
				((BaseDbBean)retEntity).readDbAfterEvent();
			}
		} catch (Exception ex) 
		{
			LoggerUtils.error.error(ex.getMessage(), ex);
		}finally
		{
			em.close();
		}
		
		return objList;
	}
	
	/**
	 * 获取结果列表
	 * @param <TEntity> 
	 * @param clazz
	 * @param hqlWhere
	 * @param paramsMap 
	 * @return
	 * 
	 */
	default<TEntity> List<TEntity> getResultList( Class<TEntity> clazz, String hqlWhere
            , Map<String, Object> paramsMap )
	{
		return this.getResultList(clazz,hqlWhere,paramsMap,0,Integer.MAX_VALUE);
	}

	/**
	 * 获取结果列表
	 * @param <TEntity> 
	 * @param clazz
	 * @param hqlWhere
	 * @return
	 */
	default<TEntity> List<TEntity> getResultList( Class<TEntity> clazz, String hqlWhere )
	{
		return this.getResultList(clazz,hqlWhere,null,0,Integer.MAX_VALUE);
	}

	/**
	 * 获取结果列表
	 * @param <TEntity> 
	 * @param clazz
	 * @return
	 */
	default<TEntity> List<TEntity> getResultList( Class<TEntity> clazz, int start, int count )
	{
		return this.getResultList(clazz,null,null,start,count);
	}

	/**
	 * 获取结果列表
	 * @param <TEntity> 
	 * @param clazz
	 * @return
	 * 
	 */
	default<TEntity> List<TEntity> getResultList( Class<TEntity> clazz )
	{
		return this.getResultList(clazz, null,null,0,Integer.MAX_VALUE);
	}
}
