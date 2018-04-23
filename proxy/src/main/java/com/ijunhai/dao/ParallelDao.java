package com.ijunhai.dao;

import org.apache.commons.lang3.tuple.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.ijunhai.dao.DaoType.GP;
import static com.ijunhai.dao.DaoType.MYSQL;

public class ParallelDao {

    private static final int DEFAULT_AWAIT_SECONDS = 60;

    private final ExecutorService exec;
    private final KylinDao kylinDao;
    private final MysqlDao mysqlDao;
    private final GPDao gpDao;


    private static ParallelDao parallelDao;

    private static class LazyHolder {
        private static final ParallelDao INSTANCE = new ParallelDao();
    }

    ParallelDao() {
        exec = Executors.newFixedThreadPool(25);
        kylinDao = KylinDao.getInstance();
        mysqlDao = MysqlDao.getInstance();
        gpDao = GPDao.getInstance();
    }

    public static ParallelDao getInstance() {
        return LazyHolder.INSTANCE;
    }


    public List<ResultSet> execQuery(List<Pair<DaoType, String>> sqls) throws Exception {
        final CountDownLatch latch = new CountDownLatch(sqls.size());
        List<Future<ResultSet>> futures = new ArrayList<>();
        for (Pair<DaoType, String> sql : sqls) {
            futures.add(exec.submit(new ResultSetCallback(sql, latch)));
        }
        latch.await(DEFAULT_AWAIT_SECONDS, TimeUnit.SECONDS);
        List<ResultSet> resultSets = new ArrayList<>();

        boolean isFinish = true;
        for (Future<ResultSet> future : futures) {
            if (!future.isDone()) {
                isFinish = false;
                future.cancel(true);
            }
        }
        if (isFinish) {
            for (Future<ResultSet> future : futures) {
                try {
                    resultSets.add(future.get());
                } catch (Exception ex) {
                    throw new Exception(ex);
                }
            }
        } else {
            throw new TimeoutException("query time out in " + DEFAULT_AWAIT_SECONDS + "s");
        }
        return resultSets;
    }

    class ResultSetCallback implements Callable<ResultSet> {

        private Pair<DaoType, String> sql;
        private CountDownLatch latch;

        public ResultSetCallback(Pair<DaoType, String> sql, CountDownLatch latch) {
            this.sql = sql;
            this.latch = latch;
        }

        @Override
        public ResultSet call() throws Exception {
            ResultSet resultSet = null;
            try {
                switch (sql.getLeft()) {
                    case KYLIN:
                        resultSet = kylinDao.execQuery(sql.getRight());
                        break;
                    case MYSQL:
                        resultSet = mysqlDao.execQuery(sql.getRight());
                        break;
                    case GP:
                        resultSet = gpDao.execQuery(sql.getRight());
                        break;
                }
            } catch (Exception ex) {
                throw new Exception("query error", ex);
            } finally {
                latch.countDown();
            }
            return resultSet;
        }
    }

}
