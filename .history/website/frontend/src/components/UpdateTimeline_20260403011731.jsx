export default function UpdateTimeline({ entries }) {
  return (
    <div className="glass-panel p-6">
      <h3 className="font-heading text-xl font-bold text-white">Release Notes</h3>
      <div className="mt-5 space-y-5">
        {entries.map((entry, index) => (
          <div key={entry.id} className="relative pl-6">
            {index !== entries.length - 1 ? (
              <span className="absolute left-[7px] top-5 h-[calc(100%+16px)] w-px bg-white/15" />
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
            <p className="mt-2 text-sm text-slate-300">{entry.changelog}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
