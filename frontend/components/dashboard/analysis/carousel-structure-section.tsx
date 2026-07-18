import { Card, CardHeader } from "@/components/ui/card";
import type { CarouselStructureSlide } from "@/lib/types";

export function CarouselStructureSection({ slides }: { slides: CarouselStructureSlide[] }) {
  if (slides.length === 0) return null;

  return (
    <Card>
      <CardHeader title="🖼️ カルーセル構成分析" description="スライド別の役割" />
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4">
        {slides
          .slice()
          .sort((a, b) => a.order - b.order)
          .map((slide) => (
            <div key={slide.order} className="rounded-lg border border-slate-100 p-3">
              <div className="mb-1.5 flex items-center gap-1.5">
                <span className="flex h-6 w-6 items-center justify-center rounded-full bg-brand-100 text-xs font-bold text-brand-700">
                  {slide.order}
                </span>
                <span className="text-xs font-semibold text-slate-700">{slide.role}</span>
              </div>
              <p className="text-xs text-slate-500">{slide.description}</p>
            </div>
          ))}
      </div>
    </Card>
  );
}
