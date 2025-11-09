package org.example.cli;

public class ArgumentParser {
    // TODO : parse(String[] args) 구현

//    java -jar json-dto-converter.jar ^
//            --input <path/to/sample.json> ^
//            --root-class <RootClassName> ^
//            --package <com.example.dto> ^
//            --out <build/generated> ^
//            --inner-classes=true

//    args[0] -> --input
//    args[1] -> <path/to/sample.json>
//    args[2] -> --root-class
//    args[3] -> <RootClassName>
//    args[4] -> --package
//    args[5] -> <com.example.dto>
//    args[6] -> --out
//    args[7] -> <build/generated>
//    args[8] -> --inner-classes=true

    public ParsedArguments parse(String[] args) {
        String inputPath;
        String rootClass;
        String packageName;
        String outDir;
        boolean innerClasses;

        // TODO : inputPath, rootClass, packageName, outDir, innerClasses가 잘못된 값일 경우 예외처리 -> 예외처리하지 않으면 return 문이 불완전
        // TODO : 상수 분리
        // TODO : 로깅
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                switch (args[i].substring(2)) {
                    case "input":
                        inputPath = args[i + 1];
                    case "root-class":
                        rootClass = args[i + 1];
                    case "package":
                        packageName = args[i + 1];
                    case "out":
                        outDir = args[i + 1];
                    case "inner-classes":
                        innerClasses = Boolean.parseBoolean(args[i + 1]);
                }
            }
        }

        retrun new ParsedArguments(inputPath, rootClass, packageName, outDir, innerClasses);
    }


}
