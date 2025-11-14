package org.example.json;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

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
}
