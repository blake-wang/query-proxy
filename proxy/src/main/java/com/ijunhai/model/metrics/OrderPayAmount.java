package com.ijunhai.model.metrics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ijunhai.dao.DaoType;
import com.ijunhai.util.PropertiesUtils;

import java.math.BigDecimal;

import static com.ijunhai.contants.ProxyConstants.GP_ORDER_TABLENAME;
import static com.ijunhai.contants.ProxyConstants.KYLIN_ORDER_TABLENAME;
import static com.ijunhai.contants.ProxyConstants.MYSQL_TABLENAME;


public class OrderPayAmount implements Metric {
    public static String NAME = "pay_amount";

    @JsonIgnore
    public String getFuction(DaoType daoType) {
        switch (daoType) {
            case MYSQL:
                return "sum(pay_sum) as pay_amount_m";
            case GP:
                return "sum(pay_amount) as \"pay_amount\" ";
            case KYLIN:
                return "sum(money) as \"pay_amount\" ";
            default:
                return null;
        }
    }

    @JsonIgnore
    public String getConditions(DaoType daoType) {
        StringBuilder sb = new StringBuilder();
        sb.append(" \"PAY_STATUS\" ='1' and \"STATUS\" ='2'  ");
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

    @Override
    public String format(String value) {
        BigDecimal bg = new BigDecimal(value);
        return bg.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }
}
