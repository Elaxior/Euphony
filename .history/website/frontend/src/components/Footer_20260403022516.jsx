export default function Footer() {
  const year = new Date().getFullYear();

  return (
    <footer className="section-shell mt-20">
      <div className="glass-panel flex flex-col items-start justify-between gap-4 px-6 py-5 text-sm text-slate-400 md:flex-row md:items-center">
        <p>© {year} Euphony. All rights reserved.</p>
        <p>Built with React, Tailwind CSS, Three.js, and Node.js</p>
      </div>
    </footer>
  );
}
