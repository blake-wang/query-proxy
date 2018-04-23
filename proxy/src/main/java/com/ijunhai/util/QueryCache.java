package com.ijunhai.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class QueryCache {
    private static Cache<String, ResultSet> queryCache;


    public QueryCache() {
        queryCache = CacheBuilder.newBuilder()
                .expireAfterAccess(1, TimeUnit.DAYS)
                .maximumSize(10000)
                .removalListener(new RemovalListener<String, ResultSet>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, ResultSet> notification) {
                        notification.getValue();
                    }
                })
                .build();
    }

    public static Cache<String, ResultSet> getCache() {
        return queryCache;
    }
}
