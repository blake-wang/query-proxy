package com.ijunhai.model.metrics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ijunhai.dao.DaoType;
import com.ijunhai.util.PropertiesUtils;

import static com.ijunhai.contants.ProxyConstants.GP_ORDER_TABLENAME;
import static com.ijunhai.contants.ProxyConstants.KYLIN_ORDER_TABLENAME;
import static com.ijunhai.contants.ProxyConstants.MYSQL_TABLENAME;


public class OrderFirstPayUv implements Metric {

    public static String NAME = "first_pay_uv";
    @JsonIgnore
    public String getFuction(DaoType daoType) {
        switch (daoType) {
            case KYLIN:
                return "count(distinct \"USER_ID\") as \"first_pay_uv\" ";
            case GP:
                return "sum(first_pay_uv) as \"first_pay_uv\" ";
            default:
                return null;
        }
    }


    @JsonIgnore
    public String getConditions(DaoType daoType){
        return " first_order_date = server_date_day and \"PAY_STATUS\" ='1' and \"STATUS\" ='2' ";
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
