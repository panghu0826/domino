package com.jule.core.configuration;

import com.jule.core.utils.GsonUtil;
import com.jule.core.utils.MD5Security;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GameConfig {
	
	private Map<String, String> tableClassNameMap = new HashMap<>();
	private static GameConfig instant;

	private static String md5val = "";

	private static Thread watcher;

	public static void init() {

		String conf = readFile();

		md5val = MD5Security.compute(conf);

		instant = load(conf);

		watcher = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(60 * 1000);
					} catch (InterruptedException e) {
					}

					// check the config file
					String conf = readFile();
					String _md5 = MD5Security.compute(conf);
					if (_md5 != md5val) {
						synchronized (instant) {
							instant = load(conf);
							md5val = _md5;
						}

					}
				}
			}
		}, "watch_GameConfig");

		watcher.start();

	}

	public static String readFile() {
		String configJson = null;
		FileInputStream in = null;
		try {
			in = new FileInputStream("./config/GameConfig.json");
			byte[] data = new byte[in.available()];
			in.read(data);
			configJson = new String(data, "utf-8");
			in.close();
		} catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return configJson;
	}

	public static GameConfig load(String configJson) {
		Object obj = GsonUtil.fromJson(configJson, GameConfig.class);
		return (GameConfig) obj;
	}

	/*****
	 * 取得配置表中的 玩法逻辑处理类名
	 * 
	 * @param playType
	 * @return
	 */
	public static String getGameTableName(String playType) {
		if (instant.tableClassNameMap == null)
			return null;
		return instant.tableClassNameMap.get(playType);
	}
}
