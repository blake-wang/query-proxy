package com.ijunhai.model.metrics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ijunhai.dao.DaoType;

import java.util.List;


public class ComplxFirstPayRetentionUv implements Metric {

    @JsonProperty
    private List<Integer> values;

    public ComplxFirstPayRetentionUv(){

    }

    @JsonCreator
    public ComplxFirstPayRetentionUv(
            @JsonProperty("values") List<Integer> values
    ){
        this.values = values;
    }

    @Override
    public String getFuction(DaoType daoType) {
        return null;
    }

    @Override
    public String getConditions(DaoType daoType) {
        return null;
    }

    @Override
    public String getTableName(DaoType daoType) {
        return null;
    }

    @Override
    public String getName() {
        return "complex_first_pay_retention_uv";
    }

    @Override
    public List<Integer> getValues(){
        return values;
    }
}
