package com.ijunhai.dao;

import com.ijunhai.exception.Exceptions;
import com.ijunhai.util.PropertiesUtils;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.ijunhai.dao.DaoConstants.*;


public class MysqlDao {
    private static String dbUrl = PropertiesUtils.get(MYSQL_URL);
    private static String dbUser = PropertiesUtils.get(MYSQL_USER);
    private static String dbPass = PropertiesUtils.get(MYSQL_PASSWORD);

    private static class LazyHolder {
        private static final MysqlDao INSTANCE = new MysqlDao();
    }

    private final DataSource dataSource;

    private MysqlDao() {
        PoolProperties poolProps = new PoolProperties();
        poolProps.setDriverClassName("com.mysql.jdbc.Driver");
        poolProps.setUrl(dbUrl);
        poolProps.setUsername(dbUser);
        poolProps.setPassword(dbPass);
        poolProps.setTestOnBorrow(true);
        poolProps.setValidationQuery("select 1");
        dataSource = new DataSource(poolProps);
    }

    public static MysqlDao getInstance() {
        return LazyHolder.INSTANCE;
    }

    public ResultSet execQuery(String sql) throws Exception {
        Connection conn = null;
        ResultSet resultSet = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement state = conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            resultSet = state.executeQuery();
        }finally {
            if (conn != null) {
                conn.close();
            }
        }
        return resultSet;
    }

}
