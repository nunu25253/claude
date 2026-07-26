import { useMutation } from "@tanstack/react-query";
import { proposalsApi } from "@/lib/api";
import type {
  CarouselGenerationRequest,
  ProposalGenerationRequest,
  ScriptGenerationRequest,
} from "@/lib/types";

export function useGenerateProposals() {
  return useMutation({
    mutationFn: (payload: ProposalGenerationRequest) => proposalsApi.generate(payload),
  });
}

export function useGenerateScript() {
  return useMutation({
    mutationFn: (payload: ScriptGenerationRequest) => proposalsApi.generateScript(payload),
  });
}

export function useGenerateCarousel() {
  return useMutation({
    mutationFn: (payload: CarouselGenerationRequest) => proposalsApi.generateCarousel(payload),
  });
}
