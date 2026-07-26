"use client";

import { cn } from "@/lib/utils";

export interface TabItem<T extends string> {
  value: T;
  label: string;
}

interface TabsProps<T extends string> {
  items: TabItem<T>[];
  value: T;
  onChange: (value: T) => void;
}

export function Tabs<T extends string>({ items, value, onChange }: TabsProps<T>) {
  return (
    <div role="tablist" className="flex flex-wrap gap-1 rounded-xl bg-slate-100 p-1">
      {items.map((item) => {
        const isActive = item.value === value;
        return (
          <button
            key={item.value}
            role="tab"
            type="button"
            aria-selected={isActive}
            onClick={() => onChange(item.value)}
            className={cn(
              "rounded-lg px-3.5 py-1.5 text-sm font-medium transition",
              isActive
                ? "bg-white text-brand-700 shadow-card"
                : "text-slate-600 hover:text-slate-700",
            )}
          >
            {item.label}
          </button>
        );
      })}
    </div>
  );
}
