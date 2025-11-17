package org.example.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.exception.UserException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

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
        assertThat(result.hadBom()).isFalse();
    }

    @Test
    void 루트가_객체나_배열이_아니면_예외가_발생한다() throws Exception {
        Path temp = Files.createTempFile("invalid-root-", ".json");
        // 루트가 문자열
        Files.writeString(temp, "\"just string\"", StandardCharsets.UTF_8);

        assertThatThrownBy(() -> JsonValidator.validateAndLoad(temp.toString()))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("루트 타입이 객체 또는 배열이어야 합니다");
    }

    @Test
    void 유효하지_않은_JSON이면_예외가_발생한다() throws Exception {
        Path temp = Files.createTempFile("broken-json-", ".json");
        Files.writeString(temp, "{ invalid json", StandardCharsets.UTF_8);

        assertThatThrownBy(() -> JsonValidator.validateAndLoad(temp.toString()))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("유효한 JSON이 아닙니다");
    }

    @Test
    void 존재하지_않는_파일이면_예외가_발생한다() {
        String nonexistent = "this/file/does/not/exist.json";

        assertThatThrownBy(() -> JsonValidator.validateAndLoad(nonexistent))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("존재하지 않거나 파일이 아닙니다");
    }

    @Test
    void UTF8_BOM이_있는_JSON도_정상적으로_처리한다() throws Exception {
        Path temp = Files.createTempFile("bom-json-", ".json");
        String json = "{\"name\":\"Bob\",\"age\":30}";
        byte[] raw = json.getBytes(StandardCharsets.UTF_8);

        byte[] withBom = new byte[3 + raw.length];
        withBom[0] = (byte) 0xEF;
        withBom[1] = (byte) 0xBB;
        withBom[2] = (byte) 0xBF;
        System.arraycopy(raw, 0, withBom, 3, raw.length);

        Files.write(temp, withBom);

        JsonValidator.Result result = JsonValidator.validateAndLoad(temp.toString());

        assertThat(result.hadBom()).isTrue();
        assertThat(result.root().get("name").asText()).isEqualTo("Bob");
    }

    @Test
    void 파일_크기가_5MB를_초과하면_예외가_발생한다() throws Exception {
        Path temp = Files.createTempFile("large-json-", ".json");
        byte[] large = new byte[(int) (5 * 1024 * 1024) + 1]; // 5MB + 1byte
        Arrays.fill(large, (byte) 'a'); // 유효 JSON일 필요는 없음, 사이즈 체크가 먼저

        Files.write(temp, large);

        assertThatThrownBy(() -> JsonValidator.validateAndLoad(temp.toString()))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("파일 크기가 너무 큽니다");
    }

    @Test
    void 문자열_JSON_파싱_유틸리티에서_유효하지_않은_JSON이면_예외가_발생한다() {
        String broken = "{ invalid json";

        assertThatThrownBy(() -> JsonValidator.assertValidAndParse(broken, "inline"))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("유효한 JSON이 아닙니다");
    }
}
