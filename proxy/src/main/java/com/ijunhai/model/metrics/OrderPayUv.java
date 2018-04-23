package com.ijunhai.model.metrics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ijunhai.dao.DaoType;
import com.ijunhai.util.PropertiesUtils;

import static com.ijunhai.contants.ProxyConstants.*;

public class OrderPayUv implements Metric {
    public static String NAME = "pay_uv";

    @JsonIgnore
    public String getFuction(DaoType daoType) {
        switch (daoType) {
            case MYSQL:
                return "sum(pay_people) as pay_uv_m ";
            case KYLIN:
                return "count(distinct \"USER_ID\") as \"pay_uv\" ";
            case GP:
                return "sum(pay_uv) as \"pay_uv\" ";
            default:
                return null;
        }
    }

    @JsonIgnore
    public String getConditions(DaoType daoType) {
        StringBuilder sb = new StringBuilder();
        sb.append("  \"PAY_STATUS\" ='1' and \"STATUS\" ='2'  ");
        return sb.toString();

    }


    @JsonIgnore
    public String getTableName(DaoType daoType) {
        switch (daoType) {
            case MYSQL:
                return MYSQL_TABLENAME;
            case KYLIN:
                return PropertiesUtils.get(KYLIN_ORDER_TABLENAME);
            case GP:
                return PropertiesUtils.get(GP_ORDER_TABLENAME);
            default:
                return null;
        }
    }

    @JsonIgnore
    public String getName() {
        return NAME;
    }


}
