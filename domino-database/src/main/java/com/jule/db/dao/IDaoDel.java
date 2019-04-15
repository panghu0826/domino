package com.jule.db.dao;

import com.jule.core.common.log.LoggerUtils;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 删除数据库实体
 * 
 * @author Thinker
 */
interface IDaoDel
{
	/** Id 字段名称字典 */
	ConcurrentHashMap<Class<?>, String> _idFieldNameMap = new ConcurrentHashMap<>();
	
	/** 删除实体 */
	String JPQL_DEL = "delete from {0} obj where obj.{1} = :id";

	/**
	 * 删除数据实体
	 * 
	 * @param entity
	 * 
	 */
	default boolean del( BaseDbBean entity )
	{
		if(entity == null)
		{
			return false;
		}

		return del(entity.getId(),entity.getClass());
	}

	/**
	 * 删除数据实体
	 * @param clazz
	 * @param id
	 * 
	 */
	default <T extends BaseDbBean> boolean del( Object id, Class<T> clazz )
	{
		//如果参数对象为空,则直接退出!
		if (clazz == null || id == null) 
		{
			return false;
		}

		//获取实体管理器
		EntityManager em = CommDao.OBJ.getEntityManager();
		//如果实体管理器为空,则直接退出!
		if (em == null)
		{
			return false;
		}

		//获取 Id 字段名称
		String idFieldName = CommDao.getIdFieldName(clazz);
		//构建 HQL 查询
		String hql = hql = MessageFormat.format(JPQL_DEL,clazz.getName(),idFieldName);

		EntityTransaction tranx = null;
		try 
		{
			//创建数据库事务
			tranx = em.getTransaction();
			//开始事务过程
			if(!tranx.isActive())
			{
				tranx.begin();
			}
			//创建并执行 SQL 查询
			Query q = em.createQuery(hql).setParameter("id", id);
			q.executeUpdate();
			//提交事务
			tranx.commit();
		} catch (Exception ex)
		{
			//记录错误日志
			LoggerUtils.error.error(ex.getMessage(), ex);
			if(tranx != null)
			{
				tranx.rollback();
			}
			return false;
		}finally
		{
			if(tranx !=null && tranx.isActive())
			{
				tranx.commit();
			}
			em.close();
		}
		return true;
	}

	/**
	 * 删除数据实体列表
	 * 
	 * @param clazz
	 * @param idList
	 * 
	 */
	default boolean delAll( List<?> idList, Class<?> clazz )
	{
		//如果参数对象为空,则直接退出!
		if (clazz == null || idList == null || idList.isEmpty()) 
		{
			return false;
		}

		//获取实体管理器
		EntityManager em = CommDao.OBJ.getEntityManager();
		//如果实体管理器为空,则直接退出!
		if (em == null) 
		{
			return false;
		}

		//获取 Id 字段名称
		String idFieldName = CommDao.getIdFieldName(clazz);
		//构建 HQL 查询
		String hql = hql = MessageFormat.format(JPQL_DEL,clazz.getName(),idFieldName);

		EntityTransaction tranx=null;
		try 
		{
			//创建数据库事务
			tranx=em.getTransaction();
			//开始事务过程
			if(!tranx.isActive())
			{
				tranx.begin();
			}
			//创建并执行 SQL 查询
			Query q = em.createQuery(hql);
	
			idList.forEach(id -> 
			{
				//设置 Id 参数
				q.setParameter("id", id);
				q.executeUpdate();
			});
			//提交事务
			tranx.commit();
		} catch (Exception ex) 
		{
			//记录错误日志
			LoggerUtils.error.error(ex.getMessage(), ex);
			if(tranx != null)
			{
				tranx.rollback();
			}
			return false;
		}finally
		{
			if(tranx !=null && tranx.isActive())
			{
				tranx.commit();
			}
			em.close();
		}
		
		return true;
	}
}
