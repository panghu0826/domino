package com.jule.domino.log.logobjs;

import javax.persistence.Column;


/**
 * 玩家日志
 * 
 * @author ran
 * @since 2016/7/12
 * 
 */
public class AbstractPlayerLog extends AbstractLog{
	/**腾讯传入id */
	protected String openid;//
	/** 角色ID */
	protected String charId;
	/** 玩家的角色名 */
	protected String charName;
	/** 平台 */
	protected String platform;
	/** 玩家等级 */
	protected int level;
	/** 玩家 VIP 等级 */
	protected int  vipLevel;
	/** 原因 */
	protected String reason;
	/** 其他参数 */
	protected String param;
	/**操作系统*/
	protected String os;
	/**下载渠道*/
	protected String downPlatform;
	/**ip*/
	protected String ip;
	/**用户设备*/
	protected String device;


	/**
	 * 默认构造
	 */
	public AbstractPlayerLog() {
	}

	/**
	 * 类默认构造器
	 * 
	 */


	@Column(length = 64)
	public String getOpenId() {
		if (this.openid == null) {
			return "";
		} else {
			return openid;
		}
	}

	@Column
	public String getCharId() {
		return charId;
	}

	@Column(length = 64)
	public String getCharName() {
		if (charName == null) {
			return "";
		} else {
			return charName.replaceAll(".*([';]+|(--)+).*", "");
		}
	}
	@Column
	public int getLevel() {
		return level;
	}
	@Column
	public int getVipLevel() {
		return vipLevel;
	}

	@Column(columnDefinition = "text default null")
	public String getReason() {
		if (this.reason == null) {
			return "";
		} else {
			return reason;
		}
	}

	@Column(columnDefinition = "text default null")
	public String getParam() {
		if (this.param == null) {
			return "";
		} else {
			return param;
		}
	}
	@Column(length = 64)
	public String getPlatform() {
		return platform;
	}

	@Column(length = 64)
	public String getOs() {
		return os;
	}
	@Column(length = 64)
	public String getDownPlatform() {
		return downPlatform;
	}
	@Column(length = 64)
	public String getIp() {
		return ip;
	}
	@Column(length = 256)
	public String getDevice() {
		return device;
	}

	public void setPlatform( String platform ) {
		this.platform = platform;
	}

	public void setOpenId( String openid) {
		this.openid = openid;
	}

	public void setCharId( String charId ) {
		this.charId = charId;
	}

	public void setCharName( String charName ) {
		this.charName = charName;
	}

	public void setLevel( int level ) {
		this.level = level;
	}

	public void setVipLevel( int vipLevel ) {
		this.vipLevel = vipLevel;
	}

	public void setReason( String reason ) {
		this.reason = reason;
	}

	public void setParam( String param ) {
		this.param = param;
	}

	public void setOs( String os ) {
		this.os = os;
	}

	public void setDownPlatform( String downPlatform ) {
		this.downPlatform = downPlatform;
	}

	public void setIp( String ip ) {
		this.ip = ip;
	}

	public void setDevice( String device ) {
		this.device = device;
	}
}