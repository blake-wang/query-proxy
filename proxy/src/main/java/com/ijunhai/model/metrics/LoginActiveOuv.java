package com.ijunhai.model.metrics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ijunhai.dao.DaoType;
import com.ijunhai.util.PropertiesUtils;

import static com.ijunhai.contants.ProxyConstants.GP_LOGIN_TABLENAME;
import static com.ijunhai.contants.ProxyConstants.KYLIN_LOGIN_TABLENAME;
import static com.ijunhai.contants.ProxyConstants.MYSQL_TABLENAME;

public class LoginActiveOuv implements Metric {

    public static String NAME = "active_ouv";

    @JsonIgnore
    public String getFuction(DaoType daoType) {
        switch (daoType) {
            case GP:
                return "sum(active_ouv) as active_ouv";
            case KYLIN:
                return "count(distinct \"USER-USER_ID\") as \"active_ouv\" ";
            default:
                return null;
        }
    }


    @JsonIgnore
    public String getConditions(DaoType daoType) {
        return " reg_date != server_date_day and EVENT = 'login' and is_test = 'regular' ";
    }

    @JsonIgnore
    public String getTableName(DaoType daoType) {
        switch (daoType) {
            case MYSQL:
                return MYSQL_TABLENAME;
            case KYLIN:
                return PropertiesUtils.get(KYLIN_LOGIN_TABLENAME);
            case GP:
                return PropertiesUtils.get(GP_LOGIN_TABLENAME);
            default:
                return null;
        }
    }

    @JsonIgnore
    public String getName() {
        return NAME;
    }

}
