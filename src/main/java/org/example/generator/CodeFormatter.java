package org.example.generator;

import java.util.Objects;

/**
 * Java 코드 문자열을 간단히 정리(포매팅)하는 유틸리티.
 * <p>
 * 완전한 코드 포매터는 아니지만, 생성된 Java 코드에 대해 다음과 같은
 * 최소한의 정리를 수행한다:
 * <ul>
 *     <li>줄 끝 공백(trailing whitespace) 제거</li>
 *     <li>연속된 빈 줄을 하나로 축소</li>
 *     <li>줄 구분자를 모두 '\n'으로 통일</li>
 *     <li>파일 끝에 개행 문자가 없으면 한 줄 추가</li>
 * </ul>
 */
public class CodeFormatter {

    /**
     * 주어진 Java 소스 코드를 간단히 정리하여 반환한다.
     * <p>
     * 수행 내용:
     * <ul>
     *     <li>모든 개행을 '\n'으로 통일</li>
     *     <li>각 줄의 끝에 있는 공백 · 탭 제거</li>
     *     <li>연속된 빈 줄은 하나로 축소</li>
     *     <li>마지막 줄이 개행으로 끝나도록 보장</li>
     * </ul>
     *
     * @param source 포매팅할 원본 Java 코드 문자열
     * @return 정리된 코드 문자열
     */
    public String format(String source) {
        Objects.requireNonNull(source, "source must not be null");

        String normalized = normalizeLineEndings(source);
        String[] lines = normalized.split("\n", -1);

        StringBuilder result = new StringBuilder();
        boolean previousBlank = false;

        for (String line : lines) {
            String trimmedEnd = trimTrailingWhitespace(line);
            boolean isBlank = trimmedEnd.isBlank();

            if (isBlank) {
                if (previousBlank) {
                    continue;
                }
                previousBlank = true;
                result.append('\n');
            } else {
                previousBlank = false;
                result.append(trimmedEnd).append('\n');
            }
        }

        return result.toString();
    }

    /**
     * 줄 끝 문자를 모두 '\n'으로 통일한다.
     * <p>
     * - Windows 스타일 CRLF("\r\n") -> "\n"
     * - 단독 CR("\r") -> "\n"
     */
    private String normalizeLineEndings(String source) {
        String withoutWindows = source.replace("\r\n", "\n");
        return withoutWindows.replace("\r", "\n");
    }

    /**
     * 한 줄 끝의 공백 문자(스페이스, 탭)를 제거한다.
     *
     * @param line 입력 줄
     * @return 끝의 whitespace가 제거된 줄
     */
    private String trimTrailingWhitespace(String line) {
        int end = line.length();
        while (end > 0) {
            char ch = line.charAt(end - 1);
            if (ch == ' ' || ch == '\t') {
                end--;
            } else {
                break;
            }
        }
        return (end == line.length()) ? line : line.substring(0, end);
    }

}
