package com.example.boot01.property.web;

import com.example.boot01.property.service.PropertyReloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyReloadService propertyReloadService;

    /**
     * 프로퍼티를 수동으로 다시 로드하는 API.
     * @return 성공/실패 메시지
     */
    @PostMapping("/property/reload")
    public ResponseEntity<String> reloadProperties() {
        boolean success = propertyReloadService.reloadProperties();
        if (success) {
            return ResponseEntity.ok("Properties reloaded successfully.");
        } else {
            return ResponseEntity.internalServerError().body("Failed to reload properties.");
        }
    }
}