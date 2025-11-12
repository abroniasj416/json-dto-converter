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

    public SchemaNode analyze(JsonValidator.Result result) {
        if (result == null || result.root() == null) {
            return new SchemaPrimitive(SchemaPrimitive.PKind.NULL);
        }
        return analyze(result.root());
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

    /**
     * 두 스키마 노드를 병합하여 변동성을 포착한다.
     * - Primitive/Primitive: 종류가 다르면 Union
     * - Object/Object: 필드 단위로 present/total 합산 및 재귀 병합
     * - Array/Array: elementTypes 합집합(1차 버전 단순화)
     * - 서로 다른 종류: Union으로 승격
     */
    private SchemaNode mergeSchemas(SchemaNode a, SchemaNode b) {
        if (a == null) return b;
        if (b == null) return a;

        // Primitive <--> Primitive
        if (a instanceof SchemaPrimitive && b instanceof SchemaPrimitive) {
            SchemaPrimitive pa = (SchemaPrimitive) a;
            SchemaPrimitive pb = (SchemaPrimitive) b;
            return (pa.pkind() == pb.pkind()) ? pa : unionOf(pa, pb);
        }

        if (a instanceof SchemaObject && b instanceof SchemaObject) {
            return mergeObjects((SchemaObject) a, (SchemaObject) b);
        }

        if (a instanceof SchemaArray && b instanceof SchemaArray) {
            return mergeArrays((SchemaArray) a, (SchemaArray) b);
        }

        if (a instanceof SchemaUnion) {
            ((SchemaUnion) a).addVariant(b);
            return a;
        }
        if (b instanceof SchemaUnion) {
            ((SchemaUnion) b).addVariant(a);
            return b;
        }

        return unionOf(a, b);
    }

    private SchemaNode mergeObjects(SchemaObject left, SchemaObject right) {
        SchemaObject merged = new SchemaObject();

        java.util.Set<String> rightNames = new java.util.HashSet<>(right.fields().keySet());

        for (java.util.Map.Entry<String, SchemaObject.FieldInfo> e : left.fields().entrySet()) {
            String name = e.getKey();
            SchemaObject.FieldInfo lf = e.getValue();

            if (right.fields().containsKey(name)) {
                SchemaObject.FieldInfo rf = right.fields().get(name);
                SchemaNode mergedSchema = mergeSchemas(lf.schema(), rf.schema());
                int present = lf.presentCount() + rf.presentCount();
                int total = lf.totalSamples() + rf.totalSamples();
                merged.fields().put(name, new SchemaObject.FieldInfo(mergedSchema, present, total));
                rightNames.remove(name);
            } else {
                int rightTotal = estimateObjectTotalSamples(right);
                int present = lf.presentCount();
                int total = lf.totalSamples() + rightTotal;
                merged.fields().put(name, new SchemaObject.FieldInfo(lf.schema(), present, total));
            }
        }

        int leftTotal = estimateObjectTotalSamples(left);
        int rightTotal = estimateObjectTotalSamples(right);
        for (String name : rightNames) {
            SchemaObject.FieldInfo rf = right.fields().get(name);
            int present = rf.presentCount();
            int total = leftTotal + rightTotal;
            merged.fields().put(name, new SchemaObject.FieldInfo(rf.schema(), present, total));
        }

        return merged;
    }

    /** 객체의 관찰된 총 샘플 수 추정: 필드들의 totalSamples 중 최댓값 사용(안전장치 포함) */
    private int estimateObjectTotalSamples(SchemaObject obj) {
        int max = 0;
        for (SchemaObject.FieldInfo fi : obj.fields().values()) {
            if (fi.totalSamples() > max) {
                max = fi.totalSamples();
            }
        }
        if (max == 0) {
            return 1; // 필드가 비어있거나 0으로만 기록된 경우: 최소 1회 관찰로 가정
        }
        return max;
    }

    private SchemaNode mergeArrays(SchemaArray left, SchemaArray right) {
        SchemaArray merged = new SchemaArray();
        merged.setEmpty(left.isEmpty() || right.isEmpty());
        merged.elementTypes().addAll(left.elementTypes());
        merged.elementTypes().addAll(right.elementTypes());
        return merged;
    }

    private SchemaUnion unionOf(SchemaNode x, SchemaNode y) {
        SchemaUnion u = new SchemaUnion();
        u.addVariant(x);
        u.addVariant(y);
        return u;
    }
}
