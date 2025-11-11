package org.example.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonValidator {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonValidator() {}

    // 문자열 JSON 유효성 검증 + 파싱 (파일 I/O 없음)
    public static JsonNode assertValidAndParse(String json, String sourceNameForMsg) {
        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("[ERROR] --input 파일이 유효한 JSON이 아닙니다: " + sourceNameForMsg);
        }
    }

    /**
     * 파일의 시작 3바이트가 UTF-8 BOM(0xEF 0xBB 0xBF)인지 확인한다.
     * BOM(Byte Order Mark)은 UTF-8 인코딩을 표시하는 마커로,
     * 존재할 경우 JSON 파싱 전에 제거가 필요하다.
     */
    private static boolean hasUtf8Bom(byte[] bytes) {
        return bytes.length >= 3 &&
                (bytes[0] & 0xFF) == 0xEF &&
                (bytes[1] & 0xFF) == 0xBB &&
                (bytes[2] & 0xFF) == 0xBF;
    }
}
