package org.example.json;

/**
 * 이름 변환 전략 인터페이스.
 * PascalCase / camelCase 변환을 정의한다.
 */
public interface NameConverter {
    String toPascalCase(String s);
    String toCamelCase(String s);
}
