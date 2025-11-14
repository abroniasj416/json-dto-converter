package org.example.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

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

    @Test
    void 혼합_타입_배열은_유니온으로_표현된다() throws Exception {
        String json = "{ \"values\": [1, \"two\", 3] }";
        JsonNode node = mapper.readTree(json);

        JsonAnalyzer analyzer = new JsonAnalyzer();
        SchemaNode root = analyzer.analyze(node);

        SchemaObject obj = (SchemaObject) root;
        SchemaArray array = (SchemaArray) obj.fields().get("values").schema();

        // elementTypes 안에 들어 있는 노드들은 모두 Primitive여야 한다
        Set<SchemaPrimitive.PKind> kinds = new HashSet<>();
        for (SchemaNode n : array.elementTypes()) {
            assertThat(n).isInstanceOf(SchemaPrimitive.class);
            kinds.add(((SchemaPrimitive) n).pkind());
        }

        // NUMBER와 STRING 두 종류가 존재하는지만 확인
        assertThat(kinds)
                .containsExactlyInAnyOrder(
                        SchemaPrimitive.PKind.NUMBER,
                        SchemaPrimitive.PKind.STRING
                );
    }
}