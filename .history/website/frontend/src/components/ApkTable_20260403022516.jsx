import { Download } from "lucide-react";

export default function ApkTable({ entries, resolveDownloadUrl, formatBytes }) {
  return (
    <div className="glass-panel overflow-hidden">
      <div className="overflow-x-auto">
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
