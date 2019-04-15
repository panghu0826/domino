package com.jule.db.dao;


import com.jule.core.common.log.LoggerUtils;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

/**
 * 保存数据库实体
 * @author Thinker
 */
interface IDaoSave 
{
	/**
	 * 添加数据库实体
	 */
	default boolean save( Object entityObj )
	{
		//如果参数对象为空,则直接退出!
		if(entityObj == null)
		{
			return false;
		}
		
		//获取实体管理器
		EntityManager em=CommDao.OBJ.getEntityManager();
		//如果实体管理器为空,则直接退出!
		if(em == null) 
		{
			LoggerUtils.error.error("保存实体对象时EntityManager为null");
			return false;
		}

		EntityTransaction tranx = null;
		try
		{
			((BaseDbBean)entityObj).writeDbBeforeEvent();
			//获取数据库事务
			tranx=em.getTransaction();
			//开始事务过程
			if(!tranx.isActive())
			{
				tranx.begin();
			}
			//保存实体
			em.persist(entityObj);
			//提交事务
			tranx.commit();
		} catch (Exception ex) 
		{
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
	 * 添加数据库实体列表
	 */
	default<T extends BaseDbBean> boolean saveAll( List<T> entityObjList )
	{
		//如果参数对象为空,则直接退出!
		if (entityObjList == null || entityObjList.isEmpty()) 
		{
			return false;
		}
		
		//获取实体管理器
		EntityManager em=CommDao.OBJ.getEntityManager();
		//如果实体管理器为空,则直接退出!
		if(em == null) 
		{
			return false;
		}

		EntityTransaction tranx = null;
		try 
		{
			//事务前处理
			entityObjList.forEach(newEntity -> 
			{
				//保存实体
				((BaseDbBean)newEntity).writeDbBeforeEvent();
			});
			//获取数据库事务
			tranx=em.getTransaction();
			//开始事务过程
			if(!tranx.isActive())
			{
				tranx.begin();
			}

			entityObjList.forEach(newEntity -> 
			{
				//保存实体
				em.persist(newEntity);
			});
			//提交事务
			tranx.commit();
		} catch (Exception ex) 
		{
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
