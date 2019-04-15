package com.jule.core.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import javax.sql.DataSource;
import java.io.InputStream;

@Slf4j
public class DatabaseFactory {

    private static SqlSessionFactory sqlSessionFactory;

    public static final DatabaseFactory getInstance() {
        return SingletonHolder.instance;
    }

    @SuppressWarnings("synthetic-access")
    private static class SingletonHolder {
        protected static final DatabaseFactory instance = new DatabaseFactory();
    }

    DatabaseFactory() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(DatabaseConfig.DATABASE_URL);
            config.setUsername(DatabaseConfig.DATABASE_USER);
            config.setPassword(DatabaseConfig.DATABASE_PASSWORD);
            config.setMinimumIdle(DatabaseConfig.DATABASE_CONNECTIONS_MIN);
            config.setMaximumPoolSize(DatabaseConfig.DATABASE_CONNECTIONS_MAX);
            config.setRegisterMbeans(true);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            DataSource dataSource = new HikariDataSource(config);
            TransactionFactory transactionFactory = new JdbcTransactionFactory();
            Environment environment = new Environment("work", transactionFactory, dataSource);
            InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, "work");
            sqlSessionFactory.getConfiguration().setEnvironment(environment);
            inputStream.close();
        } catch (Exception e) {
            log.error("DatabaseFactory 初始化失败 ", e);
        }
    }

    /**
     * 获取连接
     *
     * @return
     */
    public SqlSession getSqlSession() {
        return sqlSessionFactory.openSession();
    }
}
