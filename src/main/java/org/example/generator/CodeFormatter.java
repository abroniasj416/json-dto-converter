package org.example.generator;

import java.util.Objects;

/**
 * Java 코드 문자열을 간단히 정리(포매팅)하는 유틸리티.
 * <p>
 * 아직 실제 포매팅 기능은 구현되지 않았다.
 */
public class CodeFormatter {

    /**
     * 주어진 소스 코드를 간단히 정리한 문자열로 반환한다.
     * <p>
     * 현재는 원본을 그대로 반환하며, 포매팅 로직은 TODO 상태이다.
     *
     * @param source 포매팅할 원본 소스 코드
     * @return 현재는 포매팅되지 않은 원본 문자열
     */
    public String format(String source) {
        Objects.requireNonNull(source, "source must not be null");
        // TODO: 줄 끝 공백 제거, 빈 줄 축소, 개행 정규화 로직 구현
        return source;
    }

    private String normalizeLineEndings(String source) {
        String withoutWindows = source.replace("\r\n", "\n");
        return withoutWindows.replace("\r", "\n");
    }
}
