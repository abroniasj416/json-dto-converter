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
     * 패턴 안의 <code>${name}</code> 형태 플레이스홀더를 찾아
     * {@code variables.get("name")} 값으로 치환한다.
     * <ul>
     *     <li>맵에 해당 키가 없으면 플레이스홀더를 그대로 남긴다. (치환하지 않음)</li>
     *     <li>맵 값이 {@code null}이면 빈 문자열로 치환한다.</li>
     *     <li>닫는 중괄호('}')를 찾지 못하면 <code>$</code> 문자부터 그대로 출력한다.</li>
     * </ul>
     *
     * @param variables 플레이스홀더 이름과 치환 문자열의 매핑
     * @return 치환이 적용된 결과 문자열
     */
    public String render(Map<String, String> variables) {
        Objects.requireNonNull(variables, "variables must not be null");

        StringBuilder result = new StringBuilder();
        int length = pattern.length();
        int index = 0;

        while (index < length) {
            char ch = pattern.charAt(index);

            if (ch == DOLLAR && index + 1 < length && pattern.charAt(index + 1) == OPEN_BRACE) {
                // 플레이스홀더 시작: "${"
                int start = index + 2; // 이름 시작 위치
                int end = pattern.indexOf(CLOSE_BRACE, start);

                if (end == -1) {
                    // '}'를 찾지 못한 경우: 남은 부분을 그대로 출력하고 종료
                    result.append(pattern.substring(index));
                    break;
                }

                String name = pattern.substring(start, end);

                // 키 존재 여부와 값 null 여부를 구분해서 처리
                if (!variables.containsKey(name)) {
                    // 맵에 해당 키가 없는 경우: 플레이스홀더 그대로 남김
                    result.append(pattern, index, end + 1);
                } else {
                    String value = variables.get(name);
                    // 값이 null이면 빈 문자열로 치환
                    result.append(value != null ? value : "");
                }


                index = end + 1;
            } else {
                result.append(ch);
                index++;
            }
        }

        return result.toString();
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
