package com.ijunhai.model.metrics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ijunhai.dao.DaoType;
import com.ijunhai.util.PropertiesUtils;

import java.time.LocalDate;
import java.util.List;

import static com.ijunhai.contants.ProxyConstants.*;

public class LoginFirstPayRetentionUv implements Metric {
    public static String NAME = "first_pay_retention_uv";
    @JsonProperty
    private String firstPayDate;
    @JsonIgnore
    private LocalDate firstPayLocalDate;
    @JsonProperty
    private List<Integer> values;

    public LoginFirstPayRetentionUv() {
    }

    @JsonCreator
    public LoginFirstPayRetentionUv(
            @JsonProperty("firstPayDate") String firstPayDate,
            @JsonProperty("values") List<Integer> values
    ) {
        this.firstPayDate = firstPayDate;
        this.values = values;
        this.firstPayLocalDate = LocalDate.parse(firstPayDate);
    }


    @JsonCreator
    public LoginFirstPayRetentionUv(
            @JsonProperty("values") List<Integer> values
    ) {
        this.values = values;
    }

    @JsonIgnore
    public String getFuction(DaoType daoType) {
        switch (daoType) {
            case GP:
                StringBuilder sb = new StringBuilder();
                for (int i : values) {
                    sb.append("sum(login_people_").append(i).append(") as \"").append(i).append("first_pay_retention_uv\" ,");
                }
                if (sb.charAt(sb.length() - 1) == ',') {
                    sb.deleteCharAt(sb.length() - 1);
                }

                return sb.toString();
            case KYLIN:
                StringBuilder ssb = new StringBuilder();
                for (Integer days : values) {
                    LocalDate localDate = firstPayLocalDate.plusDays(days-1);
                    ssb.append("intersect_count(\"USER-USER_ID\", SERVER_DATE_DAY, array['").append(firstPayDate).append("','")
                            .append(localDate.toString()).append("']) as \"").append(days).append("first_pay_retention_uv\",");
                }
                if (ssb.length() > 0) {
                    ssb.deleteCharAt(ssb.length() - 1);
                }
                return ssb.toString();
                default:
                    return null;
        }
    }


    @JsonIgnore
    public String getConditions(DaoType daoType) {
        return " first_order_date = '" + firstPayDate + "' and EVENT = 'login' and is_test = 'regular' ";
    }

    @JsonIgnore
    public String getTableName(DaoType daoType) {
        switch (daoType) {
            case MYSQL:
                return MYSQL_TABLENAME;
            case KYLIN:
                return PropertiesUtils.get(KYLIN_LOGIN_TABLENAME);
            case GP:
                return PropertiesUtils.get(GP_RETENTION_FIRST_PAY_TABLENAME);
            default:
                return null;
        }
    }

    @JsonProperty
    public String getFirstPayDate() {
        return firstPayDate;
    }

    @JsonProperty
    public List<Integer> getValues() {
        return values;
    }

    @JsonIgnore
    public String getName() {
        return NAME;
    }

}
