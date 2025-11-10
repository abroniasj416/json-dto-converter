package org.example.cli;

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
}
