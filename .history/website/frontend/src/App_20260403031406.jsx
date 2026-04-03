import { lazy, Suspense, useEffect, useMemo, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { Download, Loader2, MessageCircle } from "lucide-react";
import Navbar from "./components/Navbar";
import SectionTitle from "./components/SectionTitle";
import FeatureCard from "./components/FeatureCard";
import ScreenshotCarousel from "./components/ScreenshotCarousel";
import ReviewCard from "./components/ReviewCard";
import ApkTable from "./components/ApkTable";
import UpdateTimeline from "./components/UpdateTimeline";
import Footer from "./components/Footer";
import LoadingScreen from "./components/LoadingScreen";
import {
  fallbackApks,
  fallbackReviews,
  featureCards,
  screenshotSlides
} from "./data/mock";

const Hero3D = lazy(() => import("./components/Hero3D"));

const API_URL = import.meta.env.VITE_API_URL || "http://localhost:4000/api";
const API_ROOT = API_URL.endsWith("/api") ? API_URL.slice(0, -4) : API_URL;

function resolveDownloadUrl(url) {
  if (!url || url === "#") return "#";
  if (url.startsWith("http")) return url;
  return `${API_ROOT}${url}`;
}

function formatBytes(bytes) {
  if (!bytes) return "-";
  const units = ["B", "KB", "MB", "GB"];
  let size = bytes;
  let index = 0;
  while (size >= 1024 && index < units.length - 1) {
    size /= 1024;
    index += 1;
  }
  return `${size.toFixed(1)} ${units[index]}`;
}

export default function App() {
  const [bootLoading, setBootLoading] = useState(true);
  const [reviews, setReviews] = useState(fallbackReviews);
  const [apks, setApks] = useState(fallbackApks);
  const [latestApk, setLatestApk] = useState(fallbackApks[0]);
  const [isSubmittingReview, setIsSubmittingReview] = useState(false);
  const [isSubmittingMessage, setIsSubmittingMessage] = useState(false);
  const [reviewForm, setReviewForm] = useState({
    name: "",
    title: "",
    rating: 5,
    comment: ""
  });
  const [messageForm, setMessageForm] = useState({
    name: "",
    email: "",
    message: ""
  });
  const [status, setStatus] = useState({ type: "", text: "" });

  useEffect(() => {
    const timeout = setTimeout(() => setBootLoading(false), 1300);
    return () => clearTimeout(timeout);
  }, []);

  useEffect(() => {
    async function loadData() {
      try {
        const [reviewsRes, apksRes] = await Promise.all([
          fetch(`${API_URL}/reviews`),
          fetch(`${API_URL}/apks`)
        ]);

        if (reviewsRes.ok) {
          const reviewsJson = await reviewsRes.json();
          if (Array.isArray(reviewsJson.items) && reviewsJson.items.length > 0) {
            setReviews(reviewsJson.items);
          }
        }

        if (apksRes.ok) {
          const apksJson = await apksRes.json();
          if (Array.isArray(apksJson.items) && apksJson.items.length > 0) {
            setApks(apksJson.items);
            setLatestApk(apksJson.latest);
          }
        }
      } catch {
        setStatus({
          type: "warn",
          text: "Backend is not running, showing fallback website data."
        });
      }
    }

    loadData();
  }, []);

  const averageRating = useMemo(() => {
    if (!reviews.length) return 0;
    const total = reviews.reduce((sum, review) => sum + Number(review.rating || 0), 0);
    return (total / reviews.length).toFixed(1);
  }, [reviews]);

  async function handleReviewSubmit(event) {
    event.preventDefault();
    setIsSubmittingReview(true);

    try {
      const response = await fetch(`${API_URL}/reviews`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          ...reviewForm,
          rating: Number(reviewForm.rating)
        })
      });

      if (!response.ok) {
        throw new Error("Failed to submit review");
      }

      const created = await response.json();
      setReviews((current) => [created, ...current]);
      setReviewForm({ name: "", title: "", rating: 5, comment: "" });
      setStatus({ type: "success", text: "Review submitted successfully." });
    } catch {
      setStatus({ type: "error", text: "Could not submit review right now." });
    } finally {
      setIsSubmittingReview(false);
    }
  }

  async function handleMessageSubmit(event) {
    event.preventDefault();
    setIsSubmittingMessage(true);

    try {
      const response = await fetch(`${API_URL}/messages`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(messageForm)
      });

      if (!response.ok) {
        throw new Error("Failed to send message");
      }

      setMessageForm({ name: "", email: "", message: "" });
      setStatus({ type: "success", text: "Message received. We will get back to you." });
    } catch {
      setStatus({ type: "error", text: "Could not send message right now." });
    } finally {
      setIsSubmittingMessage(false);
    }
  }

  return (
    <>
      <AnimatePresence>{bootLoading && <LoadingScreen />}</AnimatePresence>

      <div className="relative overflow-hidden pb-20">
        <div className="absolute inset-0 -z-10 bg-noise opacity-30 noise-layer" />
        <div className="absolute inset-0 -z-10 soft-grid opacity-15" />

        <Navbar latestApk={latestApk} resolveDownloadUrl={resolveDownloadUrl} />

        <header className="section-shell grid gap-8 pt-16 sm:pt-20 md:grid-cols-[1.05fr_0.95fr] md:items-center md:pt-28">
          <motion.div
            initial={{ opacity: 0, y: 28 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
          >
            <p className="mb-4 inline-flex rounded-full border border-accent-400/40 bg-accent-500/10 px-3 py-1.5 text-[10px] font-semibold uppercase tracking-[0.18em] text-accent-300 sm:px-4 sm:py-2 sm:text-xs sm:tracking-[0.25em]">
              Free Music App for Android
            </p>
            <h1 className="font-heading text-3xl font-extrabold leading-tight text-white sm:text-4xl md:text-6xl">
              The <span className="gradient-text">premium music experience</span> and Spotify free alternative for Android.
            </h1>
            <p className="mt-5 max-w-xl text-sm text-slate-300 sm:text-base md:mt-6 md:text-lg">
              Stream tracks, manage playlists, queue songs intelligently, and keep your sound
              offline with a modern player crafted for speed and elegance.
            </p>

            <div className="mt-8 flex flex-wrap gap-3 sm:mt-10 sm:gap-4">
              <a
                href={resolveDownloadUrl(latestApk?.downloadUrl)}
                className="inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-accent-500 px-6 py-3 font-bold text-white shadow-glow transition hover:translate-y-[-2px] hover:bg-accent-400 sm:w-auto"
              >
                <Download size={18} />
                Download APK
              </a>
              <a
                href="#download"
                className="inline-flex w-full items-center justify-center rounded-2xl border border-white/20 px-6 py-3 font-semibold text-slate-200 transition hover:border-white/40 hover:bg-white/5 sm:w-auto"
              >
                View Changelog
              </a>
            </div>
          </motion.div>

          <Suspense
            fallback={
              <div className="glass-panel h-[280px] animate-pulse bg-gradient-to-br from-accent-500/20 to-cyan-500/10 sm:h-[320px] md:h-[360px]" />
            }
          >
            <Hero3D />
          </Suspense>
        </header>

        <main className="mt-16 space-y-16 sm:mt-20 sm:space-y-20 md:mt-24 md:space-y-24">
          <section id="features" className="section-shell">
            <SectionTitle
              eyebrow="Core Highlights"
              title="Built to make every listening session feel alive"
              subtitle="A feature set focused on reliability, control, and visual delight."
            />
            <div className="mt-10 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
              {featureCards.map((feature) => (
                <FeatureCard key={feature.title} feature={feature} />
              ))}
            </div>
          </section>

          <section id="screenshots" className="section-shell">
            <SectionTitle
              eyebrow="Interface"
              title="Screens that feel fast, clean, and cinematic"
              subtitle="A quick look at discovery, search, queue actions, and immersive playback."
            />
            <div className="mt-10">
              <ScreenshotCarousel slides={screenshotSlides} />
            </div>
          </section>

          <section id="reviews" className="section-shell">
            <SectionTitle
              eyebrow="Community"
              title="Users rate Euphony"
              subtitle="Read what listeners and testers say about performance, design, and usability."
            />

            <div className="mt-8 grid gap-8 lg:grid-cols-[1.3fr_0.7fr]">
              <div>
                <div className="mb-4 flex items-end gap-2 sm:gap-3">
                  <span className="font-heading text-4xl font-extrabold text-white sm:text-5xl">{averageRating}</span>
                  <span className="mb-2 text-slate-300">/5 from {reviews.length} reviews</span>
                </div>
                <div className="grid gap-4 md:grid-cols-2">
                  {reviews.slice(0, 6).map((review) => (
                    <ReviewCard key={review.id} review={review} />
                  ))}
                </div>
              </div>

              <form className="glass-panel p-6" onSubmit={handleReviewSubmit}>
                <h3 className="font-heading text-xl font-bold">Leave a review</h3>
                <div className="mt-4 space-y-4">
                  <input
                    required
                    value={reviewForm.name}
                    onChange={(event) =>
                      setReviewForm((current) => ({ ...current, name: event.target.value }))
                    }
                    placeholder="Your name"
                    className="w-full rounded-xl border border-white/15 bg-black/30 px-4 py-3 text-sm text-white outline-none ring-accent-400 transition focus:ring"
                  />
                  <input
                    required
                    value={reviewForm.title}
                    onChange={(event) =>
                      setReviewForm((current) => ({ ...current, title: event.target.value }))
                    }
                    placeholder="Review title"
                    className="w-full rounded-xl border border-white/15 bg-black/30 px-4 py-3 text-sm text-white outline-none ring-accent-400 transition focus:ring"
                  />
                  <select
                    value={reviewForm.rating}
                    onChange={(event) =>
                      setReviewForm((current) => ({ ...current, rating: Number(event.target.value) }))
                    }
                    className="w-full rounded-xl border border-white/15 bg-black/30 px-4 py-3 text-sm text-white outline-none ring-accent-400 transition focus:ring"
                  >
                    {[5, 4, 3, 2, 1].map((value) => (
                      <option key={value} value={value}>
                        {value} Star{value > 1 ? "s" : ""}
                      </option>
                    ))}
                  </select>
                  <textarea
                    required
                    value={reviewForm.comment}
                    onChange={(event) =>
                      setReviewForm((current) => ({ ...current, comment: event.target.value }))
                    }
                    placeholder="Tell us how Euphony feels"
                    rows={4}
                    className="w-full rounded-xl border border-white/15 bg-black/30 px-4 py-3 text-sm text-white outline-none ring-accent-400 transition focus:ring"
                  />
                </div>
                <button
                  type="submit"
                  disabled={isSubmittingReview}
                  className="mt-5 inline-flex w-full items-center justify-center gap-2 rounded-xl bg-accent-500 px-4 py-3 font-semibold transition hover:bg-accent-400 disabled:cursor-not-allowed disabled:opacity-70"
                >
                  {isSubmittingReview ? <Loader2 size={16} className="animate-spin" /> : null}
                  Submit Review
                </button>
              </form>
            </div>
          </section>

          <section id="download" className="section-shell">
            <SectionTitle
              eyebrow="Releases"
              title="APK distribution, versioning, and changelogs"
              subtitle="Browse release notes, compare versions, and download the latest Android build."
            />

            <div className="mt-10 space-y-6">
              <ApkTable entries={apks} resolveDownloadUrl={resolveDownloadUrl} formatBytes={formatBytes} />
              <UpdateTimeline entries={apks} />
            </div>
          </section>

          <section id="about" className="section-shell">
            <SectionTitle
              eyebrow="About"
              title="A focused product for listeners who care about detail"
              subtitle="Euphony is designed and engineered with a single goal: make music playback feel premium, fast, and joyful."
            />
            <div className="glass-panel mt-8 grid gap-5 p-5 sm:p-6 md:grid-cols-3 md:p-8">
              <div>
                <p className="text-xs uppercase tracking-[0.2em] text-slate-400">Developer</p>
                <p className="mt-1 text-lg font-semibold">Euphony Team</p>
              </div>
              <div>
                <p className="text-xs uppercase tracking-[0.2em] text-slate-400">Platform</p>
                <p className="mt-1 text-lg font-semibold">Android APK Distribution</p>
              </div>
              <div>
                <p className="text-xs uppercase tracking-[0.2em] text-slate-400">Focus</p>
                <p className="mt-1 text-lg font-semibold">Streaming, Library, and Playback UX</p>
              </div>
            </div>
          </section>

          <section id="contact" className="section-shell">
            <SectionTitle
              eyebrow="Contact"
              title="Share feedback, bugs, and feature requests"
              subtitle="Use this form to send comments, report issues, or ask about collaborations."
            />

            <form className="glass-panel mt-8 p-5 sm:p-6 md:p-8" onSubmit={handleMessageSubmit}>
              <div className="grid gap-4 md:grid-cols-2">
                <input
                  required
                  value={messageForm.name}
                  onChange={(event) =>
                    setMessageForm((current) => ({ ...current, name: event.target.value }))
                  }
                  placeholder="Name"
                  className="rounded-xl border border-white/15 bg-black/30 px-4 py-3 text-sm text-white outline-none ring-accent-400 transition focus:ring"
                />
                <input
                  required
                  type="email"
                  value={messageForm.email}
                  onChange={(event) =>
                    setMessageForm((current) => ({ ...current, email: event.target.value }))
                  }
                  placeholder="Email"
                  className="rounded-xl border border-white/15 bg-black/30 px-4 py-3 text-sm text-white outline-none ring-accent-400 transition focus:ring"
                />
              </div>
              <textarea
                required
                value={messageForm.message}
                onChange={(event) =>
                  setMessageForm((current) => ({ ...current, message: event.target.value }))
                }
                placeholder="How can we help?"
                rows={5}
                className="mt-4 w-full rounded-xl border border-white/15 bg-black/30 px-4 py-3 text-sm text-white outline-none ring-accent-400 transition focus:ring"
              />
              <button
                type="submit"
                disabled={isSubmittingMessage}
                className="mt-5 inline-flex items-center gap-2 rounded-xl bg-white px-5 py-3 font-semibold text-black transition hover:bg-slate-200 disabled:cursor-not-allowed disabled:opacity-70"
              >
                {isSubmittingMessage ? <Loader2 size={16} className="animate-spin" /> : <MessageCircle size={16} />}
                Send Feedback
              </button>
            </form>
          </section>
        </main>

        <Footer />
      </div>

      <AnimatePresence>
        {status.text ? (
          <motion.div
            initial={{ opacity: 0, y: 24 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 24 }}
            className={`fixed bottom-4 left-4 right-4 z-50 rounded-xl border px-4 py-3 text-sm backdrop-blur-xl sm:bottom-6 sm:left-auto sm:right-6 sm:max-w-[420px] ${
              status.type === "success"
                ? "border-emerald-400/40 bg-emerald-500/20 text-emerald-100"
                : status.type === "error"
                  ? "border-rose-400/40 bg-rose-500/20 text-rose-100"
                  : "border-amber-300/40 bg-amber-500/20 text-amber-100"
            }`}
            onAnimationComplete={() => {
              setTimeout(() => setStatus({ type: "", text: "" }), 2800);
            }}
          >
            {status.text}
          </motion.div>
        ) : null}
      </AnimatePresence>
    </>
  );
}
