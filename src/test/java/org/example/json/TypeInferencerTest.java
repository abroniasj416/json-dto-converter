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

    @Test
    void 객체는_클래스명으로_매핑된다() {
        SchemaObject obj = new SchemaObject();
        obj.fields().put("name",
                SchemaObject.FieldInfo.presentOnce(new SchemaPrimitive(SchemaPrimitive.PKind.STRING)));

        Map<SchemaNode, TypeInferencer.TypeRef> map = inferencer.inferTypes(obj, "Person");
        TypeInferencer.TypeRef ref = map.get(obj);

        assertThat(ref.getJavaType()).isEqualTo("Person");
        assertThat(ref.isObject()).isTrue();
        assertThat(ref.isList()).isFalse();
    }

    @Test
    void 유니온_숫자타입들은_Double로_병합된다() {
        SchemaUnion union = new SchemaUnion();
        union.addVariant(new SchemaPrimitive(SchemaPrimitive.PKind.NUMBER)); // Double
        union.addVariant(new SchemaPrimitive(SchemaPrimitive.PKind.NUMBER)); // 동일 타입만 있는 단순 케이스이지만 규칙 확인용

        Map<SchemaNode, TypeInferencer.TypeRef> map = inferencer.inferTypes(union, "Value");
        TypeInferencer.TypeRef ref = map.get(union);

        assertThat(ref.getJavaType()).isEqualTo("Double");
    }

    @Test
    void 유니온_String과_Object는_String으로_병합된다() {
        SchemaUnion union = new SchemaUnion();
        union.addVariant(new SchemaPrimitive(SchemaPrimitive.PKind.STRING));
        union.addVariant(new SchemaPrimitive(SchemaPrimitive.PKind.NULL)); // NULL → Object 처리

        Map<SchemaNode, TypeInferencer.TypeRef> map = inferencer.inferTypes(union, "MaybeString");
        TypeInferencer.TypeRef ref = map.get(union);

        assertThat(ref.getJavaType()).isEqualTo("String");
    }

}