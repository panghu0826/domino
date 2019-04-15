package com.jule.robot.service;

import com.google.common.collect.Maps;
import com.jule.db.entities.User;
import com.jule.domino.log.logobjs.AbstractPlayerLog;
import com.jule.domino.log.logobjs.impl.Robot_LedgerLog;
import com.jule.domino.log.service.LogReasons;
import com.jule.domino.log.servlet.ILogAPI;
import com.jule.domino.log.utils.MyLog;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.core.UriBuilder;
import java.lang.reflect.Field;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class LogService {

	private final static Logger logger = LoggerFactory.getLogger(LogService.class);

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
	public void sendLog(User user, AbstractPlayerLog log, LogReasons.ILogReason logReason, String params){
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
					//api.sendLog(serverID, clazzName, jsonText);
					logger.info("send robot log clazzName={},jsonText={}", clazzName, jsonText);

				} catch (Exception e) {
					MyLog.OBJ.error("send log error : " + e);
				}
			}
		});
	}

	private static Map<String, String> logResonsMap = Maps.newConcurrentMap();
	
	public <T extends AbstractPlayerLog> T buildLogObj(User user, T log, LogReasons.ILogReason logReason, String params){
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
				LogReasons.ReasonDesc meta = f.getAnnotation(LogReasons.ReasonDesc.class);
				if (meta != null) {
					logResonsMap.put(f.getName(), meta.value());
				}
			}
			log.setReason(logResonsMap.get(logKey));
		} while (false);

		return log;
	}

	/**
	 * User表更新操作记录
	 * @param user
	 */
	public void sendRobotLedgerLog(User user, String playType,double org_capitalPool, double cur_capitalPool, double changeChips, LogReasons.CommonLogReason clr){
		if (user == null){
			return;
		}
		Robot_LedgerLog log = new Robot_LedgerLog();
		log.setPlayType(playType);
		log.setOrg_capitalPool(String.valueOf(org_capitalPool));
		log.setCur_capitalPool(String.valueOf(cur_capitalPool));
		log.setChangeChips(String.valueOf(changeChips));
		sendLog(user,log, clr,null);
	}


}
