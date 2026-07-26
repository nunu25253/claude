package com.buzzanalysis.domain.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User集約のリポジトリインターフェース（ドメイン層）。実装はinfrastructure層に置く。
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    /** 複数ユーザーをまとめて取得する（組織メンバー一覧等でのN+1回避用）。 */
    List<User> findByIdIn(List<UUID> ids);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * ユーザーを削除する。関連データ(保存済み分析・チーム所属・購読・各種トークン等)は
     * DBのON DELETE CASCADE/SET NULL設定により整合的に処理される
     * (V24__account_deletion_cascades.sql参照)。
     */
    void deleteById(UUID id);
}
