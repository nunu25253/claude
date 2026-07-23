import type { components } from "./generated/api";
import type { SavedAnalysis } from "./saved-analysis";

// バックエンドの実装(springdoc生成OpenAPIスキーマ)を一次ソースとする(#58と同じ方針)。
type Schemas = components["schemas"];

export type OrganizationRole = "OWNER" | "MEMBER";

export type CreateOrganizationRequest = Schemas["CreateOrganizationRequest"];
export type InviteMemberRequest = Schemas["InviteMemberRequest"];

// レスポンスDTOはRequiredで実際の必須制約を反映する(サービス側で全フィールドを必ず設定するため)。
export type Organization = Required<Schemas["OrganizationDto"]>;
export type OrganizationMember = Required<Schemas["OrganizationMemberDto"]>;

export interface TeamSavedAnalysis {
  savedAnalysis: SavedAnalysis;
  savedByUserId: string;
  savedByDisplayName: string;
}
