package org.example.cli;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ArgumentParserTest {

    @Test
    void 모든_옵션을_정상_파싱한다() throws Exception {
        // 테스트용 임시 JSON 파일 생성
        Path tempJson = Files.createTempFile("sample-json-", ".json");
        Files.writeString(tempJson, "{\"name\":\"Alice\",\"age\":20}", StandardCharsets.UTF_8);

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
    void 지원하지_않는_옵션이_들어오면_예외가_발생한다() {
        String[] args = {
                "--input", "samples/weatherapi.json",
                "--root-class", "WeatherApiResponse",
                "--unknown", "value",
                "--out", "build/generated",
                "--package", "com.org.example.entity"
        };

        ArgumentParser parser = new ArgumentParser();

        assertThatThrownBy(() -> parser.parse(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지원하지 않는 옵션");
    }

    @Test
    void 옵션이_중복되면_예외가_발생한다() {
        String[] args = {
                "--input", "samples/weatherapi.json",
                "--input", "samples/other.json",
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
    void root_class가_유효한_자바_식별자가_아니면_예외가_발생한다() {
        String[] args = {
                "--input", "samples/weatherapi.json",
                "--root-class", "123Invalid", // 숫자로 시작
                "--package", "com.org.example.entity",
                "--out", "build/generated"
        };

        ArgumentParser parser = new ArgumentParser();

        assertThatThrownBy(() -> parser.parse(args))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("--root-class 값이 유효한 자바 클래스명이 아닙니다");
    }
}