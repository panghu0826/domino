package com.jule.domino.log.servlet;

import com.jule.domino.log.db.CommonDAO;
import com.jule.domino.log.logobjs.AbstractLog;
import com.jule.domino.log.utils.MyLog;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogServlet implements ILogAPI{

	/** 对象映射 */
	private static final ObjectMapper _mapper = new ObjectMapper();
	/** 多线程线程池 */
	private static volatile ExecutorService _exec = null;

	private static Executor getExecutor() {
		if (_exec == null) {
			synchronized (LogServlet.class) {
				_exec = Executors.newSingleThreadExecutor();
			}
		}
		return _exec;
	}

	private static void insertLog(final String clazzName, final String jsonText){
		if (StringUtils.isEmpty(clazzName) || StringUtils.isEmpty(jsonText)) {
			return ;
		}
		//异步保存日志
		getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try {
					//获取类的对象
					Class<?> clazz = Class.forName(clazzName);
					// 将 JSON 文本反序列化为日志对象
					Object logObj = _mapper.readValue(jsonText, clazz);

					if (!(logObj instanceof AbstractLog)) {
						// 如果对象不是日志类,
						// 则直接退出!
						return;
					}

					// 记录日志信息
					MyLog.OBJ.info("addqueue " + clazz.getSimpleName()+ "jsonText = {}", jsonText);
					// 插入日志对象
					CommonDAO.OBJ.insert((AbstractLog) logObj);
				} catch (Exception ex) {
					// 记录异常日志
					MyLog.OBJ.error(ex.getMessage(), ex);
				}
			}
		});
	}

	@Override
	public int sendLog(int serverID, String clazzName, String jsonText) {
		insertLog(clazzName, jsonText);
		clazzName = null;
		jsonText = null;
		return 0;
	}

	@Override
	public int submitAllLog() {
		return 0;
	}

}
