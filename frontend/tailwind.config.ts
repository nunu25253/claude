import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./app/**/*.{ts,tsx}",
    "./components/**/*.{ts,tsx}",
    "./lib/**/*.{ts,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50: "#f2f6ff",
          100: "#e6ecff",
          200: "#c4d3ff",
          300: "#9fb6ff",
          400: "#6f8bff",
          500: "#4a63f5",
          600: "#3947d6",
          700: "#2d36ac",
          800: "#252c86",
          900: "#1f2568",
        },
        buzz: {
          low: "#94a3b8",
          mid: "#f59e0b",
          high: "#ef4444",
          top: "#a855f7",
        },
      },
      borderRadius: {
        xl: "0.875rem",
        "2xl": "1.25rem",
      },
      boxShadow: {
        card: "0 1px 2px 0 rgb(0 0 0 / 0.04), 0 1px 3px 0 rgb(0 0 0 / 0.06)",
      },
    },
  },
  plugins: [],
};

export default config;
