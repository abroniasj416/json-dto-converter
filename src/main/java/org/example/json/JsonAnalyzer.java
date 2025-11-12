package org.example.json;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Jackson JsonNode 트리를 관찰 기반 스키마(중간 모델)로 변환한다.
 */
public final class JsonAnalyzer {

    /** JsonNode 루트를 스키마 트리로 변환한다. */
    public SchemaNode analyze(JsonNode root) {
        if (root == null) {
            return new SchemaPrimitive(SchemaPrimitive.PKind.NULL);
        }
        return visit(root);
    }

    /** 내부 방문 함수(1차: 프리미티브만 처리) */
    private SchemaNode visit(JsonNode n) {
        if (n.isObject()) {
            return visitObject(n);
        }

        if (n.isArray()) {
            return visitArray(n);
        }

        if (n.isTextual())  return new SchemaPrimitive(SchemaPrimitive.PKind.STRING);
        if (n.isNumber())   return new SchemaPrimitive(SchemaPrimitive.PKind.NUMBER);
        if (n.isBoolean())  return new SchemaPrimitive(SchemaPrimitive.PKind.BOOLEAN);
        if (n.isNull())     return new SchemaPrimitive(SchemaPrimitive.PKind.NULL);

        // object/array 등은 다음 커밋에서 처리
        return new SchemaPrimitive(SchemaPrimitive.PKind.STRING);
    }

    private SchemaNode visitObject(JsonNode obj) {
        SchemaObject so = new SchemaObject();
        java.util.Iterator<String> it = obj.fieldNames();
        while (it.hasNext()) {
            String name = it.next();
            JsonNode child = obj.get(name);
            SchemaNode childSchema = visit(child);
            so.fields().put(name, SchemaObject.FieldInfo.presentOnce(childSchema));
        }
        return so;
    }

    private SchemaNode visitArray(JsonNode arr) {
        SchemaArray sa = new SchemaArray();
        if (arr.size() == 0) {
            sa.setEmpty(true);
            return sa;
        }

        SchemaNode acc = null;
        for (JsonNode elem : arr) {
            SchemaNode elemSchema = visit(elem);
            acc = (acc == null) ? elemSchema : mergeSchemas(acc, elemSchema);
        }

        if (acc instanceof SchemaUnion) {
            sa.elementTypes().addAll(((SchemaUnion) acc).variants());
        } else {
            sa.elementTypes().add(acc);
        }
        return sa;
    }

    /** 스키마 병합 스텁 */
    // TODO : 구체화
    private SchemaNode mergeSchemas(SchemaNode a, SchemaNode b) {
        if (a == null) return b;
        if (b == null) return a;
        return a; // 임시: 다음 커밋에서 실제 규칙 구현
    }
}
