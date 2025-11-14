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

    @Test
    void 배열_JSON은_SchemaArray와_원소_타입을_생성한다() throws Exception {
        String json = "{ \"values\": [1, 2, 3] }";
        JsonNode node = mapper.readTree(json);

        JsonAnalyzer analyzer = new JsonAnalyzer();
        SchemaNode root = analyzer.analyze(node);

        SchemaObject obj = (SchemaObject) root;
        SchemaObject.FieldInfo valuesInfo = obj.fields().get("values");
        SchemaNode valuesSchema = valuesInfo.schema();

        assertThat(valuesSchema).isInstanceOf(SchemaArray.class);
        SchemaArray array = (SchemaArray) valuesSchema;

        assertThat(array.elementTypes()).hasSize(1);
        SchemaNode elem = array.elementTypes().iterator().next();
        assertThat(elem).isInstanceOf(SchemaPrimitive.class);
        assertThat(((SchemaPrimitive) elem).pkind())
                .isEqualTo(SchemaPrimitive.PKind.NUMBER);
    }
}