package com.jule.db.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.msgpack.annotation.Ignore;

import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;


/**
 * 数据库实体bean
 * @author ran
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class BaseDbBean 
{
	@Transient
	@Ignore
	protected static final Gson gson = new GsonBuilder().serializeNulls().create();
	
	/*服务器id,数据库分库分表id*/

	public abstract Object getId();
	public abstract void setId(long id);
	
	/**
	 * 读取DB数据后事件
	 */
	public void readDbAfterEvent()
	{
	}
	/**
	 * 写DB数据前事件
	 */
	public void writeDbBeforeEvent()
	{
	}
}
