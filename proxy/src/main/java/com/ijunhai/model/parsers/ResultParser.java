package com.ijunhai.model.parsers;

import com.ijunhai.model.metrics.Metric;
import com.ijunhai.model.QueryModel;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;


public class ResultParser {

    private Map<String, List<Map<String, String>>> finalMetricMap;
    private Map<String, Map<String, String>> finalDimensionMap;
    private static MessageDigest messageDigest;

    static {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ignored) {
        }
    }

    public ResultParser() {
        this.finalMetricMap = new HashMap<>();
        this.finalDimensionMap = new HashMap<>();
    }

    public void resultSetParse(List<String> metricNameLists, List<ResultSet> ResultSets) throws Exception {
        for (ResultSet resultSet : ResultSets) {
            ResultSetMetaData metaData = resultSet.getMetaData();
//            String tableName = metaData.getTableName(1) == null ? "kylin" : metaData.getTableName(1);
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                if (columnCount == 1 && StringUtils.isEmpty(resultSet.getString(1))) {
                    break;
                }
                Map<String, String> dimensionMap = new TreeMap<>();
                Map<String, String> metricMap = new HashMap<>();

                for (int i = 1; i <= columnCount; i++) {
                    String key = metaData.getColumnLabel(i);
                    String value = resultSet.getString(i);
                    if (key.equals("date") && !value.contains("-")) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(value);
                        sb.insert(6, "-").insert(4, "-");
                        value = sb.toString();
                    }
                    //use 'contains'  because retention's key (like "3retention_uv") is different from metric.getName()
//                    if (key.toLowerCase().contains(metric.getName()) && !queryType.equals(MYSQL)) {
                    //证明key为指标
                    if ((key.toLowerCase().contains("retention") || key.toLowerCase().contains("yet")
                            || metricNameLists.contains(key.toLowerCase())) && !key.equals("_m")) {
                        metricMap.put(key.toLowerCase(), value);
                        if (!metricMap.containsKey(key.toLowerCase() + "_revision")) {
                            metricMap.put(key.toLowerCase() + "_revision", "0");
                        }
                    } else if (key.contains("_m")) {
                        key = key.substring(0, key.length() - 2);
                        metricMap.put(key.toLowerCase(), value);
                        if (key.contains("amount")) {
                            BigDecimal bg = new BigDecimal(value);
                            metricMap.put(key.toLowerCase() + "_revision", bg.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        } else {
                            metricMap.put(key.toLowerCase() + "_revision", value.split("\\.")[0]);
                        }
                    } else {
                        dimensionMap.put(key.toLowerCase(), value);
                    }
                }


                String key = Base64.getEncoder().encodeToString(messageDigest.digest(dimensionMap.toString().getBytes()));
                if (!finalDimensionMap.containsKey(key)) {
                    finalDimensionMap.put(key, dimensionMap);
                }
                StringBuffer sb = new StringBuffer();
                if (!finalMetricMap.containsKey(key)) {
                    finalMetricMap.put(key, new ArrayList<>());
                    finalMetricMap.get(key).add(metricMap);

                    //merge results
                } else if (finalMetricMap.containsKey(key)) {
//                Map<String, List<Map<String, String>>> finalMetricMap
                    finalMetricMap.get(key).forEach(map -> map.keySet().forEach(k -> {
//                        k.equals(metric.getName())
//                        if (metricMap.get(k)==null){
//                            metricMap.put(k,"0");
//                        }
//                        if (map.get(k)==null){
//                            map.put(k,"0");
//                        }
//
//                        metricMap.putIfAbsent(k, "0");
//                        map.putIfAbsent(k,"0");
                        if (metricMap.keySet().contains(k) && !k.contains("_revision") && metricMap.get(k) != null && map.get(k) != null) {
                            double a = Double.parseDouble(map.get(k)) + Double.parseDouble(metricMap.get(k));
                            map.put(k, a + "");
                            sb.append(k);
                        }
                    }));
                    //Query the metric that mysql hasn't and kylin has.And they have the same dimension (key).

                    if (sb.toString().isEmpty()) {
                        finalMetricMap.get(key).add(metricMap);
                    }

                }
            }
        }

    }


    public List<Map<String, String>> finalParse(QueryModel model) {

        String granularity = model.getGranularity();
        List<Metric> metricList = model.getMetrics();
        List<String> orderByList = model.getOrderByFields();
        String limit = model.getLimit() == null ? "2000" : model.getLimit();
        //order by
        DimensionComparator bvc = new DimensionComparator(finalMetricMap, finalDimensionMap, granularity, orderByList);
        Map<String, Map<String, String>> keyDimensionTreeMap = new TreeMap<>(bvc);
        keyDimensionTreeMap.putAll(finalDimensionMap);

        List<Map<String, String>> result = new ArrayList<>();
        Set<Map.Entry<String, Map<String, String>>> set = keyDimensionTreeMap.entrySet();
        for (Map.Entry<String, Map<String, String>> i : set) {
            Map<String, String> resultMap = new HashMap<>();
            resultMap.putAll(i.getValue());

            finalMetricMap.get(i.getKey()).forEach(metricMap -> metricMap.forEach(resultMap::put));

            for (Metric metric : metricList) {
                String value = resultMap.get(metric.getName());

                if (!metric.getName().contains("retention") && !metric.getName().contains("yet")) {
                    if ((!resultMap.keySet().contains(metric.getName()) || StringUtils.isEmpty(value))) {
                        resultMap.put(metric.getName(), "");
                        resultMap.put(metric.getName() + "_revision", "0");
                    } else {
                        resultMap.put(metric.getName(), metric.format(value));
                    }
                }
            }
            result.add(resultMap);
        }
        if (!limit.isEmpty()) {
            int lm = Integer.parseInt(limit);
            if (result.size() > lm) {
                result = result.subList(0, lm);
            }
        }
        return result;
    }
}
