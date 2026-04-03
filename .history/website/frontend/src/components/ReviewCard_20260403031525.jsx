import { Star } from "lucide-react";

export default function ReviewCard({ review }) {
  const formattedDate = new Date(review.createdAt).toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric"
  });

  return (
    <article className="glass-panel p-5">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h4 className="font-semibold text-white">{review.name}</h4>
          <p className="text-xs text-slate-400">{formattedDate}</p>
        </div>
        <div className="inline-flex items-center gap-1 rounded-lg bg-white/10 px-2 py-1 text-xs text-amber-300">
          <Star size={12} fill="currentColor" />
          {review.rating}
        </div>
      </div>
      <h5 className="mt-4 font-heading text-base font-semibold text-white">{review.title}</h5>
      <p className="mt-2 text-sm leading-relaxed text-slate-300">{review.comment}</p>
    </article>
  );
}
