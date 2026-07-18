package com.buzzanalysis.domain.user;

import java.util.Optional;
import java.util.UUID;

/**
 * User集約のリポジトリインターフェース（ドメイン層）。実装はinfrastructure層に置く。
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
