import { Star } from "lucide-react";
import { motion } from "framer-motion";

export default function ReviewCard({ review }) {
  const formattedDate = new Date(review.createdAt).toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric"
  });

  return (
    <motion.article
      initial={{ opacity: 0, y: 14 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, amount: 0.2 }}
      transition={{ duration: 0.4 }}
      whileHover={{ y: -6 }}
      className="glass-panel modern-surface relative overflow-hidden p-5"
    >
      <div className="absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-cyanGlow/90 to-transparent" />
      <div className="flex items-center justify-between gap-4">
        <div>
          <h4 className="font-semibold text-white">{review.name}</h4>
          <p className="text-xs text-slate-400">{formattedDate}</p>
        </div>
        <div className="inline-flex items-center gap-1 rounded-lg border border-amber-300/35 bg-amber-300/10 px-2 py-1 text-xs text-amber-300">
          <Star size={12} fill="currentColor" />
          {review.rating}
        </div>
      </div>
      <h5 className="mt-4 font-heading text-base font-semibold text-white">{review.title}</h5>
      <p className="mt-2 text-sm leading-relaxed text-slate-300">{review.comment}</p>
    </motion.article>
  );
}
