package com.example.boot01.property.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PropertyReloadScheduler {

    private final PropertyReloadService propertyReloadService;

    /**
     * 설정된 cron 주기에 따라 프로퍼티 리로드를 실행한다.
     * 기본값은 5분 간격. (config.property.reload.cron 프로퍼티로 제어 가능)
     */
    @Scheduled(cron = "${config.property.reload.cron:0 */5 * * * *}")
    public void schedulePropertyReload() {
        log.debug("Executing scheduled property reload.");
        propertyReloadService.reloadProperties();
    }
}