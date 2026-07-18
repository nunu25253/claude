package com.buzzanalysis.application.rag.dto;

/** RAG質問応答APIのリクエストDTO（Phase16）。{@code topK}省略時は既定値を使用する。 */
public record RagQueryRequest(String question, Integer topK) {
}
