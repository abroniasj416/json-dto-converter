package org.example.json;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultNameConverterTest {

    private final DefaultNameConverter converter = new DefaultNameConverter();

    @Test
    void snake_case를_PascalCase로_변환한다() {
        // "profit_rate" -> "ProfitRate"
        assertThat(converter.toPascalCase("profit_rate")).isEqualTo("ProfitRate");
    }

    @Test
    void snake_case를_camelCase로_변환한다() {
        // "profit_rate" -> "profitRate"
        assertThat(converter.toCamelCase("profit_rate")).isEqualTo("profitRate");
    }

    @Test
    void 구분자가_여러_종류여도_잘_처리한다() {
        // "user-name id" -> "UserNameId" / "userNameId"
        assertThat(converter.toPascalCase("user-name id")).isEqualTo("UserNameId");
        assertThat(converter.toCamelCase("user-name id")).isEqualTo("userNameId");
    }

    @Test
    void null_또는_빈문자열은_그대로_반환한다() {
        assertThat(converter.toPascalCase(null)).isNull();
        assertThat(converter.toPascalCase("")).isEmpty();
        assertThat(converter.toCamelCase(null)).isNull();
        assertThat(converter.toCamelCase("")).isEmpty();
    }
}
