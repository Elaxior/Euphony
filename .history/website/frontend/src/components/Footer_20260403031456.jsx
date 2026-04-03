import { motion } from "framer-motion";

export default function Footer() {
  const year = new Date().getFullYear();

  return (
    <footer className="section-shell mt-14 sm:mt-20">
      <motion.div
        initial={{ opacity: 0, y: 14 }}
        whileInView={{ opacity: 1, y: 0 }}
        viewport={{ once: true, amount: 0.8 }}
        transition={{ duration: 0.4 }}
        className="glass-panel modern-surface flex flex-col items-center justify-between gap-3 px-4 py-4 text-center text-xs text-slate-400 sm:px-6 sm:py-5 sm:text-sm md:flex-row md:items-center md:text-left"
      >
        <p>© {year} Euphony. All rights reserved.</p>
        <p>Built with React, Tailwind CSS, Three.js, and Node.js</p>
      </motion.div>
    </footer>
  );
}
