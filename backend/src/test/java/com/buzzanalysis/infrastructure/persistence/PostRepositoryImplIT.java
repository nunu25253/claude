package com.buzzanalysis.infrastructure.persistence;

import com.buzzanalysis.domain.account.SocialAccount;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostSearchCriteria;
import com.buzzanalysis.domain.post.PostSearchResult;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.infrastructure.persistence.adapter.PostRepositoryImpl;
import com.buzzanalysis.infrastructure.persistence.adapter.SocialAccountRepositoryImpl;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PostRepositoryImpl} のTestcontainers(PostgreSQL)を使った統合テスト。
 * Flywayマイグレーションを実クローンのPostgreSQLに適用し、Specificationベースの検索が正しく動作することを検証する。
 */
@Testcontainers
@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostMapper.class, PlatformMapper.class, PostRepositoryImpl.class, SocialAccountMapper.class, SocialAccountRepositoryImpl.class})
class PostRepositoryImplIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
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

    @Test
    void saveAndFindByPlatformAndExternalId_roundTripsCorrectly() {
        SocialAccount account = socialAccountRepository.save(
                SocialAccount.createNew(Platform.INSTAGRAM, "it-ext-account", "it_user", "IT User",
                        "https://instagram.com/it_user", 1000L, 50L));

        Post post = new Post(
                UUID.randomUUID(), account.getId(), Platform.INSTAGRAM, "it-ext-post-1",
                "https://www.instagram.com/reel/it-ext-post-1/", OffsetDateTime.now(), "it_user",
                "integration test caption", List.of("integrationtest", "unittest"), 500L, 20L, 10000L, 5L,
                30, null, PostType.REEL, OffsetDateTime.now(), OffsetDateTime.now()
        );

        Post saved = postRepository.save(post);

        Optional<Post> found = postRepository.findByPlatformAndExternalId(Platform.INSTAGRAM, "it-ext-post-1");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getHashtags()).containsExactlyInAnyOrder("integrationtest", "unittest");
        assertThat(found.get().getLikeCount()).isEqualTo(500L);
    }

    @Test
    void search_filtersByHashtagAndSupportsPaging() {
        SocialAccount account = socialAccountRepository.save(
                SocialAccount.createNew(Platform.TIKTOK, "it-ext-account-2", "it_user_2", "IT User 2",
                        "https://tiktok.com/@it_user_2", 2000L, 30L));

        postRepository.save(new Post(UUID.randomUUID(), account.getId(), Platform.TIKTOK, "it-post-a",
                "https://www.tiktok.com/@it_user_2/video/it-post-a", OffsetDateTime.now().minusDays(1), "it_user_2",
                "caption A", List.of("searchtest"), 100L, 5L, 2000L, 1L, 20, null, PostType.VIDEO,
                OffsetDateTime.now(), OffsetDateTime.now()));
        postRepository.save(new Post(UUID.randomUUID(), account.getId(), Platform.TIKTOK, "it-post-b",
                "https://www.tiktok.com/@it_user_2/video/it-post-b", OffsetDateTime.now(), "it_user_2",
                "caption B", List.of("othertag"), 200L, 10L, 3000L, 2L, 25, null, PostType.VIDEO,
                OffsetDateTime.now(), OffsetDateTime.now()));

        PostSearchResult result = postRepository.search(
                new PostSearchCriteria(null, "searchtest", null, Platform.TIKTOK, 0, 10, "publishedAt", false));

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).getExternalId()).isEqualTo("it-post-a");
    }
}
