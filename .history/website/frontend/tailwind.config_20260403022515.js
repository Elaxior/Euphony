/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      fontFamily: {
        heading: ["Sora", "sans-serif"],
        body: ["Manrope", "sans-serif"]
      },
      colors: {
        ink: {
          950: "#050509",
          900: "#0b0b15",
          800: "#141423"
        },
        accent: {
          500: "#7c5cff",
          400: "#9e86ff",
          300: "#bdb0ff"
        },
        cyanGlow: "#67d8ff"
      },
      boxShadow: {
        glow: "0 0 40px rgba(124, 92, 255, 0.35)",
        glass: "0 16px 50px rgba(0, 0, 0, 0.28)"
      },
      backgroundImage: {
        noise: "radial-gradient(circle at 1px 1px, rgba(255,255,255,0.08) 1px, transparent 0)"
      }
    }
  },
  plugins: []
};
