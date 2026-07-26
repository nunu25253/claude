package com.buzzanalysis.domain.preprocessing;

import com.buzzanalysis.domain.normalization.NormalizedPost;

/**
 * 「AI分析用前処理」ユースケースを表すドメインサービス（Phase2）。
 * {@link NormalizedPost}（Phase1の成果物）を入力とし、{@link PreprocessedPost} を出力する。
 */
public interface PostPreprocessor {

    PreprocessedPost preprocess(NormalizedPost normalizedPost);
}
