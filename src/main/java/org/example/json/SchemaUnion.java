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

    public Set<SchemaNode> variants() {
        return variants;
    }
}
