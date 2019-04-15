package com.jule.domino.dispacher.service;

import com.jule.core.jedis.StoredObjManager;
import com.jule.domino.base.enums.GameConst;
import com.jule.domino.dispacher.dao.DBUtil;
import com.jule.domino.dispacher.dao.bean.AdConfigModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 日志配置服务
 *
 * @author ran
 * @since 2018年7月10日16:52:44
 *
 */
public class AdConfigService {

	/** 单例对象 */
	public static final AdConfigService OBJ = new AdConfigService();

	private final static Logger logger = LoggerFactory.getLogger(AdConfigService.class);

	/**默认最大次数*/
	private static final int DEFUALT_TIMES = 5;
	/**默认最大筹码*/
	private static final int DEFUALT_MAX_CHIPS = 1000;


	public AdConfigService() {
	}

	public void init(){
		logger.info("开始加载广告配置");
		AdConfigModel config  = DBUtil.loadAdConfig();
		if (config == null){
			logger.error("广告配置为空 AdConfigModel = null");
			return;
		}

		StoredObjManager.set(GameConst.CACHE_AD_CONFIGS, config);
		logger.info("加载广告配置成功");
	}

	/**
	 * 获取每日次数上限
	 * @return
	 */
	public int getMaxTimes(){
		AdConfigModel config = StoredObjManager.get(AdConfigModel.class, GameConst.CACHE_AD_CONFIGS);
		if (config == null){
			//重新加载
			this.init();
			//重新查询
			config = StoredObjManager.get(AdConfigModel.class, GameConst.CACHE_AD_CONFIGS);
		}

		if (config == null){
			return DEFUALT_TIMES;
		}
		return config.getFrequency();
	}

	public int getMaxChips(){
		AdConfigModel config = StoredObjManager.get(AdConfigModel.class, GameConst.CACHE_AD_CONFIGS);
		if (config == null){
			//重新加载
			this.init();
			//重新查询
			config = StoredObjManager.get(AdConfigModel.class, GameConst.CACHE_AD_CONFIGS);
		}

		if (config == null){
			return DEFUALT_MAX_CHIPS;
		}
		return config.getChipNumber();
	}



}
