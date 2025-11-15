package org.example.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.exception.InternalException;
import org.example.exception.UserException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class JsonValidator {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private JsonValidator() {
    }

    // CLI --input 파일 전용: 파일 검사 + UTF-8/BOM 처리 + JSON 파싱 + 루트 타입 확인
    public static Result validateAndLoad(String inputPath) {
        Path path = Paths.get(inputPath).toAbsolutePath().normalize();

        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new UserException("[ERROR] --input 경로가 존재하지 않거나 파일이 아닙니다: " + path);
        }
        if (!Files.isReadable(path)) {
            throw new UserException("[ERROR] --input 파일을 읽을 수 없습니다: " + path);
        }

        try {
            long size = Files.size(path);
            if (size > MAX_FILE_SIZE) {
                throw new UserException("[ERROR] --input 파일 크기가 너무 큽니다(최대 5MB): " + path);
            }

            byte[] bytes = Files.readAllBytes(path);
            boolean hadBom = hasUtf8Bom(bytes);
            String json = new String(hadBom ? stripBom(bytes) : bytes, StandardCharsets.UTF_8);

            JsonNode root = assertValidAndParse(json, path.toString());
            if (!root.isObject() && !root.isArray()) {
                throw new UserException("[ERROR] 루트 타입이 객체 또는 배열이어야 합니다: " + path);
            }

            return new Result(root, hadBom, size);
        } catch (IOException e) {
            // 파일 읽기 중의 I/O 오류는 내부 문제로 보고 감싼다
            throw new InternalException("입력 파일을 읽는 중 내부 오류가 발생했습니다: " + path, e);
        }
    }

    // 문자열 JSON 유효성 검증 + 파싱 (파일 I/O 없음)
    public static JsonNode assertValidAndParse(String json, String sourceNameForMsg) {
        try {
            return MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new UserException("[ERROR] --input 파일이 유효한 JSON이 아닙니다: " + sourceNameForMsg, e);
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

    /**
     * UTF-8 BOM(0xEF 0xBB 0xBF)이 존재할 경우
     * 해당 3바이트를 제거한 새로운 바이트 배열을 반환한다.
     * BOM이 없으면 원본 배열을 그대로 반환한다.
     */
    private static byte[] stripBom(byte[] bytes) {
        if (!hasUtf8Bom(bytes)) return bytes;
        byte[] result = new byte[bytes.length - 3];
        System.arraycopy(bytes, 3, result, 0, result.length);
        return result;
    }

    // 결과 DTO
    public static class Result {
        private final JsonNode root;
        private final boolean hadBom;
        private final long sizeBytes;

        public Result(JsonNode root, boolean hadBom, long sizeBytes) {
            this.root = root;
            this.hadBom = hadBom;
            this.sizeBytes = sizeBytes;
        }

        public JsonNode root() {
            return root;
        }

        public boolean hadBom() {
            return hadBom;
        }

        public long sizeBytes() {
            return sizeBytes;
        }
    }
}
