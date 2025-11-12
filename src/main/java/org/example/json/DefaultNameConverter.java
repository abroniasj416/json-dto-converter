package org.example.json;

/**
 * 간단한 이름 변환기 구현:
 * - snake/kebab/space 구분자를 공백으로 통일 후 PascalCase, camelCase 변환
 */
public class DefaultNameConverter implements NameConverter {

    @Override
    public String toPascalCase(String s) {
        if (s == null || s.isEmpty()) return s;
        String[] parts = s.replaceAll("[^A-Za-z0-9]+", " ").trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) sb.append(p.substring(1));
        }
        return sb.toString();
    }

    @Override
    public String toCamelCase(String s) {
        String pascal = toPascalCase(s);
        if (pascal == null || pascal.isEmpty()) return pascal;
        return Character.toLowerCase(pascal.charAt(0)) + pascal.substring(1);
    }
}