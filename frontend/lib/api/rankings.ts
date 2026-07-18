import { apiClient } from "../api-client";
import type { RankingItem, RankingQueryParams } from "../types";

export const rankingsApi = {
  list: (params: RankingQueryParams) =>
    apiClient.get<RankingItem[]>("/rankings", { params }),
};
