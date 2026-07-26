package com.buzzanalysis.domain.preprocessing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HashtagAndMentionExtractorTest {

    private final HashtagExtractor hashtagExtractor = new HashtagExtractor();
    private final MentionExtractor mentionExtractor = new MentionExtractor();

    @Test
    void hashtagExtractor_extractsJapaneseAndEnglishHashtags_withoutDuplicates() {
        String text = "新商品発売！ #新商品 #お得情報 #新商品 @brand_official";

        assertThat(hashtagExtractor.extract(text)).containsExactly("新商品", "お得情報");
    }

    @Test
    void hashtagExtractor_returnsEmptyList_whenNoHashtags() {
        assertThat(hashtagExtractor.extract("普通の投稿文です")).isEmpty();
        assertThat(hashtagExtractor.extract(null)).isEmpty();
    }

    @Test
    void mentionExtractor_extractsMentions() {
        String text = "ありがとうございます @creator_a @creator_b さんとコラボしました";

        assertThat(mentionExtractor.extract(text)).containsExactly("creator_a", "creator_b");
    }

    @Test
    void mentionExtractor_returnsEmptyList_whenNoMentions() {
        assertThat(mentionExtractor.extract("メンションなしの投稿")).isEmpty();
    }
}
