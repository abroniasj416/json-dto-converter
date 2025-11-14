package org.example.cli;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArgumentParserTest {

    private Path createTempJsonFile() throws IOException {
        Path tempJson = Files.createTempFile("sample-json-", ".json");
        Files.writeString(tempJson, "{\"name\":\"Alice\",\"age\":20}", StandardCharsets.UTF_8);
        return tempJson;
    }

    @Test
    void 모든_옵션을_정상_파싱한다() throws Exception {
        Path tempJson = createTempJsonFile();

        String[] args = {
                "--input", tempJson.toString(),
                "--root-class", "WeatherApiResponse",
                "--package", "com.org.example.entity",
                "--out", "build/generated",
                "--inner-classes", "true"
        };

        ArgumentParser parser = new ArgumentParser();
        ParsedArguments parsed = parser.parse(args);

        assertThat(parsed.getInputPath()).isEqualTo(tempJson.toString());
        assertThat(parsed.getRootClass()).isEqualTo("WeatherApiResponse");
        assertThat(parsed.getPackageName()).isEqualTo("com.org.example.entity");
        assertThat(parsed.getOutDir()).isEqualTo("build/generated");
        assertThat(parsed.isInnerClasses()).isTrue();
    }

    @Test
    void 필수_옵션이_누락되면_예외가_발생한다() {
        String[] args = {
                "--root-class", "WeatherApiResponse",
                "--package", "com.org.example.entity",
                "--out", "build/generated"
        };

        ArgumentParser parser = new ArgumentParser();

        assertThatThrownBy(() -> parser.parse(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("--input은 필수입니다");
    }

    @Test
    void 지원하지_않는_옵션이_들어오면_예외가_발생한다() throws Exception {
        Path tempJson = createTempJsonFile();

        String[] args = {
                "--input", tempJson.toString(),
                "--root-class", "WeatherApiResponse",
                "--unknown", "value",                      // 여기 때문에 실패해야 함
                "--out", "build/generated",
                "--package", "com.org.example.entity"
        };

        ArgumentParser parser = new ArgumentParser();

        assertThatThrownBy(() -> parser.parse(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지원하지 않는 옵션");
    }

    @Test
    void 옵션이_중복되면_예외가_발생한다() throws Exception {
        Path tempJson = createTempJsonFile();

        String[] args = {
                "--input", tempJson.toString(),
                "--input", tempJson.toString(),            // 중복
                "--root-class", "WeatherApiResponse",
                "--package", "com.org.example.entity",
                "--out", "build/generated"
        };

        ArgumentParser parser = new ArgumentParser();

        assertThatThrownBy(() -> parser.parse(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("옵션이 중복되었습니다");
    }

    @Test
    void root_class가_유효한_자바_식별자가_아니면_예외가_발생한다() throws Exception {
        Path tempJson = createTempJsonFile();

        String[] args = {
                "--input", tempJson.toString(),
                "--root-class", "123Invalid",              // 여기서 걸려야 함
                "--package", "com.org.example.entity",
                "--out", "build/generated"
        };

        ArgumentParser parser = new ArgumentParser();

        assertThatThrownBy(() -> parser.parse(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("--root-class 값이 유효한 자바 클래스명이 아닙니다");
    }
}
