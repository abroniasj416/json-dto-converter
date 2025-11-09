package org.example.cli;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentParserTest {

    @Test
    void 모든_옵션을_정상_파싱한다() {
        String[] args = {
                "--input", "samples/weatherapi.json",
                "--root-class", "WeatherApiResponse",
                "--package", "com.team606.mrdinner.entity",
                "--out", "build/generated",
                "--inner-classes", "true"
        };

        ArgumentParser parser = new ArgumentParser();
        ParsedArguments parsed = parser.parse(args);

        assertThat(parsed.getInputPath()).isEqualTo("samples/weatherapi.json");
        assertThat(parsed.getRootClass()).isEqualTo("WeatherApiResponse");
        assertThat(parsed.getPackageName()).isEqualTo("com.team606.mrdinner.entity");
        assertThat(parsed.getOutDir()).isEqualTo("build/generated");
        assertThat(parsed.isInnerClasses()).isTrue();
    }
}