package org.example.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JsonAnalyzerTest {
    private final ObjectMapper mapper = new ObjectMapper();
    
    @Test
    void 단순_객체_JSON을_SchemaObject로_분석한다() throws Exception {
        String json = "{ \"name\": \"Alice\", \"age\": 20 }";
        JsonNode node = mapper.readTree(json);

        JsonAnalyzer analyzer = new JsonAnalyzer();
        SchemaNode root = analyzer.analyze(node);

        assertThat(root).isInstanceOf(SchemaObject.class);
        SchemaObject obj = (SchemaObject) root;

        assertThat(obj.fields()).containsKeys("name", "age");

        SchemaObject.FieldInfo nameInfo = obj.fields().get("name");
        assertThat(nameInfo.optional()).isFalse();
        assertThat(nameInfo.schema()).isInstanceOf(SchemaPrimitive.class);
        assertThat(((SchemaPrimitive) nameInfo.schema()).pkind())
                .isEqualTo(SchemaPrimitive.PKind.STRING);

        SchemaObject.FieldInfo ageInfo = obj.fields().get("age");
        assertThat(ageInfo.schema()).isInstanceOf(SchemaPrimitive.class);
        assertThat(((SchemaPrimitive) ageInfo.schema()).pkind())
                .isEqualTo(SchemaPrimitive.PKind.NUMBER);
    }
}