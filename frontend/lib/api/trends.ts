import { apiClient } from "../api-client";
import type { TrendQueryParams, TrendResponse } from "../types";

export const trendsApi = {
  get: (params: TrendQueryParams) =>
    apiClient.get<TrendResponse>("/trends", { params }),
};
