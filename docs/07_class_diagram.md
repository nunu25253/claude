# 07. クラス図

本ドキュメントは主要なドメインモデルおよび、SNS抽象化（Strategy/Factory）・BuzzScore算出（Strategy）・レポート出力（Strategy/Builder）まわりの設計をクラス図として示します。パッケージ構成は `06_directory_structure.md` に対応します。

## 1. SocialPlatform 抽象化とプラットフォーム実装（Strategy + Factory パターン）

新規SNS追加時は `SocialPlatform` を実装するクラスを1つ追加し、`PlatformFactory` に登録するだけで、検索・投稿分析・競合分析などの既存ユースケースにそのまま組み込める設計です。

```mermaid
classDiagram
    class SocialPlatform {
        <<interface>>
        +getPlatformCode() PlatformCode
        +fetchPostByUrl(postUrl: String) PostData
        +fetchPostById(platformPostId: String) PostData
        +fetchAccountPosts(accountId: String, since: Instant) List~PostData~
        +fetchAccountProfile(accountId: String) SocialAccountData
        +searchPostsByKeyword(keyword: String) List~PostData~
        +searchPostsByHashtag(hashtag: String) List~PostData~
        +supports(platformCode: PlatformCode) boolean
    }

    class AbstractSocialPlatform {
        <<abstract>>
        #apiClient: PlatformApiClient
        #rateLimiter: RateLimiter
        +fetchPostByUrl(postUrl: String) PostData
        #parsePostUrl(postUrl: String) String
        #applyRateLimit() void
    }

    class InstagramService {
        -InstagramApiClient apiClient
        -InstagramPostMapper mapper
        +getPlatformCode() PlatformCode
        +fetchPostByUrl(postUrl: String) PostData
        +fetchAccountPosts(accountId: String, since: Instant) List~PostData~
    }

    class TikTokService {
        -TikTokApiClient apiClient
        -TikTokPostMapper mapper
        +getPlatformCode() PlatformCode
        +fetchPostByUrl(postUrl: String) PostData
        +fetchAccountPosts(accountId: String, since: Instant) List~PostData~
    }

    class XService {
        -XApiClient apiClient
        -XPostMapper mapper
        +getPlatformCode() PlatformCode
        +fetchPostByUrl(postUrl: String) PostData
        +fetchAccountPosts(accountId: String, since: Instant) List~PostData~
    }

    class YouTubeService {
        <<future>>
        +getPlatformCode() PlatformCode
    }

    class PlatformApiClient {
        <<interface>>
        +get(path: String, params: Map) HttpResponse
    }

    class PlatformFactory {
        -Map~PlatformCode, SocialPlatform~ platforms
        +PlatformFactory(platforms: List~SocialPlatform~)
        +resolve(platformCode: PlatformCode) SocialPlatform
        +resolveFromUrl(postUrl: String) SocialPlatform
    }

    class PlatformCode {
        <<enumeration>>
        INSTAGRAM
        TIKTOK
        X
        YOUTUBE
        PINTEREST
        THREADS
    }

    class PostData {
        <<DTO>>
        +platformPostId: String
        +postUrl: String
        +postType: PostType
        +caption: String
        +postedAt: Instant
        +likeCount: long
        +commentCount: long
        +viewCount: Long
        +shareCount: Long
        +videoDurationSec: Integer
        +imageCount: Integer
        +hashtags: List~String~
        +rawMetadata: Map
    }

    SocialPlatform <|.. AbstractSocialPlatform
    AbstractSocialPlatform <|-- InstagramService
    AbstractSocialPlatform <|-- TikTokService
    AbstractSocialPlatform <|-- XService
    AbstractSocialPlatform <|-- YouTubeService
    AbstractSocialPlatform --> PlatformApiClient
    SocialPlatform --> PostData : returns
    SocialPlatform --> PlatformCode
    PlatformFactory --> SocialPlatform : resolves
    PlatformFactory --> PlatformCode
```

## 2. BuzzScore 算出（Strategy パターン）

```mermaid
classDiagram
    class BuzzScoreStrategy {
        <<interface>>
        +calculate(context: BuzzScoreContext) StrategyScore
        +getWeight() double
        +getName() String
    }

    class EngagementRateStrategy {
        +calculate(context: BuzzScoreContext) StrategyScore
    }
    class ViewCountStrategy {
        +calculate(context: BuzzScoreContext) StrategyScore
    }
    class CommentRateStrategy {
        +calculate(context: BuzzScoreContext) StrategyScore
    }
    class FormatStrategy {
        +calculate(context: BuzzScoreContext) StrategyScore
    }
    class HashtagStrategy {
        +calculate(context: BuzzScoreContext) StrategyScore
    }
    class PostingTimeStrategy {
        +calculate(context: BuzzScoreContext) StrategyScore
    }
    class TextStructureStrategy {
        +calculate(context: BuzzScoreContext) StrategyScore
    }
    class AiInsightStrategy {
        +calculate(context: BuzzScoreContext) StrategyScore
    }

    class BuzzScoreContext {
        <<DTO>>
        +post: Post
        +postMetrics: PostMetrics
        +analysisResult: AnalysisResult
        +accountBaseline: CompetitorStats
    }

    class StrategyScore {
        <<DTO>>
        +strategyName: String
        +rawScore: double
        +weight: double
    }

    class BuzzScoreCalculator {
        -List~BuzzScoreStrategy~ strategies
        +BuzzScoreCalculator(strategies: List~BuzzScoreStrategy~)
        +calculate(context: BuzzScoreContext) BuzzScore
        -aggregate(scores: List~StrategyScore~) double
    }

    BuzzScoreStrategy <|.. EngagementRateStrategy
    BuzzScoreStrategy <|.. ViewCountStrategy
    BuzzScoreStrategy <|.. CommentRateStrategy
    BuzzScoreStrategy <|.. FormatStrategy
    BuzzScoreStrategy <|.. HashtagStrategy
    BuzzScoreStrategy <|.. PostingTimeStrategy
    BuzzScoreStrategy <|.. TextStructureStrategy
    BuzzScoreStrategy <|.. AiInsightStrategy
    BuzzScoreCalculator o-- "8" BuzzScoreStrategy : composes
    BuzzScoreCalculator --> BuzzScoreContext : uses
    BuzzScoreStrategy --> StrategyScore : returns
    BuzzScoreCalculator --> BuzzScore : produces
```

## 3. ドメインエンティティ関連（集約ルートと関連エンティティ）

```mermaid
classDiagram
    class Post {
        +id: UUID
        +socialAccountId: UUID
        +platformId: UUID
        +platformPostId: String
        +postUrl: String
        +postType: PostType
        +caption: String
        +postedAt: Instant
        +videoDurationSec: Integer
        +imageCount: Integer
        +likeCount: long
        +commentCount: long
        +viewCount: Long
        +shareCount: Long
        +engagementRate: BigDecimal
        +hashtags: List~Hashtag~
        +addMetricsSnapshot(snapshot: PostMetrics) void
        +calculateEngagementRate() BigDecimal
    }

    class PostType {
        <<enumeration>>
        REEL
        IMAGE
        VIDEO
        CAROUSEL
        TEXT
    }

    class PostMetrics {
        +id: UUID
        +postId: UUID
        +capturedAt: Instant
        +likeCount: long
        +commentCount: long
        +viewCount: Long
        +shareCount: Long
        +engagementRate: BigDecimal
    }

    class Hashtag {
        +id: UUID
        +name: String
        +normalizedName: String
        +usageCount: long
    }

    class SocialAccount {
        +id: UUID
        +platformId: UUID
        +platformAccountId: String
        +username: String
        +displayName: String
        +profileUrl: String
        +followerCount: Long
        +genre: String
        +isTracked: boolean
    }

    class Platform {
        +id: UUID
        +code: PlatformCode
        +name: String
        +apiProvider: String
        +isActive: boolean
    }

    class AnalysisResult {
        +id: UUID
        +postId: UUID
        +requestedByUserId: UUID
        +aiModel: String
        +summary: String
        +targetAudience: String
        +hookAnalysis: String
        +ctaAnalysis: String
        +sentimentAnalysis: SentimentAnalysis
        +videoStructureAnalysis: VideoStructureAnalysis
        +carouselStructureAnalysis: CarouselStructureAnalysis
        +titleAnalysis: String
        +textAnalysis: String
        +postingTimeAnalysis: String
        +hashtagAnalysis: Map
        +improvementSuggestions: List~ImprovementSuggestion~
        +similarPosts: List~UUID~
        +status: AnalysisStatus
    }

    class BuzzScore {
        +id: UUID
        +postId: UUID
        +analysisResultId: UUID
        +totalScore: BigDecimal
        +breakdown: BuzzScoreBreakdown
        +strategyVersion: String
    }

    class CompetitorStats {
        +id: UUID
        +socialAccountId: UUID
        +periodType: String
        +periodStart: LocalDate
        +periodEnd: LocalDate
        +avgLikeCount: BigDecimal
        +avgCommentCount: BigDecimal
        +postingFrequencyPerWeek: BigDecimal
        +avgVideoDurationSec: BigDecimal
        +avgCaptionLength: BigDecimal
    }

    class Ranking {
        +id: UUID
        +rankingType: RankingType
        +platformId: UUID
        +genre: String
        +periodStart: Instant
        +periodEnd: Instant
        +entries: List~RankingEntry~
    }

    class RankingEntry {
        +id: UUID
        +postId: UUID
        +rankPosition: int
        +score: BigDecimal
    }

    class Report {
        +id: UUID
        +userId: UUID
        +analysisResultId: UUID
        +title: String
        +format: ReportFormat
        +fileUrl: String
        +status: String
    }

    class SavedAnalysis {
        +id: UUID
        +userId: UUID
        +analysisResultId: UUID
        +folderName: String
        +memo: String
    }

    class User {
        +id: UUID
        +email: String
        +name: String
        +role: UserRole
        +plan: UserPlan
    }

    Platform "1" --> "many" SocialAccount : has
    SocialAccount "1" --> "many" Post : publishes
    Post "1" --> "many" PostMetrics : tracks over time
    Post "many" --> "many" Hashtag : tagged with
    Post "1" --> "many" AnalysisResult : analyzed into
    AnalysisResult "1" --> "1" BuzzScore : produces
    AnalysisResult "1" --> "many" Report : exported as
    AnalysisResult "1" --> "many" SavedAnalysis : bookmarked as
    SocialAccount "1" --> "many" CompetitorStats : aggregated into
    Ranking "1" --> "many" RankingEntry : contains
    RankingEntry --> Post : references
    User "1" --> "many" SavedAnalysis : owns
    User "1" --> "many" Report : requests
    Post --> PostType
```

## 4. アプリケーション層：ユースケース・Command・Repository・DI の関係

Repository（インターフェースはdomain層、実装はinfrastructure層）、Command（ユースケース入力のカプセル化）、Observer（ドメインイベント発行・購読）、DIによる疎結合を示します。

```mermaid
classDiagram
    class AnalyzePostUrlCommand {
        <<Command>>
        +postUrl: String
        +requestedByUserId: UUID
    }

    class AnalyzePostUrlUseCase {
        -SocialPlatformPort socialPlatformPort
        -PostRepository postRepository
        -AiAnalysisPort aiAnalysisPort
        -BuzzScoreCalculator buzzScoreCalculator
        -AnalysisResultRepository analysisResultRepository
        -ApplicationEventPublisher eventPublisher
        +execute(command: AnalyzePostUrlCommand) AnalysisResult
    }

    class SocialPlatformPort {
        <<interface>>
        +resolveAndFetch(postUrl: String) PostData
    }

    class AiAnalysisPort {
        <<interface>>
        +analyze(post: Post, promptContext: Map) AnalysisResult
    }

    class PostRepository {
        <<interface>>
        +save(post: Post) Post
        +findByPlatformPostId(platformId: UUID, platformPostId: String) Optional~Post~
        +search(criteria: PostSearchCriteria) Page~Post~
    }

    class AnalysisResultRepository {
        <<interface>>
        +save(result: AnalysisResult) AnalysisResult
        +findByPostId(postId: UUID) Optional~AnalysisResult~
    }

    class PostAnalyzedEvent {
        <<DomainEvent>>
        +postId: UUID
        +analysisResultId: UUID
    }

    class ReportAutoGenerationListener {
        <<Observer>>
        +onPostAnalyzed(event: PostAnalyzedEvent) void
    }

    class RankingUpdateListener {
        <<Observer>>
        +onPostAnalyzed(event: PostAnalyzedEvent) void
    }

    class CacheInvalidationListener {
        <<Observer>>
        +onPostAnalyzed(event: PostAnalyzedEvent) void
    }

    class PostRepositoryJpaAdapter {
        <<infrastructure>>
        -PostJpaRepository jpaRepository
        -PostMapper mapper
        +save(post: Post) Post
        +findByPlatformPostId(platformId: UUID, platformPostId: String) Optional~Post~
        +search(criteria: PostSearchCriteria) Page~Post~
    }

    AnalyzePostUrlUseCase --> AnalyzePostUrlCommand : handles
    AnalyzePostUrlUseCase --> SocialPlatformPort : uses (DI)
    AnalyzePostUrlUseCase --> AiAnalysisPort : uses (DI)
    AnalyzePostUrlUseCase --> PostRepository : uses (DI)
    AnalyzePostUrlUseCase --> AnalysisResultRepository : uses (DI)
    AnalyzePostUrlUseCase --> BuzzScoreCalculator : uses (DI)
    AnalyzePostUrlUseCase ..> PostAnalyzedEvent : publishes
    PostRepository <|.. PostRepositoryJpaAdapter : implements
    PostAnalyzedEvent <.. ReportAutoGenerationListener : subscribes
    PostAnalyzedEvent <.. RankingUpdateListener : subscribes
    PostAnalyzedEvent <.. CacheInvalidationListener : subscribes
```

## 5. レポート出力（Strategy + Builder パターン）

```mermaid
classDiagram
    class ReportExportStrategy {
        <<interface>>
        +export(content: ReportContent) ReportFile
        +getFormat() ReportFormat
    }

    class PdfReportExporter {
        +export(content: ReportContent) ReportFile
    }
    class MarkdownReportExporter {
        +export(content: ReportContent) ReportFile
    }
    class HtmlReportExporter {
        +export(content: ReportContent) ReportFile
    }

    class ReportExporterFactory {
        -Map~ReportFormat, ReportExportStrategy~ exporters
        +resolve(format: ReportFormat) ReportExportStrategy
    }

    class ReportContentBuilder {
        <<Builder>>
        -ReportContent content
        +withPostSummary(post: Post) ReportContentBuilder
        +withBuzzScore(score: BuzzScore) ReportContentBuilder
        +withAiAnalysis(result: AnalysisResult) ReportContentBuilder
        +withImprovementSuggestions(list: List) ReportContentBuilder
        +withCompetitorComparison(stats: CompetitorStats) ReportContentBuilder
        +build() ReportContent
    }

    class ReportContent {
        <<DTO>>
        +title: String
        +sections: List~ReportSection~
    }

    class GenerateReportUseCase {
        -ReportContentBuilder contentBuilder
        -ReportExporterFactory exporterFactory
        -StoragePort storagePort
        -ReportRepository reportRepository
        +execute(command: GenerateReportCommand) Report
    }

    ReportExportStrategy <|.. PdfReportExporter
    ReportExportStrategy <|.. MarkdownReportExporter
    ReportExportStrategy <|.. HtmlReportExporter
    ReportExporterFactory --> ReportExportStrategy : resolves
    ReportContentBuilder --> ReportContent : builds
    GenerateReportUseCase --> ReportContentBuilder : uses
    GenerateReportUseCase --> ReportExporterFactory : uses
    GenerateReportUseCase --> ReportContent
```

## 6. 適用パターンまとめ

| パターン | 適用箇所 | 目的 |
|----------|----------|------|
| Repository | `domain.repository.*`（インターフェース） / `infrastructure.persistence.repository.*`（JPA実装） | ドメイン層をDB技術詳細から分離する |
| Factory | `PlatformFactory`、`ReportExporterFactory` | プラットフォームコードや出力形式に応じた実装解決をカプセル化する |
| Strategy | `SocialPlatform` 実装群、`BuzzScoreStrategy` 実装群、`ReportExportStrategy` 実装群 | アルゴリズム・振る舞いの切り替えを容易にし、拡張時の既存コード改修を防ぐ（開放閉鎖の原則） |
| Builder | `AnalysisPromptBuilder`（AIプロンプト構築）、`ReportContentBuilder`（レポート内容構築） | 複数のオプション項目からなる複雑なオブジェクトを段階的に構築する |
| Observer | `PostAnalyzedEvent` とそのリスナー群 | 投稿分析完了後の副作用（レポート自動生成、ランキング更新、キャッシュ無効化）を疎結合に実行する |
| Command | `AnalyzePostUrlCommand`、`GenerateReportCommand` 等 | ユースケースの入力をオブジェクト化し、Undo/監査ログ/非同期キュー投入を容易にする |
| DI（依存性注入） | Spring のコンストラクタインジェクション全般 | インターフェースを介した疎結合を実現し、テスト時にモック差し替えを容易にする |
