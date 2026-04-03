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
      <p className="text-[10px] font-semibold uppercase tracking-[0.18em] text-accent-300 sm:text-xs sm:tracking-[0.22em]">{eyebrow}</p>
      <h2 className="mt-3 font-heading text-2xl font-extrabold text-white sm:text-3xl md:text-4xl">{title}</h2>
      <p className="mt-3 max-w-2xl text-sm text-slate-300 sm:mt-4 sm:text-base">{subtitle}</p>
    </motion.div>
  );
}
