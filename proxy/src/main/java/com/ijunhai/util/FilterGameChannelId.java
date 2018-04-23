package com.ijunhai.util;

import java.util.List;


public class FilterGameChannelId {
    private static List<String> list;

    public static String getFilter(String tableName) throws Exception {
        if (tableName.equals("") && list != null) {
            return "ok";
        } else if (list != null) {
            tableName = tableName.trim().split(" ")[1];
            String field = FieldMapping.getKylin("GAMECHANNELID", tableName);
            return field + " not in (" +
                    list.toString().substring(1, list.toString().length() - 1) +
                    ")";
        } else {
            return null;
        }
    }

    public static void setList(List<String> list) {
        FilterGameChannelId.list = list;
    }
}
