package org.example.json;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * TypeInferencer (skeleton)
 *
 * JsonAnalyzer가 만든 SchemaNode 트리를 바탕으로 Java 타입을 추론한다.
 * 지금 커밋에서는 의존성 주입을 위한 기본 구조만 제공한다.
 */
public class TypeInferencer {

    // 의존성: 이름 변환 전략
    private final NameConverter nameConverter;

    public TypeInferencer(NameConverter nameConverter) {
        this.nameConverter = (nameConverter != null) ? nameConverter : new DefaultNameConverter();
    }

    public TypeInferencer() {
        this(new DefaultNameConverter());
    }

    // 공개 API 스텁 (다음 커밋에서 실제 로직 추가)
    public Map<SchemaNode, Object> inferTypes(SchemaNode root, String rootClassName) {
        if (root == null) throw new IllegalArgumentException("root node is null");
        if (rootClassName == null || rootClassName.isBlank()) rootClassName = "Root";
        return new IdentityHashMap<>();
    }
}
