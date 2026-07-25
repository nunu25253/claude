import { apiClient } from "../api-client";
import type { DataMode } from "../types";

export const systemApi = {
  getDataMode: () => apiClient.get<DataMode>("/system/data-mode"),
};
