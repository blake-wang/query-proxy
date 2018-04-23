package com.ijunhai.util;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ijunhai.contants.ProxyConstants.*;

public class JDBCPool {
    private ExecutorService exec = Executors.newFixedThreadPool(25);
    private static String dbUrl = PropertiesUtils.get(MYSQL_URL);
    private static String dbUser = PropertiesUtils.get(MYSQL_USER);
    private static String dbPass = PropertiesUtils.get(MYSQL_PASSWORD);
    private static JDBCPool JDBCPool;
    private DataSource dataSource = new DataSource();
    private Connection conn;

    private JDBCPool() {
        PoolProperties poolProps = new PoolProperties();
        poolProps.setDriverClassName("com.mysql.jdbc.Driver");
        poolProps.setUrl(dbUrl);
        poolProps.setUsername(dbUser);
        poolProps.setPassword(dbPass);
        poolProps.setTestOnBorrow(true);
        poolProps.setValidationQuery("select 1");
        dataSource.setPoolProperties(poolProps);
    }

    public synchronized static JDBCPool getInstance() {
        if (JDBCPool == null) {
            try {
                JDBCPool = new JDBCPool();
            } catch (Exception e) {
                return null;
            }
        }
        return JDBCPool;
    }

    public ResultSet getConnection(String sql) throws SQLException {
        try {
//        if (conn == null) {
//            JDBCPool = new JDBCPool();
//        }
            conn = dataSource.getConnection();
            PreparedStatement state = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = state.executeQuery();
            return resultSet;
        }finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

//    public Future<Pair<ResultSet, DaoType>> asyncExecQuery(String sql, final DaoType queryType) {
//        return exec.submit(new Callable<Pair<ResultSet, DaoType>>() {
//            @Override
//            public Pair<ResultSet, DaoType> call() throws Exception {
//                Connection connection = null;
//                try {
//                    connection = getConnection();
//                    Statement state = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
//                    ResultSet resultSet = state.executeQuery(sql);
//                    return Pair.of(resultSet, queryType);
//                } finally {
//                    if (connection != null) {
//                        free(connection);
//                    }
//                }
//            }
//        });
//    }

}
