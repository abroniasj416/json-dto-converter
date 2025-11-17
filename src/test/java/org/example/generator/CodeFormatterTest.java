package org.example.generator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CodeFormatterTest {

    private final CodeFormatter formatter = new CodeFormatter();

    @Test
    void 개행_문자를_전부_LF로_통일한다() {
        String src = "line1\r\nline2\rline3\n";
        String result = formatter.format(src);

        // line1, line2, line3 각각 LF로 끝나는지만 보장
        assertThat(result).contains("line1\nline2\nline3\n");
        assertThat(result).doesNotContain("\r");
    }

    @Test
    void 줄_끝_공백을_제거한다() {
        String src = "int x = 1;   \nint y = 2;\t\n";
        String result = formatter.format(src);

        // CodeFormatter는 마지막에 빈 줄 1개를 유지하므로 \n\n으로 끝남
        assertThat(result).isEqualTo("int x = 1;\nint y = 2;\n\n");
    }

    @Test
    void 연속된_빈줄을_하나로_축소한다() {
        String src = "class A {\n\n\n    int x;\n}\n";
        String result = formatter.format(src);

        // 중간의 연속된 빈 줄은 하나로 줄이고, 마지막에도 빈 줄 1개 존재
        assertThat(result).isEqualTo("class A {\n\n    int x;\n}\n\n");
    }
}
