package com.ijunhai.model.parsers;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;


public class DimensionComparator implements Comparator<String> {
    private Map<String, Map<String, String>> keyDimensionHashMap;
    private Map<String, List<Map<String, String>>> keyMetricMap;
    private int num = 1;
    private DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd");
    private DateTimeFormatter mysqlFormat = DateTimeFormat.forPattern("yyyyMMdd");

    private String granularity;
    private List<String> orderByList;

    public DimensionComparator(Map<String, List<Map<String, String>>> keyMetricMap,
                               Map<String, Map<String, String>> keyDimensionHashMap,
                               String granularity, List<String> orderByList) {
        this.keyDimensionHashMap = keyDimensionHashMap;
        this.keyMetricMap = keyMetricMap;
        this.granularity = granularity;
        this.orderByList = orderByList;
    }

    public int compare(String a, String b) {

        //orderByList
        if (orderByList.size() != 0 && orderByList != null) {
            int i = 0;
            num = compareTo(a, b, i);
            //orderBylist null granularity notnull
        } else if (orderByList.size() == 0 && granularity.length() != 0) {
            DateTime timeC;
            DateTime timeD;
            if (!keyDimensionHashMap.get(a).get("date").contains("-")) {
                timeC = DateTime.parse(keyDimensionHashMap.get(a).get("date"), mysqlFormat);
            } else {
                timeC = DateTime.parse(keyDimensionHashMap.get(a).get("date"), format);
            }
            if (!keyDimensionHashMap.get(b).get("date").contains("-")) {
                timeD = DateTime.parse(keyDimensionHashMap.get(b).get("date"), mysqlFormat);
            } else {
                timeD = DateTime.parse(keyDimensionHashMap.get(b).get("date"), format);
            }

            if (timeC.compareTo(timeD) <= 0) {
                num = 1;
            } else {
                num = -1;
            }
        }
        return num;
    }

    private int compareTo(String a, String b, int i) {
        String[] split = orderByList.get(i).split(" ", 2);
        String orderByKey = split[0].toLowerCase();

        if (!orderByKey.equals("day") && !orderByKey.equals("hour") && !orderByKey.equals("minute")) {
            String orderByValueA = keyDimensionHashMap.get(a).get(orderByKey);
            String orderByValueB = keyDimensionHashMap.get(b).get(orderByKey);
            if (orderByValueA == null && orderByValueB == null) {
                StringBuffer tmpa = new StringBuffer();
                StringBuffer tmpb = new StringBuffer();
                keyMetricMap.get(a).forEach(map -> map.keySet().forEach(key -> {
                    if (orderByKey.equals(key)) {
                        tmpa.append(map.get(key));
                    }
                }));

                keyMetricMap.get(b).forEach(map -> map.keySet().forEach(key -> {
                    if (orderByKey.equals(key)) {
                        tmpb.append(map.get(key));
                    }
                }));
                orderByValueA = tmpa.toString();
                orderByValueB = tmpb.toString();
            }

            int numA = 0;
            int numB = 0;
            try {
                if (orderByValueA != null && !orderByValueA.isEmpty() && !orderByValueA.equals("error") && !orderByValueA.equals("null")) {
                    numA = Integer.parseInt(orderByValueA);
                }
                if (!orderByValueB.isEmpty() && !orderByValueB.equals("error") && !orderByValueB.equals("null")) {
                    numB = Integer.parseInt(orderByValueB);
                }
            } catch (NumberFormatException e) {
                if (orderByValueA.compareTo(orderByValueB) < 0) {
                    num = -1;
                } else if (orderByValueA.compareTo(orderByValueB) > 0) {
                    num = 1;
                } else if (orderByValueA.compareTo(orderByValueB) == 0 && i + 1 < orderByList.size()) {
                    i++;
                    compareTo(a, b, i);
                }
            }
            if (split.length == 2 && split[1].equals("asc") || split.length == 1) {
                if (numA < numB) {
                    num = -1;
                } else if (numA > numB) {
                    num = 1;
                } else if (numA == numB && i + 1 < orderByList.size()) {
                    i++;
                    compareTo(a, b, i);
                }
            } else {
                if (numA < numB) {
                    num = 1;
                } else if (numA > numB) {
                    num = -1;
                } else if (numA == numB && i + 1 < orderByList.size()) {
                    i++;
                    compareTo(a, b, i);
                }
            }

            //time
        } else if (orderByKey.equals("day") || orderByKey.equals("hour") || orderByKey.equals("minute")) {
            DateTime timeA = DateTime.parse(keyDimensionHashMap.get(a).get("date"), format);
            DateTime timeB = DateTime.parse(keyDimensionHashMap.get(b).get("date"), format);
            if (split.length == 2 && split[1].equals("asc") || split.length == 1) {
                if (timeA.compareTo(timeB) < 0) {
                    num = -1;
                } else if (timeA.compareTo(timeB) > 0) {
                    num = 1;
                } else if (timeA.compareTo(timeB) == 0 && orderByKey.equals("day") && i + 1 < orderByList.size()) {
                    i++;
                    compareTo(a, b, i);
                    //condition is hour or minute
                } else if (timeA.compareTo(timeB) == 0 && (orderByKey.equals("hour") || orderByKey.equals("minute"))) {
                    int hourA;
                    int hourB;

                    hourA = Integer.parseInt(keyDimensionHashMap.get(a).get("hour"));
                    hourB = Integer.parseInt(keyDimensionHashMap.get(b).get("hour"));
                    if (hourA < hourB) {
                        num = -1;
                    } else if (hourA > hourB) {
                        num = 1;
                    } else if (hourA == hourB && orderByKey.equals("hour") && i + 1 < orderByList.size()) {
                        i++;
                        compareTo(a, b, i);
                    } else if (hourA == hourB && orderByKey.equals("minute")) {
                        int minuteA;
                        int minuteB;

                        minuteA = Integer.parseInt(keyDimensionHashMap.get(a).get("minute"));
                        minuteB = Integer.parseInt(keyDimensionHashMap.get(b).get("minute"));
                        if (minuteA < minuteB) {
                            num = -1;
                        } else if (minuteA > minuteB) {
                            num = 1;
                        } else if (minuteA == minuteB && i + 1 < orderByList.size()) {
                            i++;
                            compareTo(a, b, i);
                        }
                    }
                }

                //desc
            } else {
                if (timeA.compareTo(timeB) < 0) {
                    num = 1;
                } else if (timeA.compareTo(timeB) > 0) {
                    num = -1;
                } else if (timeA.compareTo(timeB) == 0 && i + 1 < orderByList.size()) {
                    i++;
                    compareTo(a, b, i);
                } else if (timeA.compareTo(timeB) == 0 && (orderByKey.equals("hour") || orderByKey.equals("minute"))) {
                    int hourA;
                    int hourB;

                    hourA = Integer.parseInt(keyDimensionHashMap.get(a).get("hour"));
                    hourB = Integer.parseInt(keyDimensionHashMap.get(b).get("hour"));
                    if (hourA < hourB) {
                        num = 1;
                    } else if (hourA > hourB) {
                        num = -1;
                    } else if (hourA == hourB && orderByKey.equals("hour") && i + 1 < orderByList.size()) {
                        i++;
                        compareTo(a, b, i);
                    } else if (hourA == hourB && orderByKey.equals("minute")) {
                        int minuteA;
                        int minuteB;
                        minuteA = Integer.parseInt(keyDimensionHashMap.get(a).get("minute"));
                        minuteB = Integer.parseInt(keyDimensionHashMap.get(b).get("minute"));
                        if (minuteA < minuteB) {
                            num = 1;
                        } else if (minuteA > minuteB) {
                            num = -1;
                        } else if (minuteA == minuteB && i + 1 < orderByList.size()) {
                            i++;
                            compareTo(a, b, i);
                        }
                    }
                }
            }
        }
        return num;
    }
}
