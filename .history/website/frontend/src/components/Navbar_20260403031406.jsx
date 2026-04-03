import { Download } from "lucide-react";
import { navLinks } from "../data/mock";

export default function Navbar({ latestApk, resolveDownloadUrl }) {
  return (
    <nav className="section-shell sticky top-2 z-40 pt-3 md:top-4 md:pt-4">
      <div className="glass-panel flex items-center justify-between gap-2 px-3 py-2 sm:px-4 md:px-6 md:py-3">
        <a href="#" className="flex min-w-0 items-center gap-2">
          <span className="h-10 w-10 overflow-hidden rounded-lg border border-white/20 bg-white/5">
            <img
              src="/logo.webp"
              alt="Euphony logo"
              className="h-full w-full scale-[1.35] object-cover"
            />
          </span>
          <span className="truncate font-heading text-base font-bold tracking-tight text-white md:text-lg">Euphony</span>
        </a>

        <div className="hidden items-center gap-6 text-sm text-slate-300 md:flex">
          {navLinks.map((link) => (
            <a key={link.href} href={link.href} className="transition hover:text-white">
              {link.label}
            </a>
          ))}
        </div>

        <a
          href={resolveDownloadUrl(latestApk?.downloadUrl)}
          download={latestApk?.fileName || true}
          className="inline-flex shrink-0 items-center gap-2 rounded-xl border border-accent-300/40 bg-accent-500/15 px-3 py-2 text-xs font-semibold text-accent-100 transition hover:border-accent-200/60 hover:bg-accent-500/25 sm:px-4 sm:text-sm"
        >
          <Download size={15} />
          <span className="hidden sm:inline">Download APK</span>
          <span className="sm:hidden">APK</span>
        </a>
      </div>
    </nav>
  );
}
