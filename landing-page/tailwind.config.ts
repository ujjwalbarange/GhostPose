import type { Config } from "tailwindcss";

const config: Config = {
  darkMode: "class",
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        ghost: {
          bg: "#0A0A0C",
          surface: "#121216",
          card: "#18191E",
          elevated: "#202229",
          emerald: "#10B981",
          mint: "#6FFBBE",
          forest: "#005236",
          sky: "#0EA5E9",
          blue: "#0284C7",
          border: "rgba(255, 255, 255, 0.08)",
        },
        calm: {
          surface: "#F8F9FA",
          lowest: "#FFFFFF",
          low: "#F3F4F5",
          container: "#EDEEEF",
          high: "#E7E8E9",
          highest: "#E1E3E4",
          onSurface: "#191C1D",
          onSurfaceVariant: "#45474B",
          inverseSurface: "#2E3132",
          primary: "#000000",
        },
      },
      fontFamily: {
        sans: [
          "var(--font-inter)",
          "Plus Jakarta Sans",
          "-apple-system",
          "BlinkMacSystemFont",
          "sans-serif",
        ],
        mono: ["var(--font-mono)", "Space Grotesk", "monospace"],
      },
      backgroundImage: {
        "dot-grid-dark":
          "radial-gradient(circle, rgba(255,255,255,0.08) 1.2px, transparent 1.2px)",
        "dot-grid-light":
          "radial-gradient(circle, rgba(15,23,42,0.12) 1.2px, transparent 1.2px)",
        "hero-glow":
          "radial-gradient(ellipse 70% 50% at 50% -10%, rgba(16, 185, 129, 0.18), rgba(14, 165, 233, 0.08), transparent)",
      },
      backgroundSize: {
        "dot-20": "20px 20px",
      },
    },
  },
  plugins: [],
};

export default config;
