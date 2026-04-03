import { motion } from "framer-motion";

export default function FeatureCard({ feature }) {
  return (
    <motion.article
      initial={{ opacity: 0, y: 18 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, amount: 0.3 }}
      transition={{ duration: 0.45 }}
      whileHover={{ y: -8, scale: 1.01 }}
      className="glass-panel modern-surface group relative overflow-hidden p-6 transition hover:border-cyanGlow/40"
    >
      <div className="absolute -right-8 -top-8 h-24 w-24 rounded-full bg-cyanGlow/20 blur-2xl transition duration-500 group-hover:bg-cyanGlow/35" />
      <div className="h-2 w-16 rounded-full bg-gradient-to-r from-cyanGlow via-accent-400 to-emerald-300" />
      <h3 className="mt-5 font-heading text-xl font-bold text-white">{feature.title}</h3>
      <p className="mt-3 text-sm leading-relaxed text-slate-300">{feature.description}</p>
      <p className="mt-6 text-xs font-semibold uppercase tracking-[0.18em] text-cyanGlow/80">Engineered for smooth motion</p>
    </motion.article>
  );
}
