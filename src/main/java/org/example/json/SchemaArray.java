package org.example.json;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 배열 스키마. 원소 타입들의 "합집합"을 보관한다(혼합 타입 허용).
 * 빈 배열이 관찰되었는지 여부도 보관한다.
 */
public final class SchemaArray extends SchemaNode {

    // 원소 스키마들의 합집합(동일 구조 중복 제거는 1차 버전에선 보수적으로 참조 동일성 기준)
    private final Set<SchemaNode> elementTypes = new LinkedHashSet<>();

    // 관찰 시에 빈 배열이 등장했는지 여부
    private boolean empty;

    public SchemaArray() {
        super(Kind.ARRAY);
    }

    public Set<SchemaNode> elementTypes() {
        return elementTypes;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }
}
