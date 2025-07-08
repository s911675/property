package com.example.boot01.config;

import org.springframework.core.env.MapPropertySource;

import java.util.Map;

public class DatabasePropertySource extends MapPropertySource {
    public static final String PROPERTY_SOURCE_NAME = "databaseProperties";

    public DatabasePropertySource(Map<String, Object> source) {
        super(PROPERTY_SOURCE_NAME, source);
    }
}