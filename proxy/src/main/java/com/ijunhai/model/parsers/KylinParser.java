package com.ijunhai.model.parsers;

import com.ijunhai.contants.ProxyConstants;
import com.ijunhai.model.Condition;
import com.ijunhai.model.QueryModel;
import com.ijunhai.model.metrics.Metric;
import com.ijunhai.util.FieldMapping;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.ijunhai.dao.DaoType.KYLIN;


public class KylinParser implements SqlParser {
    private String granularity;
    private Condition conditions;
    private Metric metric;
    private List<String> returnDemensionsList;
    private String selectSQL;
    private String groupBySQL;
    private StringBuffer whereSQL;
    private String tableName;
    private DateTime startTime;
    private DateTime endTime;

    private static final Logger logger = LoggerFactory.getLogger(KylinParser.class);

    public KylinParser(QueryModel model, Metric metric) {
        this.granularity = model.getGranularity();
        this.conditions = model.getConditions();
        this.metric = metric;
        this.returnDemensionsList = model.getReturnDemensions();

        String start = conditions.getStart().trim();
        String end = conditions.getEnd().trim();
        tableName = metric.getTableName(KYLIN);
        if (start.contains(" ")) {
            DateTimeFormatter formatA = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            startTime = DateTime.parse(start, formatA);
            endTime = DateTime.parse(end, formatA);
        } else {
            DateTimeFormatter formatB = DateTimeFormat.forPattern("yyyy-MM-dd");
            startTime = DateTime.parse(start, formatB);
            endTime = DateTime.parse(end, formatB);
        }
    }


    @Override
    public void bulid() throws  Exception{
        bulidSelectAndGroupBySQL();
        bulidWhereSQL();
    }

    @Override
    public String getSelectSQL() {
        return selectSQL;
    }

    @Override
    public String getTableName() {
        return " from " + tableName;
    }

    @Override
    public String getGroupBySql() {
        return groupBySQL;
    }

    @Override
    public String getWhereSQL() {
        return whereSQL.toString();
    }

    @Override
    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    private void bulidSelectAndGroupBySQL() throws Exception {
        StringBuffer selectSql = new StringBuffer();
        StringBuffer groupBySql = new StringBuffer();
        groupBySql.append(" group by ");
        selectSql.append("select ").append(metric.getFuction(KYLIN)).append(" , ");
        if (!returnDemensionsList.isEmpty()) {
            for (String returnDemension : returnDemensionsList) {
                if (StringUtils.isNoneBlank(FieldMapping.getTimeColumn(returnDemension.toUpperCase()))) {
                    //returnDemensionsList is time
                    logger.error("returnDemension cannot contain " + returnDemension);
                }
                selectSql.append(FieldMapping.getKylin(returnDemension.toUpperCase(), tableName))
                        .append(" as ").append(returnDemension).append(" , ");
                groupBySql.append(FieldMapping.getKylin(returnDemension.toUpperCase(), tableName)).append(" , ");
            }
        }
        if (!metric.getName().contains("retention") && !metric.getName().contains("yet")) {
            switch (granularity.toLowerCase()) {
                case "day":
                    groupBySql.append(" server_date_day ");
                    selectSql.append(" server_date_day as \"date\" ");
                    break;
                case "hour":
                    groupBySql.append(" server_date_day, server_date_hour ");
                    selectSql.append(" server_date_day as \"date\",server_date_hour as \"hour\" ");
                    break;
                case "minute":
                    groupBySql.append(" server_date_day, server_date_hour, server_date_minute ");
                    selectSql.append(" server_date_day as \"date\",server_date_hour as \"hour\",server_date_minute as \"minute\" ");
                    break;
                default:
                    break;
            }
        } else if (ProxyConstants.longMetric.contains(metric.getName())
                && !metric.getName().equals("first_pay_retention_uv")) {
            groupBySql.append(" reg_date ");
            selectSql.append(" reg_date as \"date\" ");
        } else if (metric.getName().equals("first_pay_retention_uv")) {
            groupBySql.append(" first_order_date ");
            selectSql.append(" first_order_date as \"date\" ");
        }

        if (groupBySql.toString().trim().equals("group by")) {
            groupBySQL = "";
        } else {
            if (groupBySql.charAt(groupBySql.length() - 2) == ',') {
                groupBySql.deleteCharAt(groupBySql.length() - 2);
            }
            groupBySQL = groupBySql.toString();
        }

        if (selectSql.charAt(selectSql.length() - 2) == ',') {
            selectSql.deleteCharAt(selectSql.length() - 2);
        }
        selectSQL = selectSql.toString();
    }

    private void bulidWhereSQL() throws  Exception{
        whereSQL = new StringBuffer();
        whereSQL.append(" where ");
        if (!metric.getName().contains("retention") && !metric.getName().contains("yet")) {

            if (!conditions.getStart().trim().contains(" ")) {
                whereSQL.append(FieldMapping.TIME_COLUMN).append(" between '")
                        .append(startTime.toString("yyyy-MM-dd")).append("' and '")
                        .append(endTime.toString("yyyy-MM-dd")).append("'");
            } else {
                int days = Days.daysBetween(startTime, endTime).getDays();
                String startDate = startTime.toString("yyyy-MM-dd");
                String endDate = endTime.toString("yyyy-MM-dd");
                String startHour = String.format("%02d", startTime.getHourOfDay());
                String endHour = String.format("%02d", endTime.getHourOfDay());
                String startMinute = String.format("%02d", startTime.getMinuteOfHour());
                String endMinute = String.format("%02d", endTime.getMinuteOfHour());
                whereSQL.append(" (");
                //hour 前闭后闭
                whereSQL.append("(( server_date_day = '").append(startDate)
                        .append("' and  server_date_hour >= '").append(startHour).append("') ");
                if (!endHour.equals("00")) {
                    if (startDate.equals(endDate)) {
                        whereSQL.append("and");
                    } else {
                        whereSQL.append("or");
                    }
                    whereSQL.append(" ( server_date_day = '").append(endDate).append("'")
                            .append(" and  server_date_hour <= '").append(endHour).append("')");
                }
                //minute
//            whereSQL.append(") or (");
//            whereSQL.append("( server_date_day = '").append(startDate)
//                    .append("' and server_date_hour = '").append(startHour)
//                    .append("' and server_date_minute >= '").append(startMinute).append("') ");
//            if (startHour.equals(endHour)) {
//                whereSQL.append("and");
//            } else {
//                whereSQL.append("or");
//            }
//            whereSQL.append(" ( server_date_day = '").append(endDate)
//                    .append("' and  server_date_hour = '").append(endHour)
//                    .append("' and server_date_minute <= '").append(endMinute).append("') ");

                //date
                whereSQL.append(" ) ");
                if (days > 1) {
                    whereSQL.append(" or ");
                    whereSQL.append("(server_date_day >='").append(startTime.plusDays(1).toString("yyyy-MM-dd")).append("' and ")
                            .append("(server_date_day <='").append(endTime.minusDays(1).toString("yyyy-MM-dd")).append("'))");
                }
                whereSQL.append(")");
            }
        }
        //notInfilter
//        if ((metric.equals(AGENT) || queryType.equals(AGENTHISTORY)) && filter.toString().length() > 1) {
//            if (!metric.getName().contains("retention") && !metric.getName().contains("yet")) {
//                whereSQL.append(" and ");
//            }
//            whereSQL.append(FieldMapping.get("GAMECHANNELID", tableName))
//                    .append(" not in (")
//                    .append(filter.toString().substring(1, filter.toString().length() - 1))
//                    .append(")");
//        }

        whereSQL.append(" ");
        whereSQL.append(conditionParse(conditions.getChannelId(), "channelId"))
                .append(conditionParse(conditions.getGameChannelId(), "gameChannelId"))
                .append(conditionParse(conditions.getOsType(), "osType"))
                .append(conditionParse(conditions.getCompanyId(), "companyId"))
                .append(conditionParse(conditions.getGameId(), "gameId"));

        if (!whereSQL.toString().trim().equals("where")) {
            whereSQL.append(" and ");
        }
        whereSQL.append(metric.getConditions(KYLIN));
    }

    private String conditionParse(List list, String conditionName) throws  Exception{
        StringBuilder sb = new StringBuilder();
        if (list != null && list.size() > 0) {
            if (!whereSQL.toString().trim().equals("where")) {
                sb.append(" and ");
            }
            sb.append(FieldMapping.getKylin(conditionName.toUpperCase(), tableName))
                    .append(" in ('").append(list.get(0)).append("'");
            for (int i = 1; i < list.size(); i++) {
                sb.append(" , '").append(list.get(i)).append("'");
            }
            sb.append(") ");
        }
        return sb.toString();
    }


}
