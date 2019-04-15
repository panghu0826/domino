package com.jule.domino.dispacher.service;

import com.jule.core.jedis.StoredObjManager;
import com.jule.core.model.ServerState;
import com.jule.domino.base.dao.bean.User;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.dispacher.dao.DBUtil;
import com.jule.domino.dispacher.dao.bean.BlackWhiteModel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * 登录验证服务
 *
 * @author ran
 * @since 2018年5月17日17:57:19
 *
 */
@Slf4j
public class VilidateService {

	/** 单例对象 */
	public static final VilidateService OBJ = new VilidateService();

	private static Map<String ,BlackWhiteModel> _blackMap = new ConcurrentHashMap<>();

	public VilidateService() {
	}
	@Setter@Getter
	private ServerState server;

	public void init(){
		//30分钟定时刷新内存
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(()->loadFromDb(),0,60 * 30, TimeUnit.SECONDS);
	}

	private void loadFromDb(){
		log.debug("定时读取黑白名单");
		//清空内存
		_blackMap.clear();

		//查询全部黑白名单、放到内存
		List<BlackWhiteModel> list = DBUtil.selectAllBlack();
		if (list == null || list.size() == 0){
			log.error("黑白名单表为空");
			return;
		}

		list.forEach(e->_blackMap.put(e.getUid(), e));
	}

	/**
	 * 服务器是否关闭、开始停服更新
	 * @return true - 已关闭
	 *         false- 未关闭
	 */
	public boolean isShutDown(){
		server = StoredObjManager.get(ServerState.class, GameConst.CACHE_SERVER_STATE);
		if (server == null){
			return false;
		}

		//验证当前时间是否在维护期间
		long now = System.currentTimeMillis();
		if (server.getEndTime() > now
				&& server.getStartTime() < now){
			return true;
		}
		return false;
	}

	/**
	 * 是否白名单
	 * @param user
	 * @return		true-白名单用户
	 */
	public boolean isWhiteList(User user){
		if (user == null){
			return false;
		}

		BlackWhiteModel bean = _blackMap.get(user.getId());
		if (bean == null){
			return false;
		}

		if (bean.getWhite() == 1){
			return true;
		}
		return false;
	}

	/**
	 * 是否黑名单
	 * @param user
	 * @return		true-黑名单用户
	 */
	public boolean isBlackList(User user){
		if (user == null){
			return false;
		}

		BlackWhiteModel bean = _blackMap.get(user.getId());
		if (bean == null){
			return false;
		}

		if (bean.getBlack() == 1){
			return true;
		}
		return false;
	}

	/**
	 * 版本更新
	 * @param clientVersion
	 * @return
	 */
	public int versionUpdate(String clientVersion,String downPlatform){
		boolean reslut = VersionService.OBJ.judageVersion(clientVersion,downPlatform);
		if(reslut){
			return 1;
		}
		return 0;
	}

	/**
	 * 版本更新
	 * @param downPlatform
	 * @return
	 */
	public String getDownPlatform(String downPlatform){
		return  VersionService.OBJ.getDownPlatform(downPlatform);
	}

}
