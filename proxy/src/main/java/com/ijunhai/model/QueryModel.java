package com.ijunhai.model;


import com.ijunhai.model.metrics.Metric;

import java.util.List;

public class QueryModel {
    private List<Metric> metrics;
    private Condition conditions;
    private String granularity;
    private List<String> returnDemensions;
    private List<String> orderByFields;
    private String limit;

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public List<String> getOrderByFields() {
        return orderByFields;
    }

    public void setOrderByFields(List<String> orderByFields) {
        this.orderByFields = orderByFields;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }

    public Condition getConditions() {
        return conditions;
    }

    public void setConditions(Condition conditions) {
        this.conditions = conditions;
    }

    public String getGranularity() {
        return granularity;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    public List<String> getReturnDemensions() {
        return returnDemensions;
    }

    public void setReturnDemensions(List<String> returnDemensions) {
        this.returnDemensions = returnDemensions;
    }


}
