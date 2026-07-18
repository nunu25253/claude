package com.buzzanalysis.domain.preprocessing;

import com.buzzanalysis.domain.preprocessing.steps.EmojiRemovalStep;
import com.buzzanalysis.domain.preprocessing.steps.HtmlRemovalStep;
import com.buzzanalysis.domain.preprocessing.steps.UrlRemovalStep;
import com.buzzanalysis.domain.preprocessing.steps.WhitespaceNormalizationStep;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** {@link DefaultTextCleaner}（テキストクレンジングパイプライン）の単体テスト。 */
class DefaultTextCleanerTest {

    private final DefaultTextCleaner cleaner = new DefaultTextCleaner(List.of(
            new HtmlRemovalStep(), new UrlRemovalStep(), new EmojiRemovalStep(), new WhitespaceNormalizationStep()
    ));

    @Test
    void clean_appliesAllStepsInOrder() {
        String raw = "<b>新商品発売🎉</b>\n\n詳細は https://example.com/new をチェック！\n\n\n#新商品  #お得情報";

        String result = cleaner.clean(raw);

        assertThat(result).doesNotContain("<b>", "</b>", "🎉", "https://");
        assertThat(result).contains("新商品発売", "詳細は", "をチェック！", "#新商品", "#お得情報");
        assertThat(result).doesNotContain("\n\n\n");
    }

    @Test
    void clean_returnsEmptyString_whenInputIsNull() {
        assertThat(cleaner.clean(null)).isEmpty();
    }
}
