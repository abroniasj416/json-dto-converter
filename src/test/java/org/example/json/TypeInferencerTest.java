package org.example.json;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TypeInferencerTest {
    private final TypeInferencer inferencer = new TypeInferencer();

    @Test
    void 프리미티브_STRING은_String으로_매핑된다() {
        SchemaPrimitive primitive = new SchemaPrimitive(SchemaPrimitive.PKind.STRING);

        Map<SchemaNode, TypeInferencer.TypeRef> map = inferencer.inferTypes(primitive, "Root");
        TypeInferencer.TypeRef ref = map.get(primitive);

        assertThat(ref.getJavaType()).isEqualTo("String");
        assertThat(ref.isObject()).isFalse();
        assertThat(ref.isList()).isFalse();
    }

    @Test
    void 숫자_배열은_List_Double_로_매핑된다() {
        SchemaArray array = new SchemaArray();
        array.elementTypes().add(new SchemaPrimitive(SchemaPrimitive.PKind.NUMBER));

        Map<SchemaNode, TypeInferencer.TypeRef> map = inferencer.inferTypes(array, "Numbers");
        TypeInferencer.TypeRef ref = map.get(array);

        assertThat(ref.getJavaType()).isEqualTo("List<Double>");
        assertThat(ref.isList()).isTrue();
        assertThat(ref.getRequiredImports()).contains("java.util.List");
    }
}