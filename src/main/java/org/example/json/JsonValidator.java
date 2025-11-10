package org.example.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonValidator {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonValidator() {}

    // 유효하지 않으면 IllegalArgumentException 던짐, 필요 시 JsonNode 반환
    public static JsonNode assertValidAndParse(String json, String sourceNameForMsg) {
        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("[ERROR] --input 파일이 유효한 JSON이 아닙니다: " + sourceNameForMsg);
        }
    }
}
