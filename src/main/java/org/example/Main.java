package org.example;

import org.example.cli.ArgumentParser;
import org.example.cli.ParsedArguments;
import org.example.exception.InternalException;
import org.example.exception.UserException;
import org.example.json.JsonAnalyzer;
import org.example.json.JsonValidator;
import org.example.json.SchemaNode;
import org.example.json.TypeInferencer;

import java.util.Map;

public class Main {

    public static void main(String[] args) {
        try {
            // 1. CLI 인자 파싱
            ArgumentParser parser = new ArgumentParser();
            ParsedArguments parsed = parser.parse(args);

            // 2. JSON 파일 로드 및 검증 (Result 확보)
            JsonValidator.Result result = JsonValidator.validateAndLoad(parsed.getInputPath());

            // 3. JsonAnalyzer로 스키마 트리 생성
            JsonAnalyzer analyzer = new JsonAnalyzer();
            SchemaNode schemaRoot = analyzer.analyze(result.root());

            // 4. TypeInferencer로 타입 추론
            TypeInferencer inferencer = new TypeInferencer();
            Map<SchemaNode, TypeInferencer.TypeRef> typeMap =
                    inferencer.inferTypes(schemaRoot, parsed.getRootClass());

            // (임시) 디버깅/검증용 출력
            System.out.println("[INFO] JSON 분석 및 타입 추론이 완료되었습니다.");
            System.out.println("[INFO] 입력 파일: " + parsed.getInputPath());
            System.out.println("[INFO] 루트 클래스 이름: " + parsed.getRootClass());
            System.out.println("[INFO] 패키지 이름: " + parsed.getPackageName());
            System.out.println("[INFO] 출력 디렉터리: " + parsed.getOutDir());
            System.out.println("[INFO] 추론된 타입 수: " + typeMap.size());

            // TODO:
            //   - TypeInferencer 결과 + SchemaNode를 사용해 ModelGraph 생성
            //   - ModelGraph를 기반으로 generator 패키지 (Template / ClassGenerator / FileWriter) 호출
            //   - 실제 .java 파일 출력

        } catch (UserException e) {
            // 사용자가 옵션/입력 파일 등을 잘못 준 경우
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (InternalException e) {
            // 프로그램 내부 버그나 I/O 등의 내부 오류
            System.err.println("[ERROR] 프로그램 내부 오류가 발생했습니다.");
            System.err.println("  상세: " + e.getMessage());
            // 개발 중에는 스택트레이스 보고 싶으면 유지, 나중에 빼도 됨
            e.printStackTrace(System.err);
            System.exit(2);
        } catch (Exception e) {
            // 혹시 우리가 놓친 예외들에 대한 최후 방어선
            System.err.println("[ERROR] 처리되지 않은 예외가 발생했습니다: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(99);
        }
    }
}
