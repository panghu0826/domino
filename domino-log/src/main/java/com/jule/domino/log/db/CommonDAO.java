package com.jule.domino.log.db;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.jule.domino.log.config.AppConfig;
import com.jule.domino.log.logobjs.AbstractLog;
import com.jule.domino.log.utils.MyLog;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;


public class CommonDAO {

	private static ConcurrentHashMap<String, BlockingQueue<AbstractLog>> logMaps = new ConcurrentHashMap<String, BlockingQueue<AbstractLog>>();

	private static HashMap<String, String> nameMap = new HashMap<>();
	/** 已创建的表 */
	private Map<String, Boolean> _createdTableMap = new ConcurrentHashMap<>();
	
	public static int COMMIT_COUNT = 512;
	/** 单例对象 */
	public static final CommonDAO OBJ = new CommonDAO(AppConfig.getProps());

	/** 创建数据表 */
	private static final String SQL_createTable = "createTable_{0}";

	/** 插入数据List */
	private static final String SQL_insertIntoList = "insertInto_{0}List";

	/** 数据库连接 */
	private SqlMapClient _sqlMapClient;

	/**
	 * 类默认构造器
	 * 
	 * @param props
	 * 
	 */
	private CommonDAO( Properties props) {
		// 创建数据库连接
		this._sqlMapClient = create(props);
	}

	/**
	 * 创建数据库连接
	 * 
	 * @param props
	 * @return
	 * @throws 
	 */
	public static SqlMapClient create(Properties props) {
		if (props == null) {
			throw new IllegalArgumentException("props is null");
		}

		try {
			MyLog.OBJ.info("开始加载config/ibatis.cfg.xml");
			// 获取 ibatis 配置文件
			InputStream _is = new BufferedInputStream(new FileInputStream("./config/ibatis.cfg.xml"));
			//InputStream _is = CommonDAO.class.getClassLoader().getResourceAsStream("ibatis.cfg.xml");
			InputStream ins = new BufferedInputStream(_is);

			// 创建文本输入流
			InputStreamReader isr = new InputStreamReader(ins, Charset.forName("UTF8"));
			return SqlMapClientBuilder.buildSqlMapClient(isr, props);
		} catch (Exception e) {
			MyLog.OBJ.error(e.getMessage());
			throw new IllegalArgumentException("create sqlmap error : "+e) ;
		}
	}

	/**
	 * 启动测试
	 */
	public void sayHello(){
		MyLog.OBJ.info("hello , I'm ok!!!");
	}

	/**
	 * 日志写入服务
	 * 
	 * @param logObj
	 */
	public void insert(AbstractLog logObj) {
		if (logObj == null) {
			return ;
		}
		try {
			//获取类名
			String clazzName = logObj.getClass().getSimpleName();

			//数据库名
			String table = logObj.getSplitTableName();

			Boolean tableCreated = this._createdTableMap.get(table);
			if (tableCreated == null || !tableCreated) {
				createTable(clazzName, table);
				if (nameMap.get(clazzName) != null) {
					//如果跨天了、提交到昨天
					insert(clazzName, table);
				}
			}

			nameMap.put(clazzName, table);
			if (logMaps.get(clazzName) == null) {
				BlockingQueue<AbstractLog> queue = new LinkedBlockingDeque<>(COMMIT_COUNT * 2 + 100);
				logMaps.put(clazzName, queue);
			}

			BlockingQueue<AbstractLog> queue = logMaps.get(clazzName);
			queue.put(logObj);
			if (queue.size() < COMMIT_COUNT - 1) {
				return;
			}
			insert(clazzName, table);
		} catch (Exception e) {
			MyLog.OBJ.error("insert log error : "+e);
		}
	}
	
	/**
	 * 提交全部日志
	 */
	public void commitAllLogs() {
		MyLog.OBJ.info("开始定时提交全部记录 !!!");
        for (Entry<String, BlockingQueue<AbstractLog>> queue : logMaps.entrySet()) {
            String tableName = nameMap.get(queue.getKey());
            if (tableName == null) {
                continue;
            }
            insert(queue.getKey(), tableName);
        }
    }

	/**
	 * 插入某一类型的log
	 * @param clazzName
	 * @param table
	 */
	private void insert(String clazzName ,String table){
		try {
			insertOnebyIbaitsList(clazzName, table);
		} catch (Exception e) {
			MyLog.OBJ.info("insert error :" + e.getMessage());
		}
	}

	private void insertOnebyIbaitsList(String clazzName, String table) 
			throws SQLException, InterruptedException{
		//插入语句
		String insertInto = MessageFormat.format(SQL_insertIntoList, clazzName);

		//是否已经建表?
		Boolean tableCreated = this._createdTableMap.get(table);

		if (tableCreated == null || !tableCreated) {
			//建表语句
			String createTable = MessageFormat.format(SQL_createTable, clazzName);
			//事先建表
			this._sqlMapClient.update(createTable, table);
			//设置为已创建
			this._createdTableMap.put(table, true);
			//发送日志记录
			MyLog.OBJ.info(createTable +" => "+table);
		}

		//开始批处理日志
		this._sqlMapClient.startBatch();

		BlockingQueue<AbstractLog> queue = logMaps.get(clazzName);
		List<AbstractLog> list = new ArrayList<>();
		HashMap<String, Object> map = new HashMap<>();

		while (!queue.isEmpty()) {
			AbstractLog log = queue.take();
			if (log != null) {
				list.add(log);
			}
		}
		
		if (list.isEmpty()) {
			MyLog.OBJ.info("暂无可提交记录!!!");
			return;
		}

		map.put("splitTableName", table);
		map.put("values", list);
		this._sqlMapClient.insert(insertInto, map);
		
		// 记录日志信息
        MyLog.OBJ.info("insert " + clazzName);
        
        // 执行批处理
        this._sqlMapClient.executeBatch();
        
        try {
            // 执行批处理
            this._sqlMapClient.executeBatch();

        } catch (BatchUpdateException e) {
            MyLog.OBJ.error("Error Sql:", e.getSQLState());
            MyLog.OBJ.error("Insert Count:" + e.getUpdateCounts());
            int[] counts = e.getUpdateCounts();
            int index=0;
            for(int update:counts){
                if(update>0){
                    index++;
                }
            }
            
            int size=list.size();
            for(int i=index+1;i<size;i++){
                queue.add(list.get(i));
            }
        }
	}

	private void createTable(String clazzName ,String table) throws SQLException{
		// 是否已经创建表 ?
        Boolean tableCreated = this._createdTableMap.get(table);

        if (tableCreated == null || !tableCreated) {
            // 建表语句 ...
            String createTable = MessageFormat.format(SQL_createTable, clazzName);

            // 事先创建表
            this._sqlMapClient.update(createTable, table);

            // 设置为已创建表
            this._createdTableMap.put(table, true);
            // 记录数据库插入日志
            MyLog.OBJ.info(createTable + " => " + table);
        }
	}


}
