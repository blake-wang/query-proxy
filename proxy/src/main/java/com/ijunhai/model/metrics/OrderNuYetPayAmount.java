package com.ijunhai.model.metrics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ijunhai.dao.DaoType;
import com.ijunhai.util.PropertiesUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.ijunhai.contants.ProxyConstants.*;


public class OrderNuYetPayAmount implements Metric {
    public static String NAME = "nu_yet_pay_amount";

    @JsonProperty
    private String regDate;
    @JsonIgnore
    private LocalDate regLocalDate;
    @JsonProperty
    private String yetDate;
    @JsonIgnore
    private LocalDate yetLocalDate;
    @JsonProperty
    private String i;
    @JsonProperty
    private List<Integer> values;

    public OrderNuYetPayAmount() {
    }

    @JsonCreator
    public OrderNuYetPayAmount(
            @JsonProperty("regDate") String regDate,
            @JsonProperty("yetDate") String yetDate,
            @JsonProperty("day") Integer i
    ) {
        this.regDate = regDate;
        this.yetDate = yetDate;
        this.regLocalDate = LocalDate.parse(regDate);
        this.yetLocalDate = LocalDate.parse(yetDate);
        this.i = i == 0 ? "" : i.toString();

    }

    @JsonCreator
    public OrderNuYetPayAmount(
            @JsonProperty("values") List<Integer> values
    ) {
        this.values = values;
    }

    @JsonIgnore
    public String getFuction(DaoType daoType) {
        StringBuffer sb = new StringBuffer();
        switch (daoType) {
            case KYLIN:
                sb.append("sum(money) as \"").append(i).append("nu_yet_pay_amount\" ");
                break;
            case GP:
                for (int i : values) {
                    sb.append("sum(pay_sum_").append(i).append(") as \"").append(i).append("yet_pay_amount\" ,");
                }
                break;
            default:
                return null;
        }
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    @JsonIgnore
    public String getConditions(DaoType daoType) {
        StringBuilder sb = new StringBuilder();
        sb.append(" reg_date = '").append(regDate).append("' and  server_date_day  between '")
                .append(regDate).append("' and '").append(yetDate)
                .append("' and \"PAY_STATUS\" ='1' and \"STATUS\" ='2'  ");

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
                return PropertiesUtils.get(GP_TOTAL_TABLENAME);
            default:
                return null;
        }
    }

    @JsonIgnore
    public String getName() {
        return NAME;
    }

    @JsonProperty
    public String getRegDate() {
        return regDate;
    }

    @Override
    public String format(String value) {
        BigDecimal bg = new BigDecimal(value);
        return bg.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }
}
