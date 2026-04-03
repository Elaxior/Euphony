import { motion } from "framer-motion";

export default function SectionTitle({ eyebrow, title, subtitle }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 16 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, amount: 0.4 }}
      transition={{ duration: 0.55 }}
      className="max-w-3xl"
    >
      <p className="text-xs font-semibold uppercase tracking-[0.22em] text-accent-300">{eyebrow}</p>
      <h2 className="mt-3 font-heading text-3xl font-extrabold text-white md:text-4xl">{title}</h2>
      <p className="mt-4 text-slate-300">{subtitle}</p>
    </motion.div>
  );
}
