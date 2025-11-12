package org.example.json;

/**
 * 원시 타입 스키마. 실제 값이 아니라 "문자열/숫자/불리언/null" 같은 종류를 담는다.
 */
public final class SchemaPrimitive extends SchemaNode {

    public enum PKind { STRING, NUMBER, BOOLEAN, NULL }

    private final PKind pkind;

    public SchemaPrimitive(PKind pkind) {
        super(Kind.PRIMITIVE);
        this.pkind = pkind;
    }

    public PKind pkind() {
        return pkind;
    }
}
