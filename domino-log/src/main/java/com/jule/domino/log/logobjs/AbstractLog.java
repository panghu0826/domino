package com.jule.domino.log.logobjs;

import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日志基础类
 * 
 * @author Ron
 *
 */
@Entity
public abstract class AbstractLog {
	/** ID */
	private Long _id = null;
	/** 日志产生时间 */
	protected Long _logTime = null;

	/**
	 * 获取 ID
	 * 
	 * @return 
	 * 
	 */
	@Id
	@Column(columnDefinition = "bigint not null auto_increment")
	public final Long getId() {
		return this._id;
	}

	/**
	 * 设置 ID
	 * 
	 * @param value 
	 * 
	 */
	public final void setId(Long value) {
		this._id = value;
	}

	/**
	 * 获取日志产生时间
	 * 
	 * @return 
	 * 
	 */
	@Column
	public Long getLogTime() {
		if (this._logTime == null) {
			return 0L;
		} else {
			return this._logTime;
		}
	}

	/**
	 * 设置日志的产生时间
	 * 
	 * @param value
	 * 
	 */
	public void setLogTime(Long value) {
		this._logTime = value;
	}

	/**
	 * 获取分表名称
	 * 
	 * @return
	 * 
	 */
	@JsonIgnore
	public String getSplitTableName() {
		// 获取分表名称
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date date=new Date();
		date.setTime(this.getLogTime());
		String suffix = sdf.format(date);

		return this.getClass().getSimpleName() + "_" + suffix;
	}
	
}
