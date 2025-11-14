package org.example.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonValidatorTest {

    @Test
    void 유효한_JSON_파일을_성공적으로_로딩한다() throws Exception {
        Path temp = Files.createTempFile("valid-json-", ".json");
        Files.writeString(temp, "{\"name\":\"Alice\",\"age\":20}", StandardCharsets.UTF_8);

        JsonValidator.Result result = JsonValidator.validateAndLoad(temp.toString());

        JsonNode root = result.root();
        assertThat(root.isObject()).isTrue();
        assertThat(root.get("name").asText()).isEqualTo("Alice");
        assertThat(result.sizeBytes()).isGreaterThan(0);
    }

    @Test
    void 루트가_객체나_배열이_아니면_예외가_발생한다() throws Exception {
        Path temp = Files.createTempFile("invalid-root-", ".json");
        // 루트가 문자열
        Files.writeString(temp, "\"just string\"", StandardCharsets.UTF_8);

        assertThatThrownBy(() -> JsonValidator.validateAndLoad(temp.toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("루트 타입이 객체 또는 배열이어야 합니다");
    }

    @Test
    void 유효하지_않은_JSON이면_예외가_발생한다() throws Exception {
        Path temp = Files.createTempFile("broken-json-", ".json");
        Files.writeString(temp, "{ invalid json", StandardCharsets.UTF_8);

        assertThatThrownBy(() -> JsonValidator.validateAndLoad(temp.toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효한 JSON이 아닙니다");
    }

    @Test
    void 존재하지_않는_파일이면_예외가_발생한다() {
        String nonexistent = "this/file/does/not/exist.json";

        assertThatThrownBy(() -> JsonValidator.validateAndLoad(nonexistent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않거나 파일이 아닙니다");
    }
}
