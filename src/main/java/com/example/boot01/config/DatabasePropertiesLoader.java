package com.example.boot01.config;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertyResolver;
import org.springframework.jndi.JndiTemplate;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Spring 컨텍스트 로딩 전 DB에서 프로퍼티를 로드하는 유틸리티 클래스.
 * JNDI를 우선 시도하고, 실패 시 JDBC 직접 연결을 사용한다.
 */
public class DatabasePropertiesLoader {

    private static final Logger log = LoggerFactory.getLogger(DatabasePropertiesLoader.class);

    private final PropertyResolver propertyResolver;
    private final SqlSessionFactory sqlSessionFactory;

    public DatabasePropertiesLoader(PropertyResolver propertyResolver) {
        this.propertyResolver = propertyResolver;
        this.sqlSessionFactory = buildSqlSessionFactory();
    }

    private DataSource getDataSource() {
        // 1. JNDI 데이터소스 조회 시도
        String jndiName = propertyResolver.getProperty("spring.datasource.jndi-name");
        if (jndiName != null && !jndiName.isEmpty()) {
            try {
                log.info("Attempting to look up JNDI DataSource: {}", jndiName);
                return new JndiTemplate().lookup(jndiName, DataSource.class);
            } catch (NamingException e) {
                log.warn("JNDI DataSource not found (name: {}), falling back to JDBC.", jndiName);
            }
        }

        // 2. JNDI 실패 시 JDBC 직접 연결 데이터소스 생성
        log.info("Creating DataSource using JDBC properties.");
        com.zaxxer.hikari.HikariDataSource ds = new com.zaxxer.hikari.HikariDataSource();
        ds.setJdbcUrl(propertyResolver.getProperty("spring.datasource.url"));
        ds.setUsername(propertyResolver.getProperty("spring.datasource.username"));
        ds.setPassword(propertyResolver.getProperty("spring.datasource.password"));
        ds.setDriverClassName(propertyResolver.getProperty("spring.datasource.driver-class-name"));
        return ds;
    }

    private SqlSessionFactory buildSqlSessionFactory() {
        try {
            DataSource dataSource = getDataSource();
            TransactionFactory transactionFactory = new JdbcTransactionFactory();
            Environment environment = new Environment("database-properties", transactionFactory, dataSource);
            Configuration configuration = new Configuration(environment);
            // MyBatis 매퍼 추가
            configuration.addMappers("kr.go.seoul.lwis.bo.tr.sv.mapper");
            return new SqlSessionFactoryBuilder().build(configuration);
        } catch (Exception e) {
            // fail-fast: DataSource 생성 실패 시 예외를 던져 애플리케이션 중단
            throw new RuntimeException("Failed to create SqlSessionFactory for property loading", e);
        }
    }

    /**
     * DB에서 프로퍼티를 조회하여 Map 형태로 반환한다.
     * @return DB에서 읽어온 프로퍼티 맵
     */
    public Map<String, Object> loadProperties() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            String site = propertyResolver.getProperty("config.db.property.site", "COMMON");
            String profile = propertyResolver.getProperty("spring.profiles.active", "default");

            Map<String, String> params = new HashMap<>();
            params.put("site", site);
            params.put("profile", profile);

            log.info("Loading properties from DB for site='{}' and profile='{}'", site, profile);

            // 공통 프로퍼티와 활성 프로파일 프로퍼티를 모두 조회
            List<Map<String, String>> properties = session.selectList("kr.go.seoul.lwis.bo.tr.sv.mapper.PropertyMapper.findPropertiesBySiteAndProfile", params);

            if (properties.isEmpty()) {
                log.warn("No properties found in DB for site='{}' and profile='{}'", site, profile);
                return Collections.emptyMap();
            }

            Map<String, Object> resultMap = properties.stream()
                    .collect(Collectors.toMap(
                            prop -> prop.get("PROPERTY_KEY"),
                            prop -> prop.get("PROPERTY_VALUE")
                    ));

            log.info("Loaded {} properties from database.", resultMap.size());
            return resultMap;

        } catch (Exception e) {
            // fail-fast: DB 프로퍼티 로딩 실패 시 예외를 던져 애플리케이션 중단
            log.error("Could not load properties from database.", e);
            throw new RuntimeException("Could not load properties from database. Aborting application startup.", e);
        }
    }
}