import { motion } from "framer-motion";

export default function LoadingScreen() {
  return (
    <motion.div
      initial={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{ duration: 0.55 }}
      className="fixed inset-0 z-[120] flex items-center justify-center bg-ink-950"
    >
      <div className="text-center">
        <motion.div
          initial={{ scale: 0.8, opacity: 0.4 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ duration: 0.8, repeat: Infinity, repeatType: "reverse" }}
          className="mx-auto h-20 w-20 rounded-3xl bg-gradient-to-br from-accent-500 to-cyanGlow shadow-glow"
        />
        <p className="mt-5 font-heading text-xl font-semibold text-white">Loading Euphony</p>
        <p className="mt-1 text-sm text-slate-400">Preparing your music universe...</p>
      </div>
    </motion.div>
  );
}
