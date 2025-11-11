package org.example.cli;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class FileValidator {
    private FileValidator() {}

    /**
     * 출력 디렉터리 경로를 검증한다.
     * - 존재하지 않으면 생성
     * - 디렉터리가 아닌 경우 예외
     * - 쓰기 권한이 없는 경우 예외
     */
    public static Path validateOutDirectory(String pathStr) {
        Path path = Paths.get(pathStr).toAbsolutePath().normalize();

        try {
            if (Files.exists(path)) {
                if (!Files.isDirectory(path)) {
                    throw new IllegalArgumentException("[ERROR] --out 경로가 디렉터리가 아닙니다: " + path);
                }
            } else {
                Files.createDirectories(path); // 없으면 생성
            }

            if (!Files.isWritable(path)) {
                throw new IllegalArgumentException("[ERROR] --out 경로에 쓰기 권한이 없습니다: " + path);
            }

            return path;
        } catch (IOException e) {
            throw new IllegalArgumentException("[ERROR] --out 디렉터리 접근 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}
