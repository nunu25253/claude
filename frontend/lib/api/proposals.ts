import { apiClient } from "../api-client";
import type {
  Carousel,
  CarouselGenerationRequest,
  ContentProposal,
  ProposalGenerationRequest,
  ScriptGenerationRequest,
  VideoScript,
} from "../types";

export const proposalsApi = {
  generate: (payload: ProposalGenerationRequest) =>
    apiClient.post<ContentProposal[]>("/proposals/generate", payload),
  findByGenerationId: (generationId: string) =>
    apiClient.get<ContentProposal[]>(`/proposals/${generationId}`),
  generateScript: (payload: ScriptGenerationRequest) =>
    apiClient.post<VideoScript>("/scripts/generate", payload),
  generateCarousel: (payload: CarouselGenerationRequest) =>
    apiClient.post<Carousel>("/carousels/generate", payload),
};
