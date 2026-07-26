package com.buzzanalysis.domain.preprocessing.steps;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 各 {@link TextCleaningStep} 実装の単体テスト。 */
class TextCleaningStepsTest {

    @Test
    void emojiRemovalStep_removesEmojiButKeepsText() {
        String result = new EmojiRemovalStep().apply("今日は最高の一日でした🎉😊✨ #感謝");
        assertThat(result).isEqualTo("今日は最高の一日でした #感謝");
    }

    @Test
    void urlRemovalStep_removesHttpAndHttpsUrls() {
        String result = new UrlRemovalStep().apply("詳細はこちら https://example.com/path?x=1 をチェック");
        assertThat(result).isEqualTo("詳細はこちら  をチェック");
    }

    @Test
    void htmlRemovalStep_stripsTagsAndDecodesEntities() {
        String result = new HtmlRemovalStep().apply("<p>Tom &amp; Jerry</p><br/>");
        assertThat(result).isEqualTo("Tom & Jerry");
    }

    @Test
    void whitespaceNormalizationStep_collapsesNewlinesAndSpaces() {
        String result = new WhitespaceNormalizationStep().apply("行1  \n\n\n行2　　行3\n  ");
        assertThat(result).isEqualTo("行1\n行2 行3");
    }
}
