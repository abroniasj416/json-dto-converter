package org.example.json;

import java.util.*;

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

    /**
     * 추론된 Java 타입을 표현하는 값 객체.
     * 예: "String", "Integer", "List<Article>", "WeatherApiResponse"
     */
    public static class TypeRef {
        private final String javaType;
        private final Set<String> requiredImports;
        private final boolean isObject;
        private final boolean isList;

        public TypeRef(String javaType, Set<String> requiredImports, boolean isObject, boolean isList) {
            this.javaType = javaType;
            this.requiredImports = (requiredImports == null)
                    ? new LinkedHashSet<>()
                    : new LinkedHashSet<>(requiredImports);
            this.isObject = isObject;
            this.isList = isList;
        }

        public String getJavaType() { return javaType; }
        public Set<String> getRequiredImports() { return Collections.unmodifiableSet(requiredImports); }
        public boolean isObject() { return isObject; }
        public boolean isList() { return isList; }

        /** 예: elem.withGeneric("List", "List") → List<T> */
        public TypeRef withGeneric(String outer, String importFqcn) {
            Set<String> imps = new LinkedHashSet<>(requiredImports);
            if (importFqcn != null && !importFqcn.isEmpty()) imps.add(importFqcn);
            return new TypeRef(outer + "<" + javaType + ">", imps, false, true);
        }

        @Override public String toString() { return javaType; }
    }

    public Map<SchemaNode, TypeRef> inferTypes(SchemaNode root, String rootClassName) {
        if (root == null) throw new IllegalArgumentException("root node is null");
        if (rootClassName == null || rootClassName.isBlank()) rootClassName = "Root";
        Map<SchemaNode, TypeRef> result = new IdentityHashMap<>();
        Deque<String> path = new ArrayDeque<>();
        inferRecursive(root, new DefaultNameConverter().toPascalCase(rootClassName), path, result);
        return result;
    }

    private TypeRef inferRecursive(SchemaNode node, String suggestedClassName,
                                   Deque<String> path, Map<SchemaNode, TypeRef> acc) {
        if (node instanceof SchemaPrimitive) {
            TypeRef t = inferPrimitive((SchemaPrimitive) node);
            acc.put(node, t);
            return t;
        }
        if (node instanceof SchemaArray) {
            // 다음 커밋에서 구현
            TypeRef t = new TypeRef("Object", Set.of(), false, false);
            acc.put(node, t);
            return t;
        }
        if (node instanceof SchemaObject) {
            // 다음 커밋에서 구현
            TypeRef t = new TypeRef("Object", Set.of(), false, false);
            acc.put(node, t);
            return t;
        }
        if (node instanceof SchemaUnion) {
            // 다음 커밋에서 구현
            TypeRef t = new TypeRef("Object", Set.of(), false, false);
            acc.put(node, t);
            return t;
        }
        TypeRef t = new TypeRef("Object", Set.of(), false, false);
        acc.put(node, t);
        return t;
    }

    /** Primitive 타입 매핑 */
    private TypeRef inferPrimitive(SchemaPrimitive primitiveNode) {
        SchemaPrimitive.PKind primitiveKind = primitiveNode.pkind();
        if (primitiveKind == null) {
            return new TypeRef("Object", Set.of(), false, false);
        }

        switch (primitiveKind) {
            case STRING:
                return new TypeRef("String", Set.of(), false, false);
            case BOOLEAN:
                return new TypeRef("Boolean", Set.of(), false, false);
            case NUMBER:
                // JSON의 number는 정수/실수를 모두 포함하므로 보수적으로 Double로 매핑
                return new TypeRef("Double", Set.of(), false, false);
            case NULL:
                // null만 있는 경우에는 타입을 특정할 수 없으므로 Object로 처리
                return new TypeRef("Object", Set.of(), false, false);
            default:
                return new TypeRef("Object", Set.of(), false, false);
        }
    }



}
