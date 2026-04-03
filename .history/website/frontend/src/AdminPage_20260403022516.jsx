import { useEffect, useMemo, useState } from "react";
import { Loader2, LockKeyhole, LogOut, UploadCloud } from "lucide-react";

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:4000/api";
const TOKEN_KEY = "euphony_admin_token";

export default function AdminPage() {
  const [token, setToken] = useState(() => sessionStorage.getItem(TOKEN_KEY) || "");
  const [adminKey, setAdminKey] = useState("");
  const [isChecking, setIsChecking] = useState(true);
  const [isLoggingIn, setIsLoggingIn] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [status, setStatus] = useState({ type: "", text: "" });
  const [uploadForm, setUploadForm] = useState({
    version: "",
    build: "stable",
    changelog: "",
    apk: null
  });

  const authenticated = useMemo(() => Boolean(token), [token]);

  useEffect(() => {
    async function verifySession() {
      if (!token) {
        setIsChecking(false);
        return;
      }

      try {
        const response = await fetch(`${API_URL}/auth/verify`, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });

        if (!response.ok) {
          throw new Error("Session invalid");
        }
      } catch {
        sessionStorage.removeItem(TOKEN_KEY);
        setToken("");
      } finally {
        setIsChecking(false);
      }
    }

    verifySession();
  }, [token]);

  async function handleLogin(event) {
    event.preventDefault();
    setIsLoggingIn(true);

    try {
      const response = await fetch(`${API_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ adminKey })
      });

      if (!response.ok) {
        throw new Error("Invalid admin key");
      }

      const payload = await response.json();
      sessionStorage.setItem(TOKEN_KEY, payload.token);
      setToken(payload.token);
      setAdminKey("");
      setStatus({ type: "success", text: "Admin access granted." });
    } catch {
      setStatus({ type: "error", text: "Access denied. Invalid admin key." });
    } finally {
      setIsLoggingIn(false);
    }
  }

  async function handleUpload(event) {
    event.preventDefault();
    if (!uploadForm.apk) {
      setStatus({ type: "error", text: "Choose an APK file first." });
      return;
    }

    setIsUploading(true);
    try {
      const formData = new FormData();
      formData.append("version", uploadForm.version);
      formData.append("build", uploadForm.build);
      formData.append("changelog", uploadForm.changelog);
      formData.append("apk", uploadForm.apk);

      const response = await fetch(`${API_URL}/apks/upload`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`
        },
        body: formData
      });

      if (!response.ok) {
        throw new Error("Upload failed");
      }

      setUploadForm({
        version: "",
        build: "stable",
        changelog: "",
        apk: null
      });
      setStatus({ type: "success", text: "Build uploaded successfully." });
    } catch {
      setStatus({
        type: "error",
        text: "Upload failed. Log in again and verify backend settings."
      });
    } finally {
      setIsUploading(false);
    }
  }

  function handleLogout() {
    sessionStorage.removeItem(TOKEN_KEY);
    setToken("");
    setStatus({ type: "", text: "" });
  }

  return (
    <div className="relative min-h-screen overflow-hidden px-4 py-10 md:px-8">
      <div className="absolute inset-0 -z-10 bg-noise opacity-30 noise-layer" />
      <div className="absolute inset-0 -z-10 soft-grid opacity-15" />

      <div className="mx-auto w-full max-w-2xl">
        <div className="glass-panel p-6 md:p-8">
          <div className="flex items-center justify-between gap-4">
            <div>
              <p className="text-xs uppercase tracking-[0.2em] text-slate-400">Restricted</p>
              <h1 className="font-heading text-3xl font-bold text-white">Euphony Admin</h1>
            </div>
            {authenticated ? (
              <button
                onClick={handleLogout}
                className="inline-flex items-center gap-2 rounded-xl border border-white/20 px-4 py-2 text-sm font-semibold text-slate-100 transition hover:bg-white/10"
              >
                <LogOut size={16} />
                Logout
              </button>
            ) : null}
          </div>

          {isChecking ? (
            <div className="mt-8 inline-flex items-center gap-2 text-slate-300">
              <Loader2 size={16} className="animate-spin" />
              Validating admin session...
            </div>
          ) : null}

          {!isChecking && !authenticated ? (
            <form className="mt-8 space-y-4" onSubmit={handleLogin}>
              <p className="text-sm text-slate-300">
                This page is private. Enter your admin key to continue.
              </p>
              <input
                required
                type="password"
                value={adminKey}
                onChange={(event) => setAdminKey(event.target.value)}
                placeholder="Admin key"
                className="w-full rounded-xl border border-white/15 bg-black/30 px-4 py-3 text-sm text-white outline-none ring-accent-400 transition focus:ring"
              />
              <button
                type="submit"
                disabled={isLoggingIn}
                className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-accent-500 px-4 py-3 font-semibold text-white transition hover:bg-accent-400 disabled:cursor-not-allowed disabled:opacity-70"
              >
                {isLoggingIn ? <Loader2 size={16} className="animate-spin" /> : <LockKeyhole size={16} />}
                Unlock Admin
              </button>
            </form>
          ) : null}

          {!isChecking && authenticated ? (
            <form className="mt-8 space-y-4" onSubmit={handleUpload}>
              <h2 className="font-heading text-xl font-semibold text-white">Upload APK Build</h2>
              <input
                required
                value={uploadForm.version}
                onChange={(event) =>
                  setUploadForm((current) => ({ ...current, version: event.target.value }))
                }
                placeholder="Version (example: v1.3.0)"
                className="w-full rounded-xl border border-white/15 bg-black/30 px-4 py-3 text-sm text-white outline-none ring-accent-400 transition focus:ring"
              />
              <input
                value={uploadForm.build}
                onChange={(event) =>
                  setUploadForm((current) => ({ ...current, build: event.target.value }))
                }
                placeholder="Build channel (stable/beta)"
                className="w-full rounded-xl border border-white/15 bg-black/30 px-4 py-3 text-sm text-white outline-none ring-accent-400 transition focus:ring"
              />
              <textarea
                required
                value={uploadForm.changelog}
                onChange={(event) =>
                  setUploadForm((current) => ({ ...current, changelog: event.target.value }))
                }
                placeholder="Release notes / changelog"
                rows={4}
                className="w-full rounded-xl border border-white/15 bg-black/30 px-4 py-3 text-sm text-white outline-none ring-accent-400 transition focus:ring"
              />
              <label className="block rounded-xl border border-dashed border-white/20 bg-black/20 p-4 text-center text-sm text-slate-300">
                <UploadCloud className="mx-auto mb-2" size={18} />
                {uploadForm.apk ? uploadForm.apk.name : "Choose APK file"}
                <input
                  required
                  type="file"
                  accept=".apk"
                  className="hidden"
                  onChange={(event) =>
                    setUploadForm((current) => ({
                      ...current,
                      apk: event.target.files?.[0] || null
                    }))
                  }
                />
              </label>
              <button
                type="submit"
                disabled={isUploading}
                className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-cyan-500/80 px-4 py-3 font-semibold text-slate-950 transition hover:bg-cyan-400 disabled:cursor-not-allowed disabled:opacity-70"
              >
                {isUploading ? <Loader2 size={16} className="animate-spin" /> : null}
                Publish Build
              </button>
            </form>
          ) : null}

          {status.text ? (
            <p
              className={`mt-6 rounded-xl border px-4 py-3 text-sm ${
                status.type === "success"
                  ? "border-emerald-400/40 bg-emerald-500/20 text-emerald-100"
                  : "border-rose-400/40 bg-rose-500/20 text-rose-100"
              }`}
            >
              {status.text}
            </p>
          ) : null}
        </div>
      </div>
    </div>
  );
}