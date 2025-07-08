package com.example.boot01.property.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface PropertyMapper {
    /**
     * 주어진 사이트와 프로파일에 해당하는 프로퍼티 목록을 조회한다.
     * 공통(SITE='COMMON') 프로퍼티와 해당 사이트/프로파일 프로퍼티를 모두 가져온다.
     * @param params site, profile
     * @return 프로퍼티 목록
     */
    List<Map<String, String>> findPropertiesBySiteAndProfile(Map<String, String> params);
}