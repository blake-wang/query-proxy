package com.ijunhai.dao;

import com.ijunhai.util.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Properties;

import static com.ijunhai.dao.DaoConstants.*;


public class KylinDao {
    private static final Logger logger = LoggerFactory.getLogger(KylinDao.class);

    private static class LazyHolder {
        private static final KylinDao INSTANCE = new KylinDao();
    }

    private Properties info = new Properties();
    private String kylinUrl;
    private Driver driverManager;

    public KylinDao() {
        try {
            driverManager = (Driver) Class.forName("org.apache.kylin.jdbc.Driver").newInstance();
            info.put("user", PropertiesUtils.get(KYLIN_USERNAME));
            info.put("password", PropertiesUtils.get(KYLIN_PASSWORD));
            kylinUrl = PropertiesUtils.get(KYLIN_URL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public static KylinDao getInstance() {
        return LazyHolder.INSTANCE;
    }


    private void free(Connection conn) {
        if (conn != null) try {
            conn.close();
        } catch (SQLException ignored) {
        }
    }


    public ResultSet execQuery(String sql) throws Exception {
        Connection connection = null;
        ResultSet resultSet;
        try {
            connection = driverManager.connect(kylinUrl, info);
            Statement state = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            resultSet = state.executeQuery(sql);
        } finally {
            free(connection);
        }
        return resultSet;
    }

}
