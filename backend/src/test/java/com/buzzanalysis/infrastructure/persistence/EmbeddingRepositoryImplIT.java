package com.buzzanalysis.infrastructure.persistence;

import com.buzzanalysis.domain.account.SocialAccount;
import com.buzzanalysis.domain.embedding.Embedding;
import com.buzzanalysis.domain.embedding.EmbeddingResult;
import com.buzzanalysis.domain.embedding.EmbeddingTarget;
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
 * {@code pgvector/pgvector:pg16} イメージを使い、実際の {@code vector} 列への読み書きを検証する。
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
        float[] vector = new float[]{0.1f, -0.25f, 0.5f, 1.75f};

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
                new EmbeddingResult(new float[]{0.1f}, "text-embedding-3-small", 1), "旧本文"));

        embeddingRepository.save(Embedding.createNew(post.getId(), EmbeddingTarget.BODY,
                new EmbeddingResult(new float[]{0.9f}, "text-embedding-3-small", 1), "新本文"));

        List<Embedding> all = embeddingRepository.findAllByPostId(post.getId());
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getSourceText()).isEqualTo("新本文");
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
