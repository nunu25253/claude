package com.buzzanalysis.infrastructure.persistence;

import com.buzzanalysis.domain.account.SocialAccount;
import com.buzzanalysis.domain.embedding.Embedding;
import com.buzzanalysis.domain.embedding.EmbeddingResult;
import com.buzzanalysis.domain.embedding.EmbeddingTarget;
import com.buzzanalysis.domain.embedding.SimilarityMatch;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.infrastructure.persistence.adapter.EmbeddingRepositoryImpl;
import com.buzzanalysis.infrastructure.persistence.adapter.PostRepositoryImpl;
import com.buzzanalysis.infrastructure.persistence.adapter.SocialAccountRepositoryImpl;
import com.buzzanalysis.infrastructure.persistence.mapper.EmbeddingMapper;
import com.buzzanalysis.infrastructure.persistence.mapper.PlatformMapper;
import com.buzzanalysis.infrastructure.persistence.mapper.PostMapper;
import com.buzzanalysis.infrastructure.persistence.mapper.SocialAccountMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link EmbeddingRepositoryImpl}（pgvector連携）のTestcontainers統合テスト。
 * {@code pgvector/pgvector:pg16} イメージを使い、実際の {@code vector} 列への読み書き・
 * コサイン類似検索（Phase4）を検証する。DBスキーマは {@code vector(1536)} で固定されているため
 * （{@code V4__pgvector_embeddings.sql}）、テスト用ベクトルも1536次元で用意する。
 *
 * <p><b>注意:</b> このテストはDockerデーモンが利用可能な環境でのみ実行される。本サンドボックス環境には
 * Dockerがないため、このテストは(他のTestcontainers統合テストと同様に)実行時にスキップ/失敗する。
 * 本番投入前に、Docker利用可能な環境（開発機・CI等）で必ず実行し、pgvectorとの実際の読み書きが
 * 正しく動作することを確認すること。</p>
 */
@Testcontainers
@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostMapper.class, PlatformMapper.class, PostRepositoryImpl.class, SocialAccountMapper.class,
        SocialAccountRepositoryImpl.class, EmbeddingMapper.class, EmbeddingRepositoryImpl.class})
class EmbeddingRepositoryImplIT {

    private static final int DIMENSIONS = 1536;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("buzz_analysis_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PostRepositoryImpl postRepository;

    @Autowired
    private SocialAccountRepositoryImpl socialAccountRepository;

    @Autowired
    private EmbeddingRepositoryImpl embeddingRepository;

    @Test
    void saveAndFindByPostIdAndTarget_roundTripsVectorCorrectly() {
        Post post = createTestPost("it-embed-post-1");
        float[] vector = unitVectorAt(0);

        Embedding saved = embeddingRepository.save(Embedding.createNew(post.getId(), EmbeddingTarget.BODY,
                new EmbeddingResult(vector, "text-embedding-3-small", vector.length), "テスト本文"));

        Optional<Embedding> found = embeddingRepository.findByPostIdAndTarget(post.getId(), EmbeddingTarget.BODY);
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getVector()).containsExactly(vector);
        assertThat(found.get().getSourceText()).isEqualTo("テスト本文");
    }

    @Test
    void save_upsertsExistingRecord_insteadOfCreatingDuplicate() {
        Post post = createTestPost("it-embed-post-2");
        embeddingRepository.save(Embedding.createNew(post.getId(), EmbeddingTarget.BODY,
                new EmbeddingResult(unitVectorAt(0), "text-embedding-3-small", DIMENSIONS), "旧本文"));

        embeddingRepository.save(Embedding.createNew(post.getId(), EmbeddingTarget.BODY,
                new EmbeddingResult(unitVectorAt(1), "text-embedding-3-small", DIMENSIONS), "新本文"));

        List<Embedding> all = embeddingRepository.findAllByPostId(post.getId());
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getSourceText()).isEqualTo("新本文");
    }

    @Test
    void findNearest_returnsClosestPostFirst_byCosineDistance() {
        Post closePost = createTestPost("it-embed-post-near");
        Post farPost = createTestPost("it-embed-post-far");
        // close: query方向(軸0)にほぼ一致するベクトル / far: 直交する軸1方向のベクトル
        embeddingRepository.save(Embedding.createNew(closePost.getId(), EmbeddingTarget.BODY,
                new EmbeddingResult(unitVectorAt(0), "text-embedding-3-small", DIMENSIONS), "近い投稿"));
        embeddingRepository.save(Embedding.createNew(farPost.getId(), EmbeddingTarget.BODY,
                new EmbeddingResult(unitVectorAt(1), "text-embedding-3-small", DIMENSIONS), "遠い投稿"));

        List<SimilarityMatch> results = embeddingRepository.findNearest(EmbeddingTarget.BODY, unitVectorAt(0), 2);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).postId()).isEqualTo(closePost.getId());
        assertThat(results.get(0).similarity()).isCloseTo(1.0, org.assertj.core.data.Offset.offset(0.0001));
        assertThat(results.get(1).postId()).isEqualTo(farPost.getId());
        assertThat(results.get(1).similarity()).isCloseTo(0.0, org.assertj.core.data.Offset.offset(0.0001));
    }

    /** 指定した次元だけ1.0、それ以外は0.0の単位ベクトル（テストの可読性・再現性のため）。 */
    private float[] unitVectorAt(int dimensionIndex) {
        float[] vector = new float[DIMENSIONS];
        vector[dimensionIndex] = 1.0f;
        return vector;
    }

    private Post createTestPost(String externalId) {
        SocialAccount account = socialAccountRepository.save(
                SocialAccount.createNew(Platform.INSTAGRAM, "it-embed-account-" + externalId, "it_embed_user",
                        "IT Embed User", "https://instagram.com/it_embed_user", 1000L, 50L));
        return postRepository.save(new Post(UUID.randomUUID(), account.getId(), Platform.INSTAGRAM, externalId,
                "https://www.instagram.com/reel/" + externalId + "/", OffsetDateTime.now(), "it_embed_user",
                "caption", List.of("tag"), 100L, 10L, 5000L, 5L, 20, null, PostType.REEL,
                OffsetDateTime.now(), OffsetDateTime.now()));
    }
}
