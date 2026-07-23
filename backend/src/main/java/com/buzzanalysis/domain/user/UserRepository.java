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
}
