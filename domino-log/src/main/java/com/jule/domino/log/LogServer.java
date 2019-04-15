package com.jule.domino.log;

import com.jule.core.utils.xml.LogConfigUtils;
import com.jule.domino.log.config.AppConfig;
import com.jule.domino.log.db.CommonDAO;
import com.jule.domino.log.db.MyBatisSQLMapGen;
import com.jule.domino.log.timer.CommitLogTask;
import com.jule.domino.log.utils.MyLog;
import com.jule.domino.log.servlet.LogServlet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 * 日志服启动程序
 *
 * @author ran
 * @since 2018年4月12日10:09:13
 */
public class LogServer {
	
	/** 服务器 IP 地址 */
	private String _serverIP = "192.168.0.14";
	/** 服务器端口 */
	private int _port = 8003;
	/** Netty */
	private NettyJaxrsServer _netty;
	
	public static LogServer theApp;

	/** 停服命令队列 */
	private final SynchronousQueue<Boolean> _stopQ = new SynchronousQueue<>();
	
	/**
	 * 默认构造
	 * @param serverIP
	 * @param port
	 */
	public LogServer (String serverIP , int port){
		if (StringUtils.isEmpty(serverIP)) {
			//如果服务器IP为空
			//直接抛出异常
			throw new IllegalArgumentException(" serverIP is null ");
		}
		this._serverIP = serverIP;
		this._port = port;
	}
	
	public static void main(String[] args) throws Exception {
		long t0 = System.currentTimeMillis();

		LogConfigUtils.initLogConfig();
		MyLog.OBJ.info("开始设置log4j配置");

		MyLog.OBJ.info("开始加载配置文件");
		AppConfig.loadProps(  "./config/configs.properties");
		MyLog.OBJ.info("bind -- > "+AppConfig.getServerIp()+":"+AppConfig.getServerPort());

		MyLog.OBJ.info("开始生成auto.xml");
		MyBatisSQLMapGen.OBJ.gen("./config/auto.xml");

		//测试一发
		CommonDAO.OBJ.sayHello();
		
		//设置sql提交条数
		CommonDAO.COMMIT_COUNT = AppConfig.getSqlCommit();
		
		LogServer app = null;
		
		//创建并启动服务器
		app = new LogServer(AppConfig.getServerIp(), AppConfig.getServerPort());
		app.start();
		
		LogServer.theApp = app;
		
		//写出监控文件
		theApp.writeOutAndMonitorProcFile();
		
		//启动定时器,定时插入日志数据
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(
				new CommitLogTask(),
				1000 * 60 * AppConfig.getCommitTime(), 
				1000 * 60 * AppConfig.getCommitTime());

		long t1 = System.currentTimeMillis();
		MyLog.OBJ.info("服务器启动时间 : " + (t1 - t0) + "毫秒");
		while (Boolean.TRUE.equals(theApp._stopQ.take())) {
			MyLog.OBJ.info("停服执行");
			theApp.stop();
		}
	}
	
	private static String getRootDir(String fileName){
		return LogServer.class.getClassLoader().getResource(fileName).getPath();
	}

	private static String getDir(String fileName){
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		String path = classLoader.getResource(fileName).getPath();
		MyLog.OBJ.info(path);
		return path;
	}
	
	public void start(){
		ResteasyDeployment deployment = new ResteasyDeployment();
		deployment.setSecurityEnabled(true);
		// 注册命令类
		this.registerCmdClazz(deployment);

		_netty = new NettyJaxrsServer();
		_netty.setDeployment(deployment);
		_netty.setPort(_port);
		_netty.setRootResourcePath(_serverIP);
		_netty.setSecurityDomain(null);
		_netty.setKeepAlive(true);
		_netty.start();
		_netty.getDeployment();
	}
	
	public void stop(){
		CommonDAO.OBJ.commitAllLogs();
		
		if (_netty == null) {
			return;
		}
		
		_netty.stop();
		
		System.exit(0);
	}
	
	/**
	 * 注册命令类
	 * 
	 * @param deployment
	 * 
	 */
	private void registerCmdClazz(ResteasyDeployment deployment) {
		if (deployment == null) {
			return;
		}

		// 创建注册类列表
		List<String> resList = Arrays.asList(
				LogServlet.class.getName()
				);
		deployment.setResourceClasses(resList);
	}
	
	private void writeOutAndMonitorProcFile(){
		// 获取用户目录并创建 proc 文件
		final File D_user = SystemUtils.getUserDir();
		final File F_proc = new File(D_user, "proc");

		try {
			// 写出 proc 文件
			MyLog.OBJ.info("写出并监控文件 " + F_proc.getAbsolutePath());
			FileUtils.writeStringToFile(F_proc, "1");
		} catch (Exception ex) {
			// 输出异常信息并推出
			MyLog.OBJ.error(ex.getMessage(), ex);
			System.exit(-1);
		}

		// 创建定时服务并执行
		final ScheduledExecutorService es = Executors.newScheduledThreadPool(1);
		es.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				if (!F_proc.exists()) {
					MyLog.OBJ.info("文件 " + F_proc.getAbsolutePath() + " 被删除，准备停服");
					theApp._stopQ.offer(true);
					es.shutdown();
				}
			}
		}, 2, 2, TimeUnit.SECONDS);
	}

}
