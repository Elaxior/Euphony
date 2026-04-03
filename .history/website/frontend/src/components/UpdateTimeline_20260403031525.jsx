export default function UpdateTimeline({ entries }) {
  return (
    <div className="glass-panel p-4 sm:p-6">
      <h3 className="font-heading text-lg font-bold text-white sm:text-xl">Release Notes</h3>
      <div className="mt-4 space-y-4 sm:mt-5 sm:space-y-5">
        {entries.map((entry, index) => (
          <div key={entry.id} className="relative pl-5 sm:pl-6">
            {index !== entries.length - 1 ? (
              <span className="absolute left-[7px] top-5 h-[calc(100%+12px)] w-px bg-white/15 sm:h-[calc(100%+16px)]" />
            ) : null}
            <span className="absolute left-0 top-2 h-4 w-4 rounded-full border border-accent-200/60 bg-accent-500/40" />
            <p className="text-sm font-semibold text-white">{entry.version}</p>
            <p className="mt-1 text-xs uppercase tracking-[0.16em] text-slate-400">
              {new Date(entry.releasedAt).toLocaleDateString(undefined, {
                year: "numeric",
                month: "short",
                day: "numeric"
              })}
            </p>
            <p className="mt-2 text-xs text-slate-300 sm:text-sm">{entry.changelog}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
