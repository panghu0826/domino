package com.jule.db.dao;

import java.util.List;
import java.util.Map;

/**
 * 获取单个结果
 * @author Thinker
 */
interface IDaoGetSingleResult 
{
	/**
	 * 获取单个结果
	 * @param clazz
	 * @param jpqlWhere JPQL 查询语句中 where 后面的语句, 注意 : where 语句中需要使用 "obj." 前缀! 例如 : obj._userName
	 * @param paramsMap
	 * @return 
	 * 
	 */
	default<TEntity> TEntity getSingleResult( Class<TEntity> clazz, String jpqlWhere, Map<String, Object> paramsMap )
	{
		//获取实体列表
		List<TEntity> el = CommDao.OBJ.getResultList(clazz,jpqlWhere,paramsMap,0,1);
		if (el == null || el.isEmpty()) 
		{
			return null;
		} else 
		{
			return el.get(0);
		}
	}

	/**
	 * 获取单个结果
	 * @param clazz
	 * @param jpqlWhere
	 * @return 
	 * 
	 */
	default<TEntity> TEntity getSingleResult( Class<TEntity> clazz, String jpqlWhere )
	{
		return this.getSingleResult(clazz,jpqlWhere,null);
	}

	/**
	 * 获取单个结果
	 * @param clazz
	 * @return 
	 * 
	 */
	default<TEntity> TEntity getSingleResult( Class<TEntity> clazz)
	{
		return this.getSingleResult(clazz,null,null);
	}
}
