package converter.generator;

import java.nio.file.Path;

/**
 * 클래스 소스 코드를 실제 Java 파일(.java)로 저장하는 유틸리티.
 *
 * <p>추후 JSON → Java 코드 생성 파이프라인의 마지막 단계에서
 * 생성된 소스 코드를 디스크에 기록하는 역할을 맡는다.
 */
public class FileWriter {

    /**
     * 지정된 출력 디렉터리에 "className.java" 파일을 생성하여 내용을 기록한다.
     *
     * <p>현재는 시그니처만 정의된 상태이며, 실제 구현은 이후 커밋에서 추가한다.
     *
     * @param outDir 출력 디렉터리 (이미 존재해야 함)
     * @param className 생성할 클래스 이름
     * @param content 파일에 쓸 Java 소스 전체
     */
    public void write(Path outDir, String className, String content) {
        // TODO: 구현 예정
        throw new UnsupportedOperationException("FileWriter.write() is not implemented yet");
    }
}