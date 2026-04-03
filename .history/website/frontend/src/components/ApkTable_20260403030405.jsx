import { Download } from "lucide-react";

export default function ApkTable({ entries, resolveDownloadUrl, formatBytes }) {
  return (
    <div className="glass-panel overflow-hidden">
      <div className="md:hidden space-y-3 p-3">
        {entries.map((entry) => (
          <article key={entry.id} className="rounded-2xl border border-white/10 bg-black/30 p-4 text-sm text-slate-200">
            <div className="flex items-start justify-between gap-3">
              <div>
                <p className="font-semibold text-white">{entry.version}</p>
                <p className="mt-1 text-xs uppercase tracking-[0.14em] text-slate-400">{entry.build || "stable"}</p>
              </div>
              <span className="text-xs text-slate-400">
                {new Date(entry.releasedAt).toLocaleDateString(undefined, {
                  year: "numeric",
                  month: "short",
                  day: "numeric"
                })}
              </span>
            </div>
            <p className="mt-3 text-xs text-slate-400">Size: {formatBytes(entry.fileSize)}</p>
            <a
              href={resolveDownloadUrl(entry.downloadUrl)}
              download={entry.fileName || true}
              className="mt-4 inline-flex w-full items-center justify-center gap-2 rounded-lg border border-accent-300/30 bg-accent-500/15 px-3 py-2 text-xs font-semibold text-accent-100 transition hover:border-accent-200/50 hover:bg-accent-500/30"
            >
              <Download size={13} />
              Download APK
            </a>
          </article>
        ))}
      </div>

      <div className="hidden overflow-x-auto md:block">
        <table className="w-full min-w-[640px] text-left text-sm">
          <thead className="bg-white/5 text-xs uppercase tracking-[0.16em] text-slate-400">
            <tr>
              <th className="px-4 py-4">Version</th>
              <th className="px-4 py-4">Build</th>
              <th className="px-4 py-4">Size</th>
              <th className="px-4 py-4">Released</th>
              <th className="px-4 py-4">Download</th>
            </tr>
          </thead>
          <tbody>
            {entries.map((entry) => (
              <tr key={entry.id} className="border-t border-white/10 text-slate-200">
                <td className="px-4 py-4 font-semibold text-white">{entry.version}</td>
                <td className="px-4 py-4 capitalize">{entry.build || "stable"}</td>
                <td className="px-4 py-4">{formatBytes(entry.fileSize)}</td>
                <td className="px-4 py-4">
                  {new Date(entry.releasedAt).toLocaleDateString(undefined, {
                    year: "numeric",
                    month: "short",
                    day: "numeric"
                  })}
                </td>
                <td className="px-4 py-4">
                  <a
                    href={resolveDownloadUrl(entry.downloadUrl)}
                    download={entry.fileName || true}
                    className="inline-flex items-center gap-2 rounded-lg border border-accent-300/30 bg-accent-500/15 px-3 py-2 text-xs font-semibold text-accent-100 transition hover:border-accent-200/50 hover:bg-accent-500/30"
                  >
                    <Download size={13} />
                    APK
                  </a>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
