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
            SchemaArray arrayNode = (SchemaArray) node;

            TypeRef elementType;

            // 1) 빈 배열만 관찰된 경우 → 원소 타입을 추론할 수 없으므로 Object
            if (arrayNode.elementTypes().isEmpty()) {
                elementType = new TypeRef("Object", Set.of(), false, false);

                // 2) 한 가지 타입만 관찰된 경우 → 그 타입을 재귀적으로 추론
            } else if (arrayNode.elementTypes().size() == 1) {
                SchemaNode singleElementSchema = arrayNode.elementTypes().iterator().next();
                path.addLast("Item");
                elementType = inferRecursive(singleElementSchema, suggestedClassName + "Item", path, acc);
                path.removeLast();

                // 3) 여러 타입이 섞여 있는 배열 → 1차 버전에서는 보수적으로 Object로 처리
            } else {
                elementType = new TypeRef("Object", Set.of(), false, false);
            }

            // List<T>로 감싸고 java.util.List import 필요 표시
            TypeRef listType = elementType.withGeneric("List", "java.util.List");
            acc.put(node, listType);
            return listType;
        }
        if (node instanceof SchemaObject) {
            SchemaObject objectNode = (SchemaObject) node;

            // 클래스 이름 결정
            String className = (suggestedClassName != null && !suggestedClassName.isBlank())
                    ? nameConverter.toPascalCase(suggestedClassName)
                    : buildClassNameFromPath(path);

            // 필드 순회: Map<String, FieldInfo>
            for (Map.Entry<String, SchemaObject.FieldInfo> entry : objectNode.fields().entrySet()) {
                String fieldName = entry.getKey();
                SchemaObject.FieldInfo fieldInfo = entry.getValue();
                SchemaNode fieldSchema = fieldInfo.schema();

                path.addLast(fieldName);

                // 자식 클래스 이름: 현재 클래스명 + 필드명(PascalCase)
                String childClassName = className + nameConverter.toPascalCase(fieldName);
                inferRecursive(fieldSchema, childClassName, path, acc);

                path.removeLast();
            }

            TypeRef typeRef = new TypeRef(className, Set.of(), true, false);
            acc.put(node, typeRef);
            return typeRef;
        }
        if (node instanceof SchemaUnion) {
            SchemaUnion unionNode = (SchemaUnion) node;

            // Set<SchemaNode> variants = unionNode.variants();
            Set<SchemaNode> variantSet = unionNode.variants();
            if (variantSet == null || variantSet.isEmpty()) {
                TypeRef typeRef = new TypeRef("Object", Set.of(), false, false);
                acc.put(node, typeRef);
                return typeRef;
            }

            // Set → List로 변환해서 인덱스 부여
            List<SchemaNode> variants = new ArrayList<>(variantSet);

            List<TypeRef> refs = new ArrayList<>();
            for (int i = 0; i < variants.size(); i++) {
                SchemaNode child = variants.get(i);
                path.addLast("Alt" + i);
                refs.add(inferRecursive(child, suggestedClassName + "Alt" + i, path, acc));
                path.removeLast();
            }

            TypeRef merged = mergeUnion(refs);
            acc.put(node, merged);
            return merged;
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

    /** Union 병합 규칙: 동일/숫자/리스트/문자열 혼합을 우선 처리, 불가하면 Object */
    private TypeRef mergeUnion(List<TypeRef> refs) {
        boolean allSame = refs.stream().map(TypeRef::getJavaType).distinct().count() == 1;
        if (allSame) return refs.get(0);

        boolean allList = refs.stream().allMatch(TypeRef::isList);
        if (allList) {
            List<String> elemTypes = new ArrayList<>();
            for (TypeRef r : refs) elemTypes.add(extractGenericArgument(r.getJavaType()));
            String mergedElem = mergeElementTypes(elemTypes);
            if (mergedElem != null) {
                Set<String> imps = new LinkedHashSet<>();
                for (TypeRef r : refs) imps.addAll(r.getRequiredImports());
                imps.add("java.util.List");
                return new TypeRef("List<" + mergedElem + ">", imps, false, true);
            }
        }

        if (refs.stream().allMatch(r -> isNumericLike(r.getJavaType()))) {
            return new TypeRef("Double", Set.of(), false, false);
        }

        boolean hasString = refs.stream().anyMatch(r -> "String".equals(r.getJavaType()));
        boolean onlyStringOrNullOrObject = refs.stream().allMatch(r ->
                "String".equals(r.getJavaType()) || "Object".equals(r.getJavaType()));
        if (hasString && onlyStringOrNullOrObject) {
            return new TypeRef("String", Set.of(), false, false);
        }

        boolean anyObject = refs.stream().anyMatch(TypeRef::isObject);
        if (anyObject) {
            return new TypeRef("Object", Set.of(), false, false);
        }

        return new TypeRef("Object", Set.of(), false, false);
    }

    private boolean isNumericLike(String t) {
        return "Integer".equals(t) || "Long".equals(t) || "Double".equals(t);
    }

    private String extractGenericArgument(String listType) {
        int lt = listType.indexOf('<');
        int gt = listType.lastIndexOf('>');
        if (lt >= 0 && gt > lt) return listType.substring(lt + 1, gt).trim();
        return null;
    }

    private String mergeElementTypes(List<String> elems) {
        if (elems.stream().filter(Objects::nonNull).distinct().count() == 1)
            return elems.get(0);

        if (elems.stream().allMatch(this::isNumericLike))
            return "Double";

        boolean hasString = elems.stream().anyMatch("String"::equals);
        boolean onlyStringOrNull = elems.stream().allMatch(t ->
                t == null || "String".equals(t) || "Object".equals(t));
        if (hasString && onlyStringOrNull) return "String";

        return "Object";
    }


    /** 경로 기반 클래스명 생성 */
    private String buildClassNameFromPath(Deque<String> path) {
        if (path == null || path.isEmpty()) return "AutoClass";
        StringBuilder sb = new StringBuilder();
        for (String p : path) sb.append(nameConverter.toPascalCase(p));
        return sb.toString();
    }

}
