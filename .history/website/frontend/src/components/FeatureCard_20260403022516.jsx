import { motion } from "framer-motion";

export default function FeatureCard({ feature }) {
  return (
    <motion.article
      initial={{ opacity: 0, y: 18 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, amount: 0.3 }}
      transition={{ duration: 0.45 }}
      className="glass-panel group p-6 transition hover:border-accent-300/50 hover:bg-white/10"
    >
      <div className="h-2 w-14 rounded-full bg-gradient-to-r from-accent-500 to-cyanGlow" />
      <h3 className="mt-5 font-heading text-xl font-bold text-white">{feature.title}</h3>
      <p className="mt-3 text-sm leading-relaxed text-slate-300">{feature.description}</p>
    </motion.article>
  );
}
