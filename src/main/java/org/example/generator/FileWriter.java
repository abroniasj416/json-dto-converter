package converter.generator;

import converter.exception.UserException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 클래스 소스 코드를 실제 Java 파일(.java)로 저장하는 유틸리티.
 *
 * <p>책임:
 * <ul>
 *   <li>지정된 출력 디렉터리 아래에 클래스명.java 파일 생성</li>
 *   <li>UTF-8로 콘텐츠 저장</li>
 *   <li>쓰기 과정에서 발생하는 예외를 UserException으로 래핑</li>
 * </ul>
 */
public class FileWriter {

    /**
     * 지정된 출력 디렉터리에 "className.java" 파일을 생성하여 내용을 기록한다.
     *
     * @param outDir 출력 디렉터리 (이미 존재해야 함)
     * @param className 생성할 클래스 이름
     * @param content 파일에 쓸 Java 소스 전체
     */
    public void write(Path outDir, String className, String content) {
        if (outDir == null || className == null || content == null) {
            throw new IllegalArgumentException("outDir, className, content는 null일 수 없습니다.");
        }

        Path filePath = outDir.resolve(className + ".java");

        try {
            Files.writeString(filePath, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UserException("[ERROR] Java 파일을 생성하는 중 오류가 발생했습니다: " + filePath, e);
        }
    }
}