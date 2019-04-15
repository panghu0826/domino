package com.jule.db.dao;

import com.jule.core.common.log.LoggerUtils;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * 执行本地查询
 * @author Thinker
 */
interface IDaoExecNativeSQL
{
	/**
	 * 执行本地查询
	 * @param nativeSQL
	 * @param paramsMap 
	 * @return
	 * 
	 */
	default List<?> queryNativeSQL( String nativeSQL, Map<String, Object> paramsMap )
	{
		//如果 SQL语句为空,则直接退出!
		if(nativeSQL==null||nativeSQL.isEmpty())
		{
			return null;
		}
		checkSqlShardName(nativeSQL);

		//获取实体管理器
		EntityManager em = CommDao.OBJ.getEntityManager();
		
		//如果实体管理器为空,则直接退出!
		if (em == null) 
		{
			return null;
		}
		
		List<?> value = null;
		EntityTransaction tranx = null;
		try 
		{
			tranx = em.getTransaction();
			//开始事务过程
			if(!tranx.isActive())
			{
				tranx.begin();
			}
			//创建本地查询
			Query q = em.createNativeQuery(nativeSQL);

			if (paramsMap != null && 
				paramsMap.isEmpty() == false) 
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
			value = q.getResultList();
			tranx.commit();
		} catch (Exception ex) 
		{
			LoggerUtils.error.error(ex.getMessage(), ex);
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

		return value;
	}
	
	/**
	 * 执行本地查询
	 * @param nativeSQL
	 * @param paramsMap 
	 * @return
	 * 
	 */
	default int execNativeSQL( String nativeSQL, Map<String, Object> paramsMap )
	{
		//如果 SQL语句为空,则直接退出!
		if(nativeSQL==null||nativeSQL.isEmpty())
		{
			return -1;
		}
		checkSqlShardName(nativeSQL);

		//获取实体管理器
		EntityManager em = CommDao.OBJ.getEntityManager();
		
		//如果实体管理器为空,则直接退出!
		if (em == null) 
		{
			return -1;
		}
		
		Object value = null;
		EntityTransaction tranx = null;
		try 
		{
			tranx = em.getTransaction();
			//开始事务过程
			if(!tranx.isActive())
			{
				tranx.begin();
			}
			//创建本地查询
			Query q = em.createNativeQuery(nativeSQL);

			if (paramsMap != null && 
				paramsMap.isEmpty() == false) 
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
			value = q.getSingleResult();
			tranx.commit();
		} catch (Exception ex) 
		{
			LoggerUtils.error.error(ex.getMessage(), ex);
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

		if(value==null) return -1;
		return Integer.parseInt(String.valueOf(value));
	}
	
	/**
	 * 执行本地查询
	 * @param nativeSQL
	 * @param paramsMap 
	 * @return
	 * 
	 */
	default int executeUpdate( String nativeSQL, Map<String, Object> paramsMap )
	{
		//如果 SQL 语句为空,则直接退出!
		if (nativeSQL == null || nativeSQL.isEmpty()) 
		{
			return -1;
		}
		checkSqlShardName(nativeSQL);

		//获取实体管理器
		EntityManager em = CommDao.OBJ.getEntityManager();
		if(em == null)
		{
			return -1;
		}
		
		int ret = 0;
		EntityTransaction tranx = null;
		try 
		{
			tranx = em.getTransaction();
			//开始事务过程
			if(!tranx.isActive())
			{
				tranx.begin();
			}
			//创建本地查询
			Query q = em.createQuery(nativeSQL);

			if (paramsMap != null && 
				paramsMap.isEmpty() == false) 
			{
				paramsMap.entrySet().forEach(entry -> 
				{
					//如果进入点为空,则直接退出!
					if (entry == null)
					{
						return;
					}

					//设置参数
					q.setParameter(entry.getKey(), entry.getValue());
				});
			}
			ret = q.executeUpdate(); 
			tranx.commit();
		} catch (Exception ex) 
		{
			//记录错误日志
			LoggerUtils.error.error(ex.getMessage(), ex);
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
		return ret;
	}
	
	/**
	 * 执行本地查询
	 * @param nativeSQL
	 * @param paramsMap 
	 * @return
	 * 
	 */
	default int executeNativeUpdate( String nativeSQL, Map<String, Object> paramsMap )
	{
		//如果 SQL 语句为空,则直接退出!
		if (nativeSQL == null || nativeSQL.isEmpty()) 
		{
			return -1;
		}
		checkSqlShardName(nativeSQL);

		//获取实体管理器
		EntityManager em = CommDao.OBJ.getEntityManager();
		if(em == null)
		{
			return -1;
		}
		
		int ret = 0;
		EntityTransaction tranx = null;
		try 
		{
			tranx = em.getTransaction();
			//开始事务过程
			if(!tranx.isActive())
			{
				tranx.begin();
			}
			//创建本地查询
			Query q = em.createNativeQuery(nativeSQL);

			if (paramsMap != null && 
				paramsMap.isEmpty() == false) 
			{
				paramsMap.entrySet().forEach(entry -> 
				{
					//如果进入点为空,则直接退出!
					if (entry == null)
					{
						return;
					}

					//设置参数
					q.setParameter(entry.getKey(), entry.getValue());
				});
			}
			ret = q.executeUpdate(); 
			tranx.commit();
		} catch (Exception ex) 
		{
			//记录错误日志
			LoggerUtils.error.error(ex.getMessage(), ex);
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
		return ret;
	}
	
	/**
	 * 执行本地查询jpaSql
	 * 注意：只能执行查询select语句
	 * 
	 * @param nativeSQL
	 * @param paramsMap 
	 * @return
	 * 
	 */
	default<TEntity> Object execJpaSQL( String jpaSQL, Class<TEntity> clazz )
	{
		//如果 SQL语句为空,则直接退出!
		if(jpaSQL == null || jpaSQL.isEmpty()) 
		{
			return -1;
		}
		checkSqlShardName(jpaSQL);

		//获取实体管理器
		EntityManager em = CommDao.OBJ.getEntityManager();
		//如果实体管理器为空,则直接退出!
		if(em == null) 
		{ 
			return -1;
		}

		final String jpql = MessageFormat.format(jpaSQL, clazz.getName());
		Object value = null;
		EntityTransaction tranx = null;
		try 
		{
			tranx = em.getTransaction();
			//开始事务过程
			if(!tranx.isActive())
			{
				tranx.begin();
			}
			//创建本地查询
			Query q = em.createQuery(jpql);
			q.setHint("eclipselink.read-only", "true");
			value = q.getSingleResult();
			tranx.commit();
		} catch (Exception ex) 
		{
			LoggerUtils.error.error(ex.getMessage(), ex);
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
		
		if(value==null) return -1;
		return value;
	}
	
	default boolean checkSqlShardName( String sql ){
		if(StringUtils.isEmpty(sql)){
			return false; 
		}
		return false;
	}
}
