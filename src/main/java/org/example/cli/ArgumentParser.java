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
        List<String> options = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            if (i % 2 == 0)
                options.add(args[i]);
        }
        Set<String> removedDuplication = new HashSet<>(options);

        // 옵션 중복
        if (options.size() != removedDuplication.size())
            throw new IllegalArgumentException("[ERROR] 옵션 중복");

        for (String option : options) {
            // 옵션 형식 위반(‘--’ 없이 토큰 시작)
            if (!option.startsWith("--"))
                throw new IllegalArgumentException("[ERROR] 옵션은 '--'으로 시작해야 합니다: " + option);
            // 지원하지 않는 옵션
            if (!option.equals("--input")
                    && !option.equals("--root-class")
                    && !option.equals("--package")
                    && !option.equals("--out")
                    && !option.equals("--inner-classes"))
                throw new IllegalArgumentException("[ERROR] 지원하지 않는 옵션: " + option);

        }

        // 필수 옵션 누락 (--input, --root-class, --package, --out)
        if (!options.contains("--input"))
            throw new IllegalArgumentException("[ERROR] --input 은 필수입니다.");
        if (!options.contains("--root-class"))
            throw new IllegalArgumentException("[ERROR] --root-class 는 필수입니다.");
        if (!options.contains("--package"))
            throw new IllegalArgumentException("[ERROR] --package 는 필수입니다.");
        if (!options.contains("--out"))
            throw new IllegalArgumentException("[ERROR] --out 은 필수입니다.");
    }

    private void validateValues(String[] args) {
        // TODO : 구현
    }
}
