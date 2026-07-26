package com.buzzanalysis.domain.organization;

/** 組織内でのメンバーの権限。OWNERのみメンバーの招待・削除が可能。 */
public enum OrganizationRole {
    OWNER,
    MEMBER
}
