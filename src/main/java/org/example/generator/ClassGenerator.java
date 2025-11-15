package org.example.generator;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JSON 분석/타입 추론 결과를 기반으로
 * 단일 Java 클래스 소스 코드를 생성하는 유틸리티.
 *
 * <p>이 클래스는 아직 ModelGraph와 직접 연결되지는 않고,
 * {@link ClassSpec}, {@link FieldSpec} 을 통해
 * "생성할 클래스의 형상"을 전달받는 수준까지만 책임을 가진다.
 * 이후 ModelGraph 쪽이 완성되면,
 * 거기서 ClassSpec 목록을 만들어 이 Generator에 넘겨주는 식으로 연결하면 된다.
 */
public class ClassGenerator {

    /**
     * 필드 정의용 간단한 DTO.
     * type: "String", "int", "List<Student>" 등 Java 타입 표현
     * name: "name", "age" 등의 필드명 (camelCase 가정)
     * comment: 필드 설명이 있을 경우 필드 위에 Javadoc으로 출력 (없으면 생략)
     */
    public static final class FieldSpec {
        private final String type;
        private final String name;
        private final String comment;

        public FieldSpec(String type, String name) {
            this(type, name, null);
        }

        public FieldSpec(String type, String name, String comment) {
            this.type = Objects.requireNonNull(type, "type must not be null");
            this.name = Objects.requireNonNull(name, "name must not be null");
            this.comment = comment;
        }

        public String type() {
            return type;
        }

        public String name() {
            return name;
        }

        public Optional<String> comment() {
            return Optional.ofNullable(comment);
        }
    }

    /**
     * 클래스 전체 정의용 DTO.
     * packageName: "com.example.dto"
     * className: "WeatherApiResponse"
     * fields: 필드 목록
     */
    public static final class ClassSpec {
        private final String packageName;
        private final String className;
        private final List<FieldSpec> fields;

        public ClassSpec(String packageName, String className, List<FieldSpec> fields) {
            this.packageName = Objects.requireNonNull(packageName, "packageName must not be null");
            this.className = Objects.requireNonNull(className, "className must not be null");
            this.fields = List.copyOf(Objects.requireNonNull(fields, "fields must not be null"));
        }

        public String packageName() {
            return packageName;
        }

        public String className() {
            return className;
        }

        public List<FieldSpec> fields() {
            return fields;
        }
    }

    private final Template classTemplate;
    private final Template fieldTemplate;
    private final CodeFormatter codeFormatter;

    /**
     * 기본 템플릿을 사용하는 생성자.
     * <p>
     * - package, imports, class 선언, 필드까지 전부 Template로 치환한다.
     */
    public ClassGenerator(CodeFormatter codeFormatter) {
        this(
                // 클래스 전체 템플릿
                new Template(
                        "package ${package};\n" +
                                "\n" +
                                "${imports}" +
                                "public class ${className} {\n" +
                                "\n" +
                                "${fields}\n" +
                                "}\n"
                ),
                // 단일 필드 템플릿
                new Template(
                        "${comment}" +
                                "    private ${type} ${name};\n"
                ),
                codeFormatter
        );
    }

    /**
     * 테스트나 확장을 위해 Template를 직접 주입하고 싶을 때 사용하는 생성자.
     */
    public ClassGenerator(Template classTemplate, Template fieldTemplate, CodeFormatter codeFormatter) {
        this.classTemplate = Objects.requireNonNull(classTemplate, "classTemplate must not be null");
        this.fieldTemplate = Objects.requireNonNull(fieldTemplate, "fieldTemplate must not be null");
        this.codeFormatter = Objects.requireNonNull(codeFormatter, "codeFormatter must not be null");
    }

    /**
     * 필드 리스트를 순회하면서 필드 선언부 문자열을 만든다.
     */
    private String buildFieldsSource(List<FieldSpec> fields) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < fields.size(); i++) {
            FieldSpec field = fields.get(i);

            String commentBlock = field.comment()
                    .map(this::toFieldJavadoc)
                    .orElse("");

            Map<String, String> vars = new HashMap<>();
            vars.put("comment", commentBlock);
            vars.put("type", field.type());
            vars.put("name", field.name());

            sb.append(fieldTemplate.render(vars));

            if (i < fields.size() - 1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * 간단한 필드 Javadoc 변환.
     * 여러 줄 주석도 처리 가능하도록 줄 단위로 분리해서 붙인다.
     */
    private String toFieldJavadoc(String comment) {
        String[] lines = comment.split("\\R");
        StringBuilder sb = new StringBuilder();
        sb.append("    /**\n");
        for (String line : lines) {
            sb.append("     * ").append(line).append("\n");
        }
        sb.append("     */\n");
        return sb.toString();
    }

    /**
     * 필드 타입을 살펴보면서 필요한 import를 추출한다.
     * <p>
     * - java.lang 패키지는 import 하지 않는다.
     * - List, Map 등 컬렉션 인터페이스는 java.util 패키지를 import 한다.
     *   (실제 규칙은 필요에 따라 확장/수정 가능)
     */
    private String buildImportsSource(List<FieldSpec> fields) {
        Set<String> imports = new LinkedHashSet<>();

        for (FieldSpec field : fields) {
            collectImportsFromType(field.type(), imports);
        }

        if (imports.isEmpty()) {
            return "";
        }

        String joined = imports.stream()
                .sorted()
                .map(fqcn -> "import " + fqcn + ";\n")
                .collect(Collectors.joining());

        return joined + "\n";
    }

    /**
     * 매우 단순한 규칙으로 타입 문자열에서 import 대상을 추출한다.
     * <p>
     * - "List<Something>" → java.util.List
     * - "Map<K, V>" → java.util.Map
     *
     * 이후 LocalDateTime, BigDecimal, 커스텀 타입 등에 대한 규칙은
     * 필요해지면 확장하면 된다.
     */
    private void collectImportsFromType(String type, Set<String> imports) {
        String trimmed = type.trim();

        if (trimmed.startsWith("List<") || trimmed.equals("List")) {
            imports.add("java.util.List");
        }
        if (trimmed.startsWith("Map<") || trimmed.equals("Map")) {
            imports.add("java.util.Map");
        }

        // TODO: 필요 시 LocalDateTime, BigDecimal, Set, custom 타입 등도 여기에서 처리
    }
}
