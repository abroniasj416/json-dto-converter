package org.example.cli;

import org.example.exception.UserException;

import javax.lang.model.SourceVersion;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ArgumentParser {

    // ==========================
    // 옵션 상수
    // ==========================
    private static final String OPT_INPUT = "--input";
    private static final String OPT_ROOT_CLASS = "--root-class";
    private static final String OPT_PACKAGE = "--package";
    private static final String OPT_OUT = "--out";
    private static final String OPT_INNER_CLASSES = "--inner-classes";

    private static final Set<String> ALLOWED_OPTIONS = Set.of(
            OPT_INPUT, OPT_ROOT_CLASS, OPT_PACKAGE, OPT_OUT, OPT_INNER_CLASSES
    );

    /**
     * CLI 인자를 파싱하여 ParsedArguments로 변환한다.
     *
     * <p>
     * 처리 순서:
     * <ol>
     *     <li>옵션/값 쌍의 형식을 검사하고 Map&lt;옵션, 값&gt;으로 변환</li>
     *     <li>필수 옵션 존재 여부 확인</li>
     *     <li>각 옵션 값의 유효성 검사 (클래스명/패키지명/불리언 등)</li>
     *     <li>ParsedArguments 인스턴스 반환</li>
     * </ol>
     */
    public ParsedArguments parse(String[] args) {
        Map<String, String> options = validateAndBuildOptions(args);
        validateValues(options);

        String inputPath = options.get(OPT_INPUT);
        String rootClass = options.get(OPT_ROOT_CLASS);
        String packageName = options.get(OPT_PACKAGE);
        String outDir = options.get(OPT_OUT);

        boolean innerClasses = false;
        if (options.containsKey(OPT_INNER_CLASSES)) {
            innerClasses = Boolean.parseBoolean(options.get(OPT_INNER_CLASSES));
        }

        return new ParsedArguments(inputPath, rootClass, packageName, outDir, innerClasses);
    }

    /**
     * 옵션/값 형식을 검사하고, Map 형태로 변환한다.
     * - 옵션은 "--"로 시작해야 함
     * - 허용되지 않은 옵션 사용 시 예외
     * - 옵션 중복 사용 시 예외
     * - 값이 비어있거나 다음 옵션으로 시작하는 경우 예외
     * - 필수 옵션(--input, --root-class, --package, --out) 누락 시 예외
     */
    private Map<String, String> validateAndBuildOptions(String[] args) {
        if (args.length % 2 != 0) {
            throw new UserException("[ERROR] 옵션과 값은 쌍으로 입력해야 합니다.");
        }

        Map<String, String> options = new LinkedHashMap<>();

        for (int i = 0; i < args.length; i += 2) {
            String option = args[i];
            String value = args[i + 1];

            if (!option.startsWith("--")) {
                throw new UserException("[ERROR] 옵션은 '--'으로 시작해야 합니다: " + option);
            }
            if (!ALLOWED_OPTIONS.contains(option)) {
                throw new UserException("[ERROR] 지원하지 않는 옵션입니다: " + option);
            }
            if (options.containsKey(option)) {
                throw new UserException("[ERROR] 옵션이 중복되었습니다: " + option);
            }
            if (value.isBlank() || value.startsWith("--")) {
                throw new UserException("[ERROR] 옵션의 값이 없습니다: " + option);
            }

            options.put(option, value);
        }

        // 필수 옵션 확인
        if (!options.containsKey(OPT_INPUT)) {
            throw new UserException("[ERROR] --input은 필수입니다.");
        }
        if (!options.containsKey(OPT_ROOT_CLASS)) {
            throw new UserException("[ERROR] --root-class는 필수입니다.");
        }
        if (!options.containsKey(OPT_PACKAGE)) {
            throw new UserException("[ERROR] --package는 필수입니다.");
        }
        if (!options.containsKey(OPT_OUT)) {
            throw new UserException("[ERROR] --out은 필수입니다.");
        }

        return options;
    }

    /**
     * 옵션 값들의 구체적인 유효성을 검사한다.
     * - --root-class: 자바 식별자 규칙 및 키워드 여부
     * - --package: 각 세그먼트의 식별자/키워드 여부
     * - --out: 출력 디렉터리 경로 검증 (존재/생성/쓰기 가능 여부)
     * - --inner-classes: true/false 여부
     *
     * 파일 존재/JSON 파싱 등은 JsonValidator에서 처리하므로 여기서는 하지 않는다.
     */
    private void validateValues(Map<String, String> options) {
        for (Map.Entry<String, String> entry : options.entrySet()) {
            String option = entry.getKey();
            String value = entry.getValue();

            if (OPT_ROOT_CLASS.equals(option)) {
                if (!SourceVersion.isIdentifier(value) || SourceVersion.isKeyword(value)) {
                    throw new UserException("[ERROR] --root-class 값이 유효한 자바 클래스명이 아닙니다: " + value);
                }
            }

            if (OPT_PACKAGE.equals(option)) {
                String[] packageTokens = value.split("\\.");

                if (packageTokens.length == 0) {
                    throw new UserException("[ERROR] --package 값이 비어 있습니다: " + value);
                }

                for (String token : packageTokens) {
                    if (token.isBlank()) {
                        throw new UserException("[ERROR] --package 값에 빈 세그먼트가 포함되어 있습니다: " + value);
                    }
                    if (!SourceVersion.isIdentifier(token)) {
                        throw new UserException("[ERROR] --package 값의 일부가 유효한 식별자가 아닙니다: " + token);
                    }
                    if (SourceVersion.isKeyword(token)) {
                        throw new UserException("[ERROR] --package 값에 Java 키워드가 포함되어 있습니다: " + token);
                    }
                }
            }

            if (OPT_OUT.equals(option)) {
                // 유효하지 않은 경로라면 FileValidator 쪽에서 UserException/InternalException 발생
                FileValidator.validateOutDirectory(value);
            }

            if (OPT_INNER_CLASSES.equals(option)) {
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                    throw new UserException("[ERROR] --inner-classes 옵션은 true 또는 false만 허용됩니다: " + value);
                }
            }
        }
    }
}
