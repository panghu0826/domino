package com.jule.db.dao;

import com.jule.core.database.DatabaseConfig;
import com.jule.core.common.log.LoggerUtils;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.JNDIConnector;
import org.eclipse.persistence.sessions.Session;


/**
 * 数据库连接池
 *
 * @author ran
 */
public class C3p0SessionCustomizer implements SessionCustomizer {

	@Override
	public void customize(Session session) throws Exception {
		DatabaseLogin databaseLogin = session.getLogin();

		String jdbcDriver = databaseLogin.getDriverClassName();
		String jdbcUrl = databaseLogin.getDatabaseURL();
		String username = databaseLogin.getUserName();
		String password = DatabaseConfig.DATABASE_PASSWORD;

		ComboPooledDataSource dataSource = buildDataSource(jdbcDriver, jdbcUrl,
				username, password);
		databaseLogin.setConnector(new JNDIConnector(dataSource));
	}

	private ComboPooledDataSource buildDataSource(String jdbcDriver, String jdbcUrl,
			String username, String password) {
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		try {
			// Loads the JDBC driver
			dataSource.setDriverClass(jdbcDriver);
			dataSource.setJdbcUrl(jdbcUrl);
			dataSource.setUser(username);
			dataSource.setPassword(password);
	
			dataSource.setAcquireRetryAttempts(1);
			dataSource.setMaxStatements(0);
			dataSource.setCheckoutTimeout(2000);
			dataSource.setIdleConnectionTestPeriod(30);
			dataSource.setMaxIdleTime(1800);
			dataSource.setInitialPoolSize(10);
			dataSource.setMinPoolSize(3);
			dataSource.setMaxPoolSize(128);
			dataSource.setBreakAfterAcquireFailure(true);
		} catch (Exception e) {
			LoggerUtils.daoLogger.error("dataSource error:",e);
		}
		return dataSource;
	}
}
