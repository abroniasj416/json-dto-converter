package org.example.generator;

import java.util.Map;
import java.util.Objects;

/**
 * 간단한 문자열 템플릿 클래스.
 * <p>
 * 패턴 문자열 안의 플레이스홀더를 치환한다.
 * 플레이스홀더 형식은 <code>${name}</code> 이다.
 *
 * 예시:
 * <pre>
 * Template t = new Template("package ${package};\n\npublic class ${className} {\n${fields}\n}");
 * String result = t.render(Map.of(
 *     "package", "com.example",
 *     "className", "User",
 *     "fields", "    private String name;"
 * ));
 * </pre>
 */
public class Template {

    private static final char DOLLAR = '$';
    private static final char OPEN_BRACE = '{';
    private static final char CLOSE_BRACE = '}';

    private final String pattern;

    /**
     * 주어진 패턴 문자열로 템플릿을 생성한다.
     *
     * @param pattern 플레이스홀더를 포함할 수 있는 패턴 문자열
     */
    public Template(String pattern) {
        this.pattern = Objects.requireNonNull(pattern, "pattern must not be null");
    }

    /**
     * 템플릿 원본 패턴 문자열을 반환한다.
     */
    public String pattern() {
        return pattern;
    }

    /**
     * 주어진 변수 맵을 사용해 템플릿을 렌더링한다.
     * <p>
     * 현재는 아직 플레이스홀더 치환 로직이 구현되지 않았으며,
     * 전달된 변수 맵의 null 여부만 검증하고 원본 패턴을 그대로 반환한다.
     *
     * @param variables 플레이스홀더 이름과 치환 문자열의 매핑
     * @return 현재는 치환이 적용되지 않은 원본 패턴 문자열
     */
    public String render(Map<String, String> variables) {
        Objects.requireNonNull(variables, "variables must not be null");
        // TODO: ${name} 형식 플레이스홀더 치환 로직 구현
        return pattern;
    }

    /**
     * 편의용 정적 팩토리 메서드.
     *
     * @param pattern 템플릿 패턴
     * @return Template 인스턴스
     */
    public static Template of(String pattern) {
        return new Template(pattern);
    }
}
