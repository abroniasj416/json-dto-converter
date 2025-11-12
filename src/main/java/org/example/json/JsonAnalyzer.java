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
}
