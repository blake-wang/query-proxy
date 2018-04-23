package com.ijunhai.model.metrics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ijunhai.dao.DaoType;
import com.ijunhai.util.PropertiesUtils;

import java.time.LocalDate;
import java.util.List;

import static com.ijunhai.contants.ProxyConstants.*;


public class LoginFirstPayRetentionNuv implements Metric {
    public static String NAME = "first_pay_retention_nuv";
    @JsonProperty
    private String regDate;
    @JsonIgnore
    private LocalDate regLocalDate;
    @JsonProperty
    private List<Integer> values;

    public LoginFirstPayRetentionNuv() {
    }

    @JsonCreator
    public LoginFirstPayRetentionNuv(
            @JsonProperty("regDate") String regDate,
            @JsonProperty("values") List<Integer> values
    ) {
        this.regDate = regDate;
        this.values = values;
        this.regLocalDate = LocalDate.parse(regDate);
    }

    @JsonCreator
    public LoginFirstPayRetentionNuv(
            @JsonProperty("values") List<Integer> values
    ) {
        this.values = values;
    }

    @JsonIgnore
    public String getFuction(DaoType daoType) {
        switch (daoType) {
            case GP:
                StringBuffer sb = new StringBuffer();
                for (int i : values) {
                    sb.append("sum(login_people_").append(i).append(") as \"").append(i).append("first_pay_retention_nuv\" ,");
                }
                if (sb.charAt(sb.length() - 1) == ',') {
                    sb.deleteCharAt(sb.length() - 1);
                }

                return sb.toString();
            case KYLIN:
                StringBuffer ssb = new StringBuffer();
                for (Integer days : values) {
                    LocalDate localDate = regLocalDate.plusDays(days - 1);
                    ssb.append("intersect_count(\"USER-USER_ID\", SERVER_DATE_DAY, array['").append(regDate).append("','")
                            .append(localDate.toString()).append("']) as \"").append(days).append("first_pay_retention_nuv\",");
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

        return " reg_date = '" + regDate + "' and reg_date = first_order_date and EVENT = 'login' and is_test = 'regular' ";
    }

    @JsonIgnore
    public String getTableName(DaoType daoType) {
        switch (daoType) {
            case MYSQL:
                return MYSQL_TABLENAME;
            case KYLIN:
                return PropertiesUtils.get(KYLIN_LOGIN_TABLENAME);
            case GP:
                return PropertiesUtils.get(GP_RETENTION_NEW_PAY_TABLENAME);
            default:
                return null;
        }
    }

    @JsonProperty
    public String getRegDate() {
        return regDate;
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
