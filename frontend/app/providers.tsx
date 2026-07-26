"use client";

import { useState } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { AuthProvider } from "@/lib/auth/auth-context";
import { ServiceWorkerRegister } from "@/components/layout/service-worker-register";

export function Providers({ children }: { children: React.ReactNode }) {
  // QueryClient はコンポーネントの再レンダリングで再生成されないよう useState で保持
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            retry: 1,
            staleTime: 30 * 1000,
            refetchOnWindowFocus: false,
          },
        },
      }),
  );

  return (
    <QueryClientProvider client={queryClient}>
      <ServiceWorkerRegister />
      <AuthProvider>{children}</AuthProvider>
    </QueryClientProvider>
  );
}
