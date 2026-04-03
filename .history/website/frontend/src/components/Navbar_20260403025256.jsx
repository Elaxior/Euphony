import { Download } from "lucide-react";
import { navLinks } from "../data/mock";

export default function Navbar({ latestApk, resolveDownloadUrl }) {
  return (
    <nav className="section-shell sticky top-4 z-40 pt-4">
      <div className="glass-panel flex items-center justify-between px-5 py-3 md:px-6">
        <a href="#" className="flex items-center gap-2">
          <span className="h-10 w-10 overflow-hidden rounded-lg border border-white/20 bg-white/5">
            <img
              src="/logo.webp"
              alt="Euphony logo"
              className="h-full w-full scale-[1.35] object-cover"
            />
          </span>
          <span className="font-heading text-lg font-bold tracking-tight text-white">Euphony</span>
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
          className="inline-flex items-center gap-2 rounded-xl border border-accent-300/40 bg-accent-500/15 px-4 py-2 text-sm font-semibold text-accent-100 transition hover:border-accent-200/60 hover:bg-accent-500/25"
        >
          <Download size={15} />
          APK
        </a>
      </div>
    </nav>
  );
}
