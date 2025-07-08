package com.example.boot01.property.service;

import com.example.boot01.config.DatabasePropertySource;
import com.example.boot01.property.mapper.PropertyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyReloadService {

    private final ConfigurableEnvironment environment;
    private final PropertyMapper propertyMapper;

    /**
     * DB에서 프로퍼티를 다시 로드하여 Environment를 갱신한다.
     * @return 성공 여부
     */
    public boolean reloadProperties() {
        log.info("Attempting to reload properties from database...");
        try {
            // 현재 환경의 site와 profile 정보 가져오기
            String site = environment.getProperty("config.db.property.site", "COMMON");
            String profile = environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "default";

            Map<String, String> params = new HashMap<>();
            params.put("site", site);
            params.put("profile", profile);

            List<Map<String, String>> properties = propertyMapper.findPropertiesBySiteAndProfile(params);

            if (properties.isEmpty()) {
                log.warn("No properties found in DB for site='{}' and profile='{}' during reload.", site, profile);
            }

            Map<String, Object> newProperties = properties.stream()
                    .collect(Collectors.toMap(
                            prop -> prop.get("PROPERTY_KEY"),
                            prop -> prop.get("PROPERTY_VALUE")
                    ));

            // Environment의 PropertySource 갱신
            MutablePropertySources propertySources = environment.getPropertySources();
            // 기존에 있다면 교체, 없다면 새로 추가
            if (propertySources.contains(DatabasePropertySource.PROPERTY_SOURCE_NAME)) {
                propertySources.replace(DatabasePropertySource.PROPERTY_SOURCE_NAME, new DatabasePropertySource(newProperties));
            } else {
                propertySources.addLast(new DatabasePropertySource(newProperties));
            }

            log.info("Successfully reloaded {} properties from database.", newProperties.size());
            return true;
        } catch (Exception e) {
            log.error("Failed to reload properties from database. Keeping existing properties.", e);
            // 실패 시 기존 프로퍼티 유지 (요구사항)
            return false;
        }
    }
}