package org.example.cli;

import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class FileValidator {
    private FileValidator() {}

    // 파일 경로가 유효하고 읽을 수 있는지 검증 후 정규화된 Path 반환
    public static Path validateReadableFile(String pathStr) {
        Path path = Paths.get(pathStr).toAbsolutePath().normalize();

        if (!Files.exists(path))
            throw new IllegalArgumentException("[ERROR] --input 경로가 존재하지 않습니다: " + path);
        if (!Files.isRegularFile(path))
            throw new IllegalArgumentException("[ERROR] --input 은 파일이어야 합니다: " + path);
        if (!Files.isReadable(path))
            throw new IllegalArgumentException("[ERROR] --input 파일을 읽을 수 없습니다: " + path);

        return path;
    }

    // UTF-8로 읽어 온 문자열 반환
    public static String readUtf8(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (MalformedInputException e) {
            throw new IllegalArgumentException("[ERROR] --input 파일이 UTF-8 인코딩이 아닙니다: " + path);
        } catch (Exception e) {
            throw new IllegalArgumentException("[ERROR] --input 파일을 읽는 중 오류가 발생했습니다: " + path);
        }
    }
}
