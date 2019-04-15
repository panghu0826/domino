package com.jule.db.dao;


import com.jule.db.common.ClazzUtil;
import com.jule.core.common.log.LoggerUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Transient;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.util.List;

/**
 * 更新数据库实体
 * @author Thinker
 * 
 */
interface IDaoUpdate 
{
	/**
	 * 添加数据库实体
	 */
	default<T extends BaseDbBean> boolean update( T entityObj, Class<T> clazz )
	{
		//如果参数对象为空,则直接退出!
		if(entityObj==null) return false;
		
		//获取实体管理器
		EntityManager em=CommDao.OBJ.getEntityManager();

		//如果实体管理器为空,则直接退出!
		if(em == null)
		{
			LoggerUtils.error.error("更新实体对象时EntityManager为null");
			return false;
		}
		
		EntityTransaction tranx = null;
		try 
		{
			entityObj.writeDbBeforeEvent();
			//设置条件
			CriteriaUpdate<T> update = updateWhere(em.getCriteriaBuilder(),entityObj ,clazz);
			
			//获取数据库事务
			tranx = em.getTransaction();
			//开始事务过程
			if(!tranx.isActive())
			{
				tranx.begin();
			}
			//保存实体
			em.createQuery(update).executeUpdate();
			//提交事务
			tranx.commit();
		} catch (Exception ex) 
		{
			//记录错误日志
			LoggerUtils.error.error(ex.getMessage(), ex);
			if(tranx!=null)
			{
				tranx.rollback();
			}
			return false;
		}finally
		{
			if(tranx!=null && tranx.isActive())
			{
				tranx.commit();
			}
			em.close();
		}
		return true;
	}

	/**
	 * 更新数据库实体列表
	 */
	default<TEntity extends BaseDbBean> boolean updateAll( List<TEntity> entityObjList, Class<TEntity> clazz )
	{
		//如果参数对象为空,则直接退出!
		if(entityObjList == null || entityObjList.isEmpty()) 
		{
			return false;
		}
		
		// 获取实体管理器
		EntityManager em=CommDao.OBJ.getEntityManager();
		//如果实体管理器为空,则直接退出!
		if(em==null) 
		{
			return false;
		}

		boolean isOk = false;
		EntityTransaction tranx = null;
		try 
		{
			entityObjList.forEach(newEntity -> 
			{
				newEntity.writeDbBeforeEvent();
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
				//设置条件
				CriteriaUpdate<TEntity> update = updateWhere(em.getCriteriaBuilder()
						,newEntity,clazz);
				//保存实体
				em.createQuery(update).executeUpdate();
			});
			//提交事务
			tranx.commit();
			isOk = true;
		} catch (Exception ex) 
		{
			//记录错误日志
			LoggerUtils.error.error(ex.getMessage(),ex);
			if(tranx != null)
			{
				tranx.rollback();
			}
		}finally
		{
			if(tranx !=null && tranx.isActive())
			{
				tranx.commit();
			}
			em.close();
		}
		
		return isOk;
	}
	
	default<T extends BaseDbBean> CriteriaUpdate<T> updateWhere( CriteriaBuilder builder
            , T entityObj, Class<T> clazz ){
		// 主键
		String idFieldName = CommDao.getIdFieldName(clazz);
		Object idFieldValue = ClazzUtil.getFieldValueByName(idFieldName, entityObj);
		
		// 遍历所有属性执行set
		CriteriaUpdate<T> update = (CriteriaUpdate<T>)builder.createCriteriaUpdate(clazz);
		Root<T> root = update.from(clazz);
		Field[] fields = entityObj.getClass().getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			boolean fieldHasAnno = field.isAnnotationPresent(Transient.class);
			if(fieldHasAnno){
				continue;
			}
			String fieldName = field.getName();
			if(idFieldName.equals(fieldName)){
				//跳过主键字段
				continue;
			}
			
			Object fieldValue = null;
			try {
				fieldValue = field.get(entityObj);
			} catch (Exception e) {
				LoggerUtils.error.error("get field value error:",e);
			} 
			update.set(fieldName, fieldValue);
		}
		
		// 添加where字段
		update.where(builder.equal(root.get(idFieldName), idFieldValue)
				);
		return update;
	}
}
