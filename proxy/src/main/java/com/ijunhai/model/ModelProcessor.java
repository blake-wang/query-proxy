package com.ijunhai.model;

import com.ijunhai.contants.ProxyConstants;
import com.ijunhai.dao.DaoType;
import com.ijunhai.dao.MysqlDao;
import com.ijunhai.dao.ParallelDao;
import com.ijunhai.model.metrics.*;
import com.ijunhai.model.parsers.*;
import com.ijunhai.util.QueryCache;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.ijunhai.contants.ProxyConstants.longMetric;
import static com.ijunhai.dao.DaoType.KYLIN;
import static com.ijunhai.dao.DaoType.MYSQL;


public class ModelProcessor {
    private ScheduledExecutorService refreshFilterDataThread = Executors.newScheduledThreadPool(1);
    private static final Logger logger = LoggerFactory.getLogger(ModelProcessor.class);
    private static MessageDigest messageDigest;
    private String filterSql = ProxyConstants.MYSQL_FILTER_CONDITION;
    private List<String> list = new ArrayList<>();
    private MysqlDao mysqlConn = MysqlDao.getInstance();
    private List<Pair<DaoType, String>> sqlList;
    private String granularity;
    private List<Metric> metricList;
    private ResultParser resultParser;
    private ParallelDao conn;
    private QueryModel model;
    private List<String> metricNameLists;
    private DateTime startTime;
    private DateTime endTime;
    private DateTime startOfDay;

    static {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ignored) {
        }
    }

    public ModelProcessor(QueryModel model) {
        this.granularity = model.getGranularity();
        this.metricList = model.getMetrics();
        this.conn = ParallelDao.getInstance();
        this.resultParser = new ResultParser();
        this.model = model;
        this.sqlList = new ArrayList<>();
        this.metricNameLists = new ArrayList<>();
        this.startOfDay = new DateTime().withTimeAtStartOfDay();
        Condition conditions = model.getConditions();
        String start = conditions.getStart().trim();
        String end = conditions.getEnd().trim();
        if (start.trim().contains(" ")) {
            DateTimeFormatter formatA = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            startTime = DateTime.parse(start, formatA);
            endTime = DateTime.parse(end, formatA);
        } else {
            DateTimeFormatter formatB = DateTimeFormat.forPattern("yyyy-MM-dd");
            startTime = DateTime.parse(start, formatB);
            endTime = DateTime.parse(end, formatB);
        }
    }

    public List<Map<String, String>> process() throws Exception {
//        notInFilter();
        for (Metric metric : metricList) {
            String metricName = metric.getName();
            if (metricName.contains("complex")) {
                complexMetric(metric);
                continue;
            }
            buildSql(metric);
        }
        List<ResultSet> ResultList = conn.execQuery(sqlList);
        resultParser.resultSetParse(metricNameLists, ResultList);
        return resultParser.finalParse(model);
    }


    public void buildSql(Metric metric) throws Exception {
        metricNameLists.add(metric.getName());
        String time = granularity == null ? "" : granularity;
        if (metric.getFuction(MYSQL) != null && !time.equals("hour") && !time.equals("minute")) {
            sqlList.add(Pair.of(MYSQL, build(new MysqlParser(model, metric))));
        }
        if (longMetric.contains(metric.getName())) {
            // startTime大于（startOfDay-7）且endTime大于startOfDay则查ky实时数据；
            if (startTime.compareTo(startOfDay.minusDays(7)) == 1 && endTime.compareTo(startOfDay) >= 0) {
                sqlList.add(Pair.of(KYLIN, build(new KylinParser(model, metric))));
            } else {
                //startTime小于（startOfDay-7）或 endTime小于startOfDay则查GP历史数据；
                sqlList.add(Pair.of(DaoType.GP, build(new GPParser(model, metric))));
            }
        } else {
            //其他指标
            // startTime大于startOfDay实时ky表；endTime小于startOfDay历史GP数据；否则两块合并
            if (endTime.compareTo(startOfDay) == -1) {
                sqlList.add(Pair.of(DaoType.GP, build(new GPParser(model, metric))));
            } else if (startTime.compareTo(startOfDay) >= 0) {
                sqlList.add(Pair.of(KYLIN, build(new KylinParser(model, metric))));
            } else {
                SqlParser gp = new GPParser(model, metric);
                gp.setEndTime(startOfDay.minusDays(1));
                sqlList.add(Pair.of(DaoType.GP, build(gp)));

                SqlParser ky = new KylinParser(model, metric);
                ky.setStartTime(startOfDay);
                sqlList.add(Pair.of(KYLIN, build(ky)));
            }
        }

    }

    public String build(SqlParser sqlParser) throws Exception {
        sqlParser.bulid();
        String sql = sqlParser.getSelectSQL() + sqlParser.getTableName() +
                sqlParser.getWhereSQL() + sqlParser.getGroupBySql();
        //添加大蓝海外数据
        String sql1 = sql;
        if (!sqlParser.getTableName().contains("rpt") && !sqlParser.getTableName().contains("import")) {
            addDLHW(Pair.of(sqlParser.getTableName(), sql1));
//            sql = sql1.replaceAll("where", "where " + FilterGameChannelId.getFilter(sqlParser.getTableName()) + " and ");
        }
        return sql;
    }

    public void addDLHW(Pair<String, String> pair) {
        if (pair.getLeft().trim().equals("from aglogin")) {
            String dlLogin = pair.getRight().replaceAll("aglogin", "dllogin");
            String hwLogin = pair.getRight().replaceAll("aglogin", "hwlogin");
            sqlList.add(pair.of(KYLIN, dlLogin));
            sqlList.add(pair.of(KYLIN, hwLogin));
        } else {
            String dlOrder = pair.getRight().replaceAll("agorder", "dlorder")
                    .replaceAll(" (?i)channel_id ", " \"AGENT-CHANNEL_ID\" ")
                    .replaceAll(" (?i)game_channel_id ", " \"AGENT-GAME_CHANNEL_ID\" ")
                    .replaceAll(" (?i)game_id ", " \"GAME-GAME_ID\" ")
                    .replaceAll(" (?i)sub_pf ", " \"DEVICE-OS_TPYE\" ")
                    .replaceAll("(?i)\"pay_status\"", "\"ORDER-ORDER_STATUS\"")
                    .replaceAll("(?i)\"status\"", "\"ORDER-ORDER_STEP\"")
                    .replaceAll("(?i)money", "\"ORDER-CURRENCY_AMOUNT\"")
                    .replaceAll("='1'", "='success'")
                    .replaceAll("='2'", "='paid_success' and is_test = 'regular'")
                    .replaceAll("(?i)USER_ID", "USER-USER_ID");

            String hwOrder = dlOrder.replaceAll("dlorder", "hworder")
                    .replaceAll("\"ORDER-CURRENCY_AMOUNT\"","\"ORDER-CNY_AMOUNT\"");
            sqlList.add(pair.of(KYLIN, dlOrder));
            sqlList.add(pair.of(KYLIN, hwOrder));

        }
    }


//    public void notInFilter() throws Exception {
//        if (FilterGameChannelId.getFilter("") == null) {
//            ResultSet resultSet = null;
//            try {
//                resultSet = mysqlConn.execQuery(filterSql);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            list.clear();
//            while (resultSet.next()) {
//                String string = resultSet.getString(1);
//                if (string != null) {
//                    list.add(string);
//                }
//            }
//            FilterGameChannelId.setList(list);
//        }
//        refreshFilterDataThread.scheduleWithFixedDelay(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    ResultSet resultSet = mysqlConn.execQuery(filterSql);
//                    list.clear();
//                    while (resultSet.next()) {
//                        String string = resultSet.getString(1);
//                        if (string != null) {
//                            list.add(string);
//                        }
//                    }
//                    FilterGameChannelId.setList(list);
//                } catch (Exception ignored) {
//                }
//            }
//        }, 20, 60, TimeUnit.MINUTES);
//    }

    public void complexMetric(Metric metric) throws Exception {
        String metricName = metric.getName();
        List<Integer> valuesList = metric.getValues();
        DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime startTime = format.parseDateTime(model.getConditions().getStart().split(" ")[0]);
        DateTime endTime = format.parseDateTime(model.getConditions().getEnd().split(" ")[0]);
        List<String> timeList = new ArrayList<>();
        timeList.add(startTime.toString("yyyy-MM-dd"));
        DateTime startTimeTmp = startTime;
        while (!startTimeTmp.equals(endTime)) {
            startTimeTmp = startTimeTmp.plusDays(1);
            timeList.add(startTimeTmp.toString("yyyy-MM-dd"));
        }
        //kylin
        if (startTime.compareTo(startOfDay.minusDays(7)) == 1 && endTime.compareTo(startOfDay) >= 0) {
            for (String date : timeList) {
                DateTime today = new DateTime().withTimeAtStartOfDay();
                switch (metricName) {
                    case "complex_retention_uv":
                        metric = new LoginRetentionUv(date, valuesList);
                        break;
                    case "complex_first_pay_retention_nuv":
                        metric = new LoginFirstPayRetentionNuv(date, valuesList);
                        break;
                    case "complex_first_pay_retention_uv":
                        metric = new LoginFirstPayRetentionUv(date, valuesList);
                        break;
                    case "complex_yet_pay_nuv":
                        if (valuesList == null) {
                            metric = new OrderYetPayNuv(date, today.toString("yyyy-MM-dd"), 0);
                            buildSql(metric);
                        } else {
                            for (int i : valuesList) {
                                metric = new OrderYetPayNuv(date, format.parseDateTime(date).plusDays(i).toString("yyyy-MM-dd"), i);
                                buildSql(metric);
                            }
                        }
                        continue;
                    case "complex_nu_yet_pay_amount":
                        if (valuesList == null) {
                            metric = new OrderNuYetPayAmount(date, today.toString("yyyy-MM-dd"), 0);
                            buildSql(metric);
                        } else {
                            for (int i : valuesList) {
                                metric = new OrderNuYetPayAmount(date, format.parseDateTime(date).plusDays(i).toString("yyyy-MM-dd"), i);
                                buildSql(metric);
                            }
                        }
                        continue;
                }
                buildSql(metric);
            }
            //GP
        } else {
            switch (metricName) {
                case "complex_retention_uv":
                    metric = new LoginRetentionUv(valuesList);
                    break;
                case "complex_first_pay_retention_nuv":
                    metric = new LoginFirstPayRetentionNuv(valuesList);
                    break;
                case "complex_first_pay_retention_uv":
                    metric = new LoginFirstPayRetentionUv(valuesList);
                    break;
                case "complex_yet_pay_nuv":
                    metric = new OrderYetPayNuv(valuesList);
                    break;
                case "complex_nu_yet_pay_amount":
                    metric = new OrderNuYetPayAmount(valuesList);
                    break;
            }
            buildSql(metric);
        }
    }

    public ResultSet getCacheResultSet(String sql) throws ExecutionException, SQLException {
        String mysqlKey = Base64.getEncoder().encodeToString(messageDigest.digest(sql.getBytes()));
        ResultSet mysqlResultSet = QueryCache.getCache().get(mysqlKey, new Callable<ResultSet>() {
            @Override
            public ResultSet call() throws Exception {
                return mysqlConn.execQuery(sql);
            }
        });
        mysqlResultSet.beforeFirst();
        return mysqlResultSet;
    }
}