package com.buzzanalysis.domain.organization;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 複数ユーザーが所属できる組織(チーム)を表す集約。マルチアカウント/チーム機能の土台。
 * 現時点では「名前を持つ箱」以上の責務は持たず、メンバー管理は{@link OrganizationMembership}が担う。
 */
public class Organization {

    private final UUID id;
    private String name;
    private final OffsetDateTime createdAt;

    public Organization(UUID id, String name, OffsetDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public static Organization createNew(String name) {
        return new Organization(UUID.randomUUID(), name, OffsetDateTime.now());
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
