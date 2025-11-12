package org.example.json;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 서로 다른 스키마들의 합집합을 표현한다.
 * 예: 숫자와 문자열이 섞여 관찰된 경우, NULL과 비-NULL이 섞인 경우 등.
 */
public final class SchemaUnion extends SchemaNode {

    private final Set<SchemaNode> variants = new LinkedHashSet<>();

    public SchemaUnion() {
        super(Kind.UNION);
    }

    /** 유니온에 스키마 변형을 추가한다(중복은 Set으로 자동 제거). */
    public void addVariant(SchemaNode node) {
        if (node == null) return;
        // 간단 구현: 중첩 Union이 들어오면 납작하게 펴 주는 편의 처리
        if (node instanceof SchemaUnion) {
            variants.addAll(((SchemaUnion) node).variants());
        } else {
            variants.add(node);
        }
    }

    public Set<SchemaNode> variants() {
        return variants;
    }
}
