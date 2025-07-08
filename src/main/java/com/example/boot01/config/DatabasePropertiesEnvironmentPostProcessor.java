package com.example.boot01.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.util.Map;
import java.util.Properties;

/**
 * 애플리케이션 시작 시 DB에서 프로퍼티를 읽어 Spring Environment에 등록한다.
 */
public class DatabasePropertiesEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // DB 로더를 사용하여 프로퍼티 로드
        DatabasePropertiesLoader loader = new DatabasePropertiesLoader(environment);
        Map<String, Object> dbProperties = loader.loadProperties();

        if (dbProperties != null && !dbProperties.isEmpty()) {
            // 커스텀 PropertySource 생성
            PropertySource<?> databasePropertySource = new DatabasePropertySource(dbProperties);
            // Environment에 등록. 가장 높은 우선순위를 갖도록 마지막에 추가
            environment.getPropertySources().addLast(databasePropertySource);
        }
    }
}