package org.example.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.example.json.SchemaObject.FieldInfo;
import org.example.json.TypeInferencer.TypeRef;

/**
 * ModelGraph
 *
 * <p>JSON 분석/타입 추론이 끝난 뒤, 실제로 생성할 Java 모델 클래스들을
 * 그래프 형태로 표현하는 불변 객체이다.</p>
 *
 * <p>노드(Node)는 Java 클래스(ModelClass)에 해당하고,
 * 엣지(Edge)는 "필드가 어떤 타입을 참조하는지" 정보로 표현된다.
 * 예를 들어, WeatherApiResponse.location 필드의 타입이 Location 이라면
 * WeatherApiResponse -> Location 이라는 관계가 생긴다.</p>
 *
 * <p>이 계층은 오직 "출력 세계(Java 타입들)의 구조"에만 관심을 가지며,
 * JSON 스키마나 타입 추론 내부 구현에는 의존하지 않는다.</p>
 */
public final class ModelGraph {

    private final ModelClass rootClass;
    private final Map<String, ModelClass> classesByQualifiedName;

    private ModelGraph(ModelClass rootClass, Map<String, ModelClass> classesByQualifiedName) {
        this.rootClass = Objects.requireNonNull(rootClass, "rootClass must not be null");
        Map<String, ModelClass> copy = new LinkedHashMap<>(
                Objects.requireNonNull(classesByQualifiedName, "classesByQualifiedName must not be null")
        );
        this.classesByQualifiedName = Collections.unmodifiableMap(copy);
    }

    /**
     * 루트 클래스와 전체 클래스 컬렉션으로 ModelGraph를 만든다.
     *
     * @param rootClass  그래프의 루트에 해당하는 클래스 (예: WeatherApiResponse)
     * @param allClasses 생성 대상 전체 클래스 컬렉션 (rootClass 포함)
     * @return ModelGraph 인스턴스
     */
    public static ModelGraph of(ModelClass rootClass, Collection<ModelClass> allClasses) {
        Objects.requireNonNull(rootClass, "rootClass must not be null");
        Objects.requireNonNull(allClasses, "allClasses must not be null");

        Map<String, ModelClass> map = new LinkedHashMap<>();
        for (ModelClass modelClass : allClasses) {
            String qualifiedName = modelClass.getQualifiedName();
            if (map.containsKey(qualifiedName)) {
                throw new IllegalArgumentException("Duplicate model class qualified name: " + qualifiedName);
            }
            map.put(qualifiedName, modelClass);
        }

        String rootQualifiedName = rootClass.getQualifiedName();
        if (!map.containsKey(rootQualifiedName)) {
            map.put(rootQualifiedName, rootClass);
        }

        return new ModelGraph(rootClass, map);
    }

    /**
     * JSON 분석/타입 추론 결과를 기반으로 ModelGraph를 구성한다.
     *
     * @param schemaRoot    JsonAnalyzer가 만든 루트 SchemaNode
     * @param typeMap       TypeInferencer가 생성한 타입 매핑
     * @param packageName   최종 DTO 패키지 이름
     * @param rootClassName 루트 클래스 이름
     */
    public static ModelGraph from(SchemaNode schemaRoot,
                                  Map<SchemaNode, TypeRef> typeMap,
                                  String packageName,
                                  String rootClassName) {
        return from(schemaRoot, typeMap, packageName, rootClassName, new DefaultNameConverter());
    }

    /**
     * NameConverter를 직접 주입할 수 있는 오버로드 버전.
     */
    public static ModelGraph from(SchemaNode schemaRoot,
                                  Map<SchemaNode, TypeRef> typeMap,
                                  String packageName,
                                  String rootClassName,
                                  NameConverter nameConverter) {

        Objects.requireNonNull(schemaRoot, "schemaRoot must not be null");
        Objects.requireNonNull(typeMap, "typeMap must not be null");
        Objects.requireNonNull(packageName, "packageName must not be null");
        Objects.requireNonNull(rootClassName, "rootClassName must not be null");
        Objects.requireNonNull(nameConverter, "nameConverter must not be null");

        Map<SchemaNode, ModelClass> created = new LinkedHashMap<>();
        ModelClass rootClass = buildClassForNode(
                schemaRoot,
                typeMap,
                packageName,
                rootClassName,
                true,
                nameConverter,
                created
        );

        return ModelGraph.of(rootClass, created.values());
    }

    /**
     * 주어진 SchemaNode에 대응하는 ModelClass를 생성한다.
     * 이미 생성된 노드는 재사용한다.
     */
    private static ModelClass buildClassForNode(SchemaNode node,
                                                Map<SchemaNode, TypeRef> typeMap,
                                                String packageName,
                                                String suggestedSimpleName,
                                                boolean root,
                                                NameConverter nameConverter,
                                                Map<SchemaNode, ModelClass> created) {

        if (created.containsKey(node)) {
            return created.get(node);
        }

        if (!(node instanceof SchemaObject obj)) {
            throw new IllegalStateException("Object schema expected for class generation: " + node);
        }

        List<Field> fields = new ArrayList<>();

        for (Map.Entry<String, FieldInfo> entry : obj.fields().entrySet()) {
            String jsonName = entry.getKey();
            FieldInfo fieldInfo = entry.getValue();
            SchemaNode fieldSchema = fieldInfo.schema();

            String fieldName = nameConverter.toCamelCase(jsonName);

            TypeRef ref = typeMap.get(fieldSchema);
            if (ref == null) {
                throw new IllegalStateException("No TypeRef found for schema node: " + fieldSchema);
            }

            // TypeRef의 Java 타입 이름 사용
            String typeName = ref.getJavaType();
            boolean nullable = fieldInfo.optional();

            fields.add(new Field(jsonName, fieldName, typeName, nullable));

            // 필드 스키마가 중첩 객체/배열/유니온인 경우, 필요한 ModelClass들을 재귀적으로 생성
            createNestedClassesIfNeeded(fieldSchema, typeMap, packageName, nameConverter, created);
        }

        ModelClass modelClass = new ModelClass(packageName, suggestedSimpleName, fields, root);
        created.put(node, modelClass);
        return modelClass;

    }

    /**
     * 필드 스키마에 따라 필요한 중첩 ModelClass들을 생성한다.
     * - SchemaObject: 해당 노드에 대한 ModelClass를 생성
     * - SchemaArray: elementTypes 안에 객체가 있으면 재귀 처리
     * - SchemaUnion: variants 안에 객체가 있으면 재귀 처리
     */
    private static void createNestedClassesIfNeeded(SchemaNode schema,
                                                    Map<SchemaNode, TypeRef> typeMap,
                                                    String packageName,
                                                    NameConverter nameConverter,
                                                    Map<SchemaNode, ModelClass> created) {

        if (schema instanceof SchemaObject) {
            TypeRef ref = typeMap.get(schema);
            if (ref == null) {
                throw new IllegalStateException("No TypeRef found for object schema node: " + schema);
            }
            String className = ref.getJavaType();
            // 이미 생성된 경우 created 맵 내부에서 재사용
            buildClassForNode(schema, typeMap, packageName, className, false, nameConverter, created);
            return;
        }

        if (schema instanceof SchemaArray array) {
            for (SchemaNode elementSchema : array.elementTypes()) {
                createNestedClassesIfNeeded(elementSchema, typeMap, packageName, nameConverter, created);
            }
            return;
        }

        if (schema instanceof SchemaUnion union) {
            for (SchemaNode variant : union.variants()) {
                createNestedClassesIfNeeded(variant, typeMap, packageName, nameConverter, created);
            }
        }
    }


    /**
     * 그래프의 루트 클래스(엔트리 포인트)를 반환한다.
     */
    public ModelClass getRootClass() {
        return rootClass;
    }

    /**
     * 그래프에 포함된 모든 모델 클래스를 선언 순서대로 반환한다.
     */
    public Collection<ModelClass> getDeclaredClasses() {
        return classesByQualifiedName.values();
    }

    /**
     * FQCN(qualified name)으로 클래스 조회.
     * 예: "com.example.dto.WeatherApiResponse"
     */
    public Optional<ModelClass> findClass(String qualifiedName) {
        Objects.requireNonNull(qualifiedName, "qualifiedName must not be null");
        return Optional.ofNullable(classesByQualifiedName.get(qualifiedName));
    }

    /**
     * 그래프에 포함된 클래스 개수.
     */
    public int size() {
        return classesByQualifiedName.size();
    }

    /**
     * 디버깅/로그용 간단한 문자열 표현.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ModelGraph{\n");
        for (ModelClass modelClass : classesByQualifiedName.values()) {
            sb.append("  ").append(modelClass).append('\n');
        }
        sb.append('}');
        return sb.toString();
    }

    // =====================================================================
    //  ModelClass
    // =====================================================================

    /**
     * 실제로 생성될 Java 클래스 한 개를 표현하는 불변 객체.
     */
    public static final class ModelClass {

        private final String packageName;
        private final String simpleName;
        private final boolean root;
        private final List<Field> fields;

        /**
         * @param packageName 패키지 이름 (예: "com.example.dto")
         * @param simpleName  클래스 이름 (예: "WeatherApiResponse")
         * @param fields      필드 목록
         * @param root        루트 클래스 여부
         */
        public ModelClass(String packageName, String simpleName, List<Field> fields, boolean root) {
            this.packageName = Objects.requireNonNull(packageName, "packageName must not be null");
            this.simpleName = Objects.requireNonNull(simpleName, "simpleName must not be null");
            this.root = root;
            Objects.requireNonNull(fields, "fields must not be null");
            // 필드 순서 고정 + 불변 리스트로 래핑
            this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
        }

        /**
         * 패키지 이름 (예: "com.example.dto")
         */
        public String getPackageName() {
            return packageName;
        }

        /**
         * 단순 클래스 이름 (예: "WeatherApiResponse")
         */
        public String getSimpleName() {
            return simpleName;
        }

        /**
         * "com.example.dto.WeatherApiResponse" 형태의 FQCN
         */
        public String getQualifiedName() {
            if (packageName.isEmpty()) {
                return simpleName;
            }
            return packageName + '.' + simpleName;
        }

        /**
         * 이 클래스가 루트 클래스(엔트리 포인트)인지 여부.
         */
        public boolean isRoot() {
            return root;
        }

        /**
         * 클래스가 가진 필드들을 선언 순서대로 반환한다.
         */
        public List<Field> getFields() {
            return fields;
        }

        @Override
        public String toString() {
            return "ModelClass{" +
                    "qualifiedName='" + getQualifiedName() + '\'' +
                    ", root=" + root +
                    ", fields=" + fields +
                    '}';
        }
    }


    // =====================================================================
    //  Field
    // =====================================================================

    /**
     * 하나의 Java 필드를 표현하는 불변 객체.
     *
     * <ul>
     *     <li>jsonName: 원래 JSON 키 이름 (예: "temp_c")</li>
     *     <li>fieldName: Java 필드 이름 (예: "tempC")</li>
     *     <li>typeName: Java 타입 이름 (예: "double", "String", "List<Student>")</li>
     *     <li>nullable: JSON 상에서 null/누락 가능 여부</li>
     * </ul>
     */
    public static final class Field {

        private final String jsonName;
        private final String fieldName;
        private final String typeName;
        private final boolean nullable;

        /**
         * @param jsonName  JSON 키 이름
         * @param fieldName Java 필드 이름
         * @param typeName  Java 타입 이름 (예: "String", "int", "List<Article>")
         * @param nullable  값이 null/누락될 수 있는지 여부
         */
        public Field(String jsonName, String fieldName, String typeName, boolean nullable) {
            this.jsonName = Objects.requireNonNull(jsonName, "jsonName must not be null");
            this.fieldName = Objects.requireNonNull(fieldName, "fieldName must not be null");
            this.typeName = Objects.requireNonNull(typeName, "typeName must not be null");
            this.nullable = nullable;
        }

        /**
         * 원본 JSON 키 이름.
         */
        public String getJsonName() {
            return jsonName;
        }

        /**
         * Java 필드 이름.
         */
        public String getFieldName() {
            return fieldName;
        }

        /**
         * Java 타입 이름 (예: "String", "int", "List<Article>").
         *
         * <p>실제 코드 생성 시에는 이 문자열이 그대로
         * 필드 선언에 사용된다.</p>
         */
        public String getTypeName() {
            return typeName;
        }

        /**
         * 이 필드가 null/누락 가능하면 true.
         * 예: 일부 객체에서만 등장하는 필드, "null"이 자주 나오는 필드 등.
         */
        public boolean isNullable() {
            return nullable;
        }

        @Override
        public String toString() {
            return "Field{" +
                    "jsonName='" + jsonName + '\'' +
                    ", fieldName='" + fieldName + '\'' +
                    ", typeName='" + typeName + '\'' +
                    ", nullable=" + nullable +
                    '}';
        }
    }

}
