package com.jule.domino.dispacher.service;

import com.jule.domino.base.bean.ItemConfigBean;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.log.logobjs.impl.*;
import com.jule.domino.log.service.LogReasons;
import com.jule.domino.log.service.LogReasons.ILogReason;
import com.jule.domino.log.service.LogReasons.ReasonDesc;
import com.google.common.collect.Maps;
import com.jule.domino.log.logobjs.AbstractPlayerLog;
import com.jule.domino.log.servlet.ILogAPI;
import com.jule.domino.log.utils.MyLog;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.core.UriBuilder;
import java.lang.reflect.Field;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class LogService {
	
	/** 单例对象 */
	public static final LogService OBJ = new LogService();

	/**
	 * 线程数
	 */
	private static  final  int THREAD_NUM = 10;

	/** 游戏服务器ID */
	private int serverID;

	/** log日志服务器resetful 接口地址 */
	private String logServerUrl;

	/** 多线程线程池 */
	private static volatile ExecutorService _exec = null;

	private static URI uri = null;

	ResteasyClient client = null;

	// 设置代理
	ILogAPI api = null;

	private static final DateFormat ymdhmsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * 类默认构造器
	 * 
	 */
	private LogService() {
	}

	/**
	 * 使用前初始化服务
	 * @param logServerUrl	日志服地址
	 * @return
	 */
	public LogService init( String logServerUrl){
		return putLogServerUrl(logServerUrl)
				.buildThread();
	}

	/**
	 * 设置日志服务器地址
	 * @param value
	 * @return
	 */
	public LogService putLogServerUrl( String value) {
		this.logServerUrl = value;
		//设置日志服务器地址
		uri = UriBuilder.fromUri(logServerUrl).build();
		return this;
	}
	
	public LogService buildThread(){
		//构建线程池
		if (_exec == null) {
			synchronized (LogService.class) {
				if (_exec == null) {
					_exec = Executors.newFixedThreadPool(THREAD_NUM);
				}
			}
		}
		return this;
	}
	
	/**
	 * 发送日志
	 * @param user
	 * @param log
	 */
	public void sendLog(User user, AbstractPlayerLog log, ILogReason logReason, String params){
		buildLogObj(user,log,logReason,params);
		sendLogs(log, serverID);
	}
	
	private void sendLogs(final AbstractPlayerLog logObj, final int serverID){
		if (StringUtils.isEmpty(logServerUrl)) {
			//日志服务器地址为空
			//直接退出
			return ;
		}

		if (logObj.getLogTime() == null || logObj.getLogTime() <= 0) {
			logObj.setLogTime(System.currentTimeMillis()) ;
		}

		_exec.execute(new Runnable() {
			@Override
			public void run() {
				// 定义 HTTP 客户端
				if (client == null || client.isClosed()) {
					// 如果已经关闭，重新打开一个连接
					client = (new ResteasyClientBuilder()).connectionTTL(20, TimeUnit.SECONDS).build();
					// 构建 URL 目标
					ResteasyWebTarget target = client.target(uri);

					api = target.proxy(ILogAPI.class);
				}

				try {
					//获取json字符串
					ObjectMapper _mapper = new ObjectMapper();
					String jsonText = _mapper.writeValueAsString(logObj);

					//获取类名
					String clazzName = logObj.getClass().getName();

					//发送日志
					api.sendLog(serverID, clazzName, jsonText);

				} catch (Exception e) {
					MyLog.OBJ.error("send log error : " + e);
				}
			}
		});
	}

	private static Map<String, String> logResonsMap = Maps.newConcurrentMap();
	
	public <T extends AbstractPlayerLog> T buildLogObj( User user, T log, ILogReason logReason, String params){
		if (log == null ) {
			// 如果参数对象为空,
			// 则直接退出!
			return log;
		}

		if (user != null){
			log.setOpenId(user.getDevice_num());
			log.setCharId(user.getId());
			log.setCharName(user.getNick_name());
			log.setPlatform(user.getChannel_id());
			log.setOs(String.valueOf(user.getPlatform()));
			log.setDownPlatform(user.getDown_platform());
			log.setIp(user.getUser_ip());
			log.setDevice(user.getMei_code());
		}
		//log.setLevel(user.get);
		//log.setVipLevel();
		log.setParam(params);
		log.setLogTime(System.currentTimeMillis());

		do {
			if (logReason == null) {
				break;
			}

			String logKey = logReason.toString();
			if (logResonsMap.containsKey(logKey)) {
				log.setReason(logResonsMap.get(logKey));
				break;
			}

			Field[] fields = logReason.getClass().getDeclaredFields();
			if (fields == null) {
				break;
			}
			for (Field f : fields) {
				ReasonDesc meta = f.getAnnotation(ReasonDesc.class);
				if (meta != null) {
					logResonsMap.put(f.getName(), meta.value());
				}
			}
			log.setReason(logResonsMap.get(logKey));
		} while (false);

		return log;
	}


	/**
	 * 筹码日志
	 * @param user	玩家
	 * @param org	变化前
	 * @param cur	当前
	 * @param change	变化量
	 * @param reasons	原因
	 */
	public void sendMoneyLog(User user,double org,double cur, double change,LogReasons.CommonLogReason reasons){
		if (user == null ){
			return;
		}
		Game_PlayerChipsLog log = new Game_PlayerChipsLog();
		log.setOrg_chips(org);
		log.setCur_chips(cur);
		log.setChange(change);
		sendLog(user,log, reasons,null);
	}

	/**
	 * 用户登出日志
	 * @param user
	 */
	public void sendUserLogoutLog(User user){
		if (user == null){
			return;
		}
		long now = System.currentTimeMillis();
		long lastlogin = user.getLast_login().getTime();
		Auth_PlayerlogoutLog log = new Auth_PlayerlogoutLog();
		log.setLogoutTime(now);
		log.setOnlineTime((now - lastlogin)/1000);
		sendLog(user,log, LogReasons.CommonLogReason.LOGOUT,null);
	}

	/**
	 * User表更新操作记录
	 * @param user
	 */
	public void sendUserUpdateLog(User user){
		if (user == null){
			return;
		}
		Game_UserUpdateLog log = new Game_UserUpdateLog();
		log.setOper(String.valueOf(user.getMoney()));
		sendLog(user,log, LogReasons.CommonLogReason.USER_UPDATE,null);
	}

	/**
	 * 换头像记录
	 * @param user
	 */
	public void sendIcoChangeLog(User user,String orgIco,String curIco){
		if (user == null){
			return;
		}
		Game_IcoChangeLog log = new Game_IcoChangeLog();
		log.setCurIco(curIco);
		log.setOrgIco(orgIco);
		sendLog(user,log, LogReasons.CommonLogReason.CHANGE_ICO,null);
	}

	/**
	 * 重连记录
	 * @param user
	 */
	public void sendReconnectLog(User user,long offTimes){
		if (user == null){
			return;
		}
		Game_ReconnectLog log = new Game_ReconnectLog();
		log.setOffTime(offTimes);
		sendLog(user,log, LogReasons.CommonLogReason.RECONNECT,null);
	}

	/**
	 * 在线人数统计
	 */
	public void sendOnlineNumber(String server,int playerNumber){
		Game_OnlineNumberLog log = new Game_OnlineNumberLog();
		log.setCurrTime(new Date().getTime());
		log.setPosition("dispacher");
		log.setServer(server);
		log.setPlayerNumber(playerNumber);
		sendLog(null,log, LogReasons.CommonLogReason.ONLINENUMBER,null);
	}

	/**
	 * 获得物品日志
	 */
	public void sendItemLog(User user, int num, ItemConfigBean item, LogReasons.CommonLogReason reason){
		Game_ItemLog log = new Game_ItemLog();
		log.setItemId(item.getId());
		log.setItem_name(item.getItemName());
		log.setItem_type(item.getItemType());
		log.setNum(num);
		sendLog(user,log, reason,null);
	}
}
