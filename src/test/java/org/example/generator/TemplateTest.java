package org.example.generator;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateTest {

    @Test
    void 플레이스홀더를_변수값으로_치환한다() {
        Template t = new Template("Hello, ${name}!");
        String result = t.render(Map.of("name", "World"));

        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    void 맵에_없는_키는_그대로_남긴다() {
        Template t = new Template("Hello, ${name}!");
        String result = t.render(Map.of());

        assertThat(result).isEqualTo("Hello, ${name}!");
    }

    @Test
    void null_값은_빈_문자열로_치환한다() {
        Template t = new Template("Value: ${v}");

        Map<String, String> vars = new HashMap<>();
        vars.put("v", null);  // null 허용

        String result = t.render(vars);

        assertThat(result).isEqualTo("Value: ");
    }

    @Test
    void 닫는_중괄호가_없으면_나머지를_그대로_출력한다() {
        Template t = new Template("Hello, ${name");
        String result = t.render(Map.of("name", "World"));

        assertThat(result).isEqualTo("Hello, ${name");
    }
}
