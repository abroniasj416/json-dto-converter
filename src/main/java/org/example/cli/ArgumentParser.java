package org.example.cli;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArgumentParser {
    // TODO : parse(String[] args) 구현

//    java -jar json-dto-converter.jar ^
//            --input <path/to/sample.json> ^
//            --root-class <RootClassName> ^
//            --package <com.example.dto> ^
//            --out <build/generated> ^
//            --inner-classes true

//    args[0] -> --input
//    args[1] -> <path/to/sample.json>
//    args[2] -> --root-class
//    args[3] -> <RootClassName>
//    args[4] -> --package
//    args[5] -> <com.example.dto>
//    args[6] -> --out
//    args[7] -> <build/generated>
//    args[8] -> --inner-classes
//    args[9] -> true

    public ParsedArguments parse(String[] args) {
        String inputPath = null;
        String rootClass = null;
        String packageName = null;
        String outDir = null;
        boolean innerClasses = false;

        // TODO : inputPath, rootClass, packageName, outDir, innerClasses가 잘못된 값일 경우 예외처리 -> 예외처리하지 않으면 return 문이 불완전
        // TODO : 상수 분리
        // TODO : 로깅
        validateOptions(args);
        validateValues(args);

        for (int i = 0; i < args.length; i++) {
            if (i % 2 != 0)
                continue;

            switch (args[i].substring(2)) {
                case "input":
                    inputPath = args[i + 1];
                    break;
                case "root-class":
                    rootClass = args[i + 1];
                    break;
                case "package":
                    packageName = args[i + 1];
                    break;
                case "out":
                    outDir = args[i + 1];
                    break;
                case "inner-classes":
                    innerClasses = Boolean.parseBoolean(args[i + 1]);
            }

        }

        return new ParsedArguments(inputPath, rootClass, packageName, outDir, innerClasses);
    }

    private void validateOptions(String[] args) {
        // 옵션-값의 쌍 최소 요건 검사
        if (args.length % 2 != 0)
            throw new IllegalArgumentException("[ERROR] 옵션과 값은 쌍으로 입력해야 합니다.");

        Set<String> allowed = Set.of("--input", "--root-class", "--package", "--out", "--inner-classes");
        Set<String> seen = new HashSet<>();

        for (int i = 0; i < args.length; i += 2) {
            String option = args[i];
            String value = args[i + 1];

            // ‘--’ 없이 토큰 시작하는 옵션 형식 위반 예외
            if (!option.startsWith("--"))
                throw new IllegalArgumentException("[ERROR] 옵션은 '--'으로 시작해야 합니다: " + option);
            // 지원하지 않는 옵션 예외
            if (!allowed.contains(option))
                throw new IllegalArgumentException("[EROR] 지원하지 않는 옵션입니다: " + option);
            // 옵션 중복 예외
            if (!seen.add(option))
                throw new IllegalArgumentException(("[ERROR] 옵션이 중복되었습니다: " + option));
            // 옵션 값 누락 예외
            if (value == null || value.isBlank() || value.startsWith("--"))
                throw new IllegalArgumentException("[ERROR] 옵션의 값이 없습니다: " + option);
        }

        // 필수 옵션 미포함 예외
        List<String> options = new ArrayList<>();
        for (int i = 0; i < args.length; i += 2)
            options.add(args[i]);
        if (options.contains("--input"))
            throw new IllegalArgumentException("[ERROR] --input은 필수입니다.");
        if (options.contains("--root-class"))
            throw new IllegalArgumentException("[ERROR] --root-class는 필수입니다.");
        if (options.contains("--package"))
            throw new IllegalArgumentException("[ERROR] --package는 필수입니다.");
        if (options.contains("--out"))
            throw new IllegalArgumentException("[ERROR] --out은 필수입니다.");
    }

    private void validateValues(String[] args) {
        // TODO : 구현

    }
}
