import { apiClient, toQueryParams } from "../api-client";
import type {
  AnalyzePostRequest,
  AnalyzePostResponse,
  Page,
  Post,
  PostSearchParams,
} from "../types";

export const postsApi = {
  analyze: (payload: AnalyzePostRequest) =>
    apiClient.post<AnalyzePostResponse>("/posts/analyze", payload),

  search: (params: PostSearchParams) =>
    apiClient.get<Page<Post>>("/posts/search", { params: toQueryParams(params) }),
};
