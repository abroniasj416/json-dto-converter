package org.example.json;

/**
 * 관찰 기반 스키마의 최상위 추상 타입.
 * 실제 값(JsonNode)이 아니라 "형태"와 "변동성"을 표현한다.
 */
public abstract class SchemaNode {

    public enum Kind { OBJECT, ARRAY, PRIMITIVE, UNION }

    private final Kind kind;

    protected SchemaNode(Kind kind) {
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }
}