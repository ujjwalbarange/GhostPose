"use client";

import React, { useState } from "react";
import PhoneMockup from "@/components/PhoneMockup";
import FaqSection from "@/components/FaqSection";
import {
  Download,
  ShieldCheck,
  Sparkles,
  Share2,
  Lock,
  BookmarkCheck,
  Layers,
  Check,
  Copy,
  Menu,
  X,
  Smartphone,
  EyeOff,
  Cpu,
  Code2,
  ArrowRight,
  Sliders,
  Zap,
} from "lucide-react";

const APK_DOWNLOAD_HREF = "/ghostpose-v1.0.apk";
const SHA256_FINGERPRINT =
  "e8a49c90f23d1bc8902ff93712b7a85d263914a17d8c52b9f04e1a6c3b8d92f1";

export default function LandingPage() {
  const [showAnnouncement, setShowAnnouncement] = useState<boolean>(true);
  const [mobileMenuOpen, setMobileMenuOpen] = useState<boolean>(false);
  const [copiedChecksum, setCopiedChecksum] = useState<boolean>(false);
  const [downloadTriggered, setDownloadTriggered] = useState<boolean>(false);

  const handleCopyChecksum = () => {
    if (typeof navigator !== "undefined" && navigator.clipboard) {
      navigator.clipboard.writeText(SHA256_FINGERPRINT);
      setCopiedChecksum(true);
      setTimeout(() => setCopiedChecksum(false), 2000);
    }
  };

  const handleDownloadClick = () => {
    setDownloadTriggered(true);
    setTimeout(() => setDownloadTriggered(false), 4000);
  };

  return (
    <div className="min-h-screen bg-[#0A0A0C] text-slate-50 flex flex-col relative overflow-x-hidden">
      {/* Ambient Top Spotlight */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-x-0 top-0 h-[560px] bg-hero-glow"
      />

      {/* 1. Top Announcement Bar */}
      {showAnnouncement && (
        <aside className="relative z-50 w-full bg-gradient-to-r from-emerald-950/90 via-[#042f2e] to-sky-950/90 border-b border-emerald-500/20 px-4 py-2 text-xs sm:text-sm font-medium text-emerald-100 flex items-center justify-between">
          <div className="flex-1 text-center truncate">
            <a
              href="#install-guide"
              className="inline-flex items-center gap-1.5 hover:underline"
            >
              <span className="inline-block h-2 w-2 rounded-full bg-emerald-400" />
              <span>
                GhostPose v1.0 for Android is live — now with Pinterest &amp;
                Instagram link resolution.
              </span>
              <span className="font-bold text-emerald-300 underline decoration-emerald-400/60">
                Get APK →
              </span>
            </a>
          </div>
          <button
            type="button"
            aria-label="Dismiss announcement"
            onClick={() => setShowAnnouncement(false)}
            className="ml-2 rounded-full p-1 text-emerald-200/80 hover:bg-white/10 hover:text-white transition-colors"
          >
            <X className="h-4 w-4" />
          </button>
        </aside>
      )}

      {/* 2. Sticky Glassmorphic Navigation Header */}
      <header className="sticky top-0 z-40 w-full backdrop-blur-xl bg-[#0A0A0C]/80 border-b border-white/[0.08]">
        <div className="mx-auto max-w-6xl h-16 px-4 sm:px-6 flex items-center justify-between">
          {/* Brand Logo + Wordmark */}
          <a href="#" className="flex items-center gap-2.5 group">
            <div className="relative h-9 w-9 rounded-xl bg-[#16191E] border border-white/15 flex items-center justify-center shadow-md overflow-hidden group-hover:scale-105 transition-transform">
              <img
                src="/ghostpose-logo.png"
                alt="GhostPose Logo"
                className="h-full w-full object-cover"
                onError={(e) => {
                  (e.currentTarget as HTMLImageElement).style.display = "none";
                }}
              />
              <span className="font-mono font-bold text-sm text-emerald-400">
                P
              </span>
            </div>
            <div className="font-extrabold text-xl tracking-tight flex items-center">
              <span className="text-white">Ghost</span>
              <span className="text-emerald-400">Pose</span>
            </div>
          </a>

          {/* Desktop Navigation Links */}
          <nav className="hidden md:flex items-center gap-7 text-sm font-medium text-slate-300">
            <a
              href="#how-it-works"
              className="hover:text-white transition-colors"
            >
              How It Works
            </a>
            <a
              href="#install-guide"
              className="hover:text-white transition-colors"
            >
              Install Guide
            </a>
            <a
              href="#comparison"
              className="hover:text-white transition-colors"
            >
              Comparison
            </a>
            <a href="#security-faq" className="hover:text-white transition-colors">
              Security &amp; FAQ
            </a>
          </nav>

          {/* Right CTA Action + Mobile Hamburger */}
          <div className="flex items-center gap-2.5">
            <a
              href="#download-cta"
              className="h-9 sm:h-10 px-4 sm:px-5 rounded-full bg-emerald-400 hover:bg-emerald-300 text-slate-950 text-xs sm:text-sm font-bold tracking-tight active:scale-95 transition-all shadow-[0_0_25px_rgba(16,185,129,0.3)] flex items-center justify-center gap-1.5"
            >
              <Download className="h-3.5 w-3.5 stroke-[2.5]" />
              <span>Get APK v1.0</span>
            </a>

            <button
              type="button"
              aria-label="Toggle Menu"
              onClick={() => setMobileMenuOpen((prev) => !prev)}
              className="md:hidden h-10 w-10 rounded-full border border-white/10 bg-white/[0.04] flex items-center justify-center text-slate-200 hover:bg-white/10 transition-colors"
            >
              {mobileMenuOpen ? (
                <X className="h-5 w-5" />
              ) : (
                <Menu className="h-5 w-5" />
              )}
            </button>
          </div>
        </div>

        {/* Mobile Dropdown Drawer */}
        {mobileMenuOpen && (
          <div className="md:hidden border-t border-white/[0.08] bg-[#121216]/95 backdrop-blur-2xl px-5 py-4 space-y-3">
            <a
              href="#how-it-works"
              onClick={() => setMobileMenuOpen(false)}
              className="block py-2 text-sm font-semibold text-slate-200 hover:text-emerald-400"
            >
              How It Works (3-Step Flow)
            </a>
            <a
              href="#install-guide"
              onClick={() => setMobileMenuOpen(false)}
              className="block py-2 text-sm font-semibold text-slate-200 hover:text-emerald-400"
            >
              2-Step Install Guide
            </a>
            <a
              href="#comparison"
              onClick={() => setMobileMenuOpen(false)}
              className="block py-2 text-sm font-semibold text-slate-200 hover:text-emerald-400"
            >
              Why GhostPose vs Split-Screen
            </a>
            <a
              href="#security-faq"
              onClick={() => setMobileMenuOpen(false)}
              className="block py-2 text-sm font-semibold text-slate-200 hover:text-emerald-400"
            >
              Android 13/14/15 Sideload FAQ
            </a>
            <a
              href={APK_DOWNLOAD_HREF}
              download="ghostpose-v1.0.apk"
              onClick={() => {
                setMobileMenuOpen(false);
                handleDownloadClick();
              }}
              className="mt-2 w-full py-3 rounded-full bg-emerald-400 text-slate-950 font-bold text-sm flex items-center justify-center gap-2"
            >
              <Download className="h-4 w-4" />
              <span>Download GhostPose APK (v1.0)</span>
            </a>
          </div>
        )}
      </header>

      <main className="flex-1 w-full">
        {/* 3. HERO SECTION (Mobile-First Single Column -> Desktop 2-Column Hero) */}
        <section
          id="download-cta"
          className="relative mx-auto max-w-6xl px-4 sm:px-6 pt-10 pb-14 lg:py-20"
        >
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 lg:gap-8 items-center">
            {/* Left Column: Copy, Conversion CTA, Trust Metadata */}
            <div className="lg:col-span-7 flex flex-col items-center lg:items-start text-center lg:text-left">
              {/* Release Status Chip */}
              <div className="inline-flex items-center gap-2 rounded-full border border-emerald-500/30 bg-emerald-500/10 px-3.5 py-1.5 text-xs font-mono font-semibold text-emerald-300 mb-6">
                <span className="h-2 w-2 rounded-full bg-emerald-400" />
                <span>v1.0 Release • Android 8.0+ • Free Utility</span>
              </div>

              {/* Headline */}
              <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-white leading-[1.08]">
                Copy any pose. <br />
                <span className="bg-gradient-to-r from-emerald-300 via-teal-200 to-sky-400 bg-clip-text text-transparent">
                  Direct on your camera.
                </span>
              </h1>

              {/* Subtitle */}
              <p className="mt-5 text-base sm:text-lg text-slate-300 max-w-xl leading-relaxed">
                Overlay reference poses from Pinterest, Instagram, or your
                gallery with dynamic transparency directly over your camera.
                Full touch passthrough—zero distractions.
              </p>

              {/* Primary Download APK Button Block */}
              <div className="mt-8 w-full sm:w-auto flex flex-col sm:flex-row items-center gap-3.5">
                <a
                  href={APK_DOWNLOAD_HREF}
                  download="ghostpose-v1.0.apk"
                  onClick={handleDownloadClick}
                  className="w-full sm:w-auto min-h-[56px] px-8 py-4 rounded-full bg-emerald-400 hover:bg-emerald-300 text-slate-950 font-extrabold text-base sm:text-lg flex items-center justify-center gap-3 shadow-[0_12px_40px_-8px_rgba(16,185,129,0.55)] hover:shadow-[0_16px_50px_-6px_rgba(16,185,129,0.75)] active:scale-[0.98] transition-all"
                >
                  <Download className="h-5 w-5 stroke-[2.5]" />
                  <span>Download GhostPose APK (v1.0)</span>
                </a>

                <a
                  href="#install-guide"
                  className="w-full sm:w-auto min-h-[56px] px-6 py-4 rounded-full backdrop-blur-xl bg-white/[0.04] hover:bg-white/[0.08] border border-white/[0.1] text-white font-semibold text-sm sm:text-base flex items-center justify-center gap-2 transition-colors"
                >
                  <span>Installation Guide</span>
                  <ArrowRight className="h-4 w-4 text-slate-400" />
                </a>
              </div>

              {/* Micro-Metadata Subtext */}
              <div className="mt-3.5 flex flex-wrap items-center justify-center lg:justify-start gap-2 text-xs font-mono text-slate-400">
                <span>Direct APK</span>
                <span>•</span>
                <span className="text-slate-300 font-semibold">18.4 MB</span>
                <span>•</span>
                <span>100% Free &amp; Open Source</span>
                <span>•</span>
                <span className="text-emerald-400">Universal ARM64 / x86_64</span>
              </div>

              {/* Post-Download Helper Toast */}
              {downloadTriggered && (
                <div className="mt-4 w-full max-w-lg rounded-2xl border border-emerald-500/40 bg-emerald-950/60 p-3.5 text-left text-xs text-emerald-100 backdrop-blur-xl">
                  <div className="font-bold text-emerald-300 flex items-center gap-1.5">
                    <Check className="h-4 w-4" />
                    <span>Downloading ghostpose-v1.0.apk...</span>
                  </div>
                  <p className="mt-1 text-emerald-100/80">
                    Once downloaded, open the APK to install. On Android 13–15,
                    see our{" "}
                    <a
                      href="#security-faq"
                      className="underline font-semibold text-white"
                    >
                      10-second Restricted Settings tip
                    </a>{" "}
                    if prompted when enabling the overlay.
                  </p>
                </div>
              )}

              {/* Platform Trust Pills Row */}
              <div className="mt-8 pt-6 border-t border-white/[0.08] flex flex-wrap items-center justify-center lg:justify-start gap-2.5 text-xs font-semibold text-slate-300">
                <span className="inline-flex items-center gap-1.5 rounded-full bg-white/[0.04] border border-white/[0.08] px-3.5 py-1.5">
                  <ShieldCheck className="h-4 w-4 text-emerald-400" />
                  <span>Play Protect Verified</span>
                </span>
                <span className="inline-flex items-center gap-1.5 rounded-full bg-white/[0.04] border border-white/[0.08] px-3.5 py-1.5">
                  <EyeOff className="h-4 w-4 text-sky-400" />
                  <span>Zero Camera Permission Needed</span>
                </span>
                <span className="inline-flex items-center gap-1.5 rounded-full bg-white/[0.04] border border-white/[0.08] px-3.5 py-1.5">
                  <Cpu className="h-4 w-4 text-emerald-400" />
                  <span>0% Idle Battery Drain</span>
                </span>
              </div>
            </div>

            {/* Right Column: Interactive Realistic In-App Product Mockup */}
            <div className="lg:col-span-5 flex justify-center">
              <PhoneMockup />
            </div>
          </div>
        </section>

        {/* 4. HIGHLIGHT TICKER STRIP */}
        <div className="w-full border-y border-white/[0.07] bg-white/[0.02] py-3.5">
          <div className="mx-auto max-w-6xl px-4 sm:px-6 flex flex-wrap items-center justify-between gap-4 text-xs sm:text-sm font-semibold text-slate-300">
            <div className="flex flex-wrap items-center gap-4 sm:gap-6">
              <span className="inline-flex items-center gap-1.5 text-white">
                <Layers className="h-4 w-4 text-emerald-400" />
                <span>Dual-Window Passthrough</span>
              </span>
              <span className="text-white/20">•</span>
              <span className="inline-flex items-center gap-1.5 text-white">
                <Share2 className="h-4 w-4 text-sky-400" />
                <span>Pinterest &amp; Instagram Share Sheet</span>
              </span>
              <span className="text-white/20">•</span>
              <span className="inline-flex items-center gap-1.5 text-white">
                <Sliders className="h-4 w-4 text-emerald-400" />
                <span>Draggable HUD Controls</span>
              </span>
              <span className="text-white/20 hidden sm:inline">•</span>
              <span className="hidden sm:inline-flex items-center gap-1.5 text-white">
                <Zap className="h-4 w-4 text-amber-400" />
                <span>Low-RAM RGB_565 Engine</span>
              </span>
            </div>
            <a
              href="#how-it-works"
              className="rounded-full border border-white/10 bg-white/[0.04] px-3.5 py-1 text-xs font-semibold text-emerald-300 hover:bg-white/[0.08] transition-colors"
            >
              Explore Features →
            </a>
          </div>
        </div>

        {/* 5. "HOW IT WORKS" 3-STEP FLOW */}
        <section
          id="how-it-works"
          className="mx-auto max-w-6xl px-4 sm:px-6 py-16 sm:py-24"
        >
          <div className="text-center max-w-2xl mx-auto mb-12">
            <span className="font-mono text-xs font-bold uppercase tracking-widest text-emerald-400">
              Effortless Workflow
            </span>
            <h2 className="mt-2 text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
              Set up your shot in three{" "}
              <span className="text-emerald-400">tiny</span> steps.
            </h2>
            <p className="mt-3 text-sm sm:text-base text-slate-400">
              No split-screen juggling or switching back and forth between
              Pinterest and your camera roll.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {/* Step 1 */}
            <div className="rounded-3xl backdrop-blur-xl bg-white/[0.03] border border-white/[0.08] p-6 sm:p-7 flex flex-col justify-between hover:border-emerald-500/30 transition-colors">
              <div>
                <div className="flex items-center justify-between mb-6">
                  <div className="h-12 w-12 rounded-2xl bg-emerald-500/15 border border-emerald-500/30 flex items-center justify-center text-emerald-300">
                    <Share2 className="h-6 w-6" />
                  </div>
                  <span className="font-mono text-xs font-bold uppercase tracking-wider text-slate-500">
                    Step 01
                  </span>
                </div>
                <h3 className="text-xl font-bold text-white">Share or Paste</h3>
                <p className="mt-2.5 text-sm text-slate-300 leading-relaxed">
                  Send any photo, Pinterest pin (including{" "}
                  <code className="text-emerald-300 font-mono text-xs">
                    pin.it
                  </code>{" "}
                  links), Instagram post, or Google Search image directly into{" "}
                  <strong className="text-white">GhostPose Overlay</strong> via
                  the native Android Share Sheet.
                </p>
              </div>
              <div className="mt-6 pt-4 border-t border-white/[0.06] flex items-center justify-between text-xs font-mono text-slate-400">
                <span>Auto-extracts HD image</span>
                <span className="text-emerald-400">ACTION_SEND</span>
              </div>
            </div>

            {/* Step 2 */}
            <div className="rounded-3xl backdrop-blur-xl bg-white/[0.03] border border-white/[0.08] p-6 sm:p-7 flex flex-col justify-between hover:border-emerald-500/30 transition-colors">
              <div>
                <div className="flex items-center justify-between mb-6">
                  <div className="h-12 w-12 rounded-2xl bg-sky-500/15 border border-sky-500/30 flex items-center justify-center text-sky-300">
                    <Lock className="h-6 w-6" />
                  </div>
                  <span className="font-mono text-xs font-bold uppercase tracking-wider text-slate-500">
                    Step 02
                  </span>
                </div>
                <h3 className="text-xl font-bold text-white">Lock &amp; Shoot</h3>
                <p className="mt-2.5 text-sm text-slate-300 leading-relaxed">
                  Pinch-to-zoom, mirror-flip, dial in your guide opacity
                  (10%–100%), and tap{" "}
                  <strong className="text-white">Lock Pose</strong>. Finger taps
                  pass 100% through the ghost image straight to your camera
                  shutter.
                </p>
              </div>
              <div className="mt-6 pt-4 border-t border-white/[0.06] flex items-center justify-between text-xs font-mono text-slate-400">
                <span>Zero shutter delay</span>
                <span className="text-sky-400">FLAG_NOT_TOUCHABLE</span>
              </div>
            </div>

            {/* Step 3 */}
            <div className="rounded-3xl backdrop-blur-xl bg-white/[0.03] border border-white/[0.08] p-6 sm:p-7 flex flex-col justify-between hover:border-emerald-500/30 transition-colors">
              <div>
                <div className="flex items-center justify-between mb-6">
                  <div className="h-12 w-12 rounded-2xl bg-emerald-500/15 border border-emerald-500/30 flex items-center justify-center text-emerald-300">
                    <BookmarkCheck className="h-6 w-6" />
                  </div>
                  <span className="font-mono text-xs font-bold uppercase tracking-wider text-slate-500">
                    Step 03
                  </span>
                </div>
                <h3 className="text-xl font-bold text-white">
                  Save to History
                </h3>
                <p className="mt-2.5 text-sm text-slate-300 leading-relaxed">
                  Bookmark your favorite reference poses with custom tags
                  (Portraits, Full Body, 0.5x Wide, Golden Hour) into your local
                  offline Pose Hub for instant 1-tap relaunch anytime.
                </p>
              </div>
              <div className="mt-6 pt-4 border-t border-white/[0.06] flex items-center justify-between text-xs font-mono text-slate-400">
                <span>100% Offline SQLite</span>
                <span className="text-emerald-400">Room DB</span>
              </div>
            </div>
          </div>
        </section>

        {/* 6. TWO-STEP INSTALL CARD (Inspired by Reference HTML Section 5) */}
        <section
          id="install-guide"
          className="mx-auto max-w-4xl px-4 sm:px-6 py-8"
        >
          <div className="rounded-[32px] border border-emerald-500/25 bg-gradient-to-b from-emerald-950/30 via-[#121216] to-[#121216] p-6 sm:p-10 shadow-2xl">
            <div className="text-center mb-8">
              <h2 className="text-2xl sm:text-3xl font-extrabold text-white tracking-tight">
                Install GhostPose to start posing.
              </h2>
              <p className="text-sm text-slate-400 mt-1.5">
                No login required. Ready on any Android phone in under 10
                seconds.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
              {/* Step 1 Card */}
              <div className="rounded-2xl bg-white/[0.03] border border-white/[0.08] p-5 flex flex-col justify-between gap-4">
                <div>
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-xs font-bold uppercase tracking-wider text-emerald-400">
                      STEP 1
                    </span>
                    <span className="text-xs font-mono text-slate-400">
                      18.4 MB • Verified APK
                    </span>
                  </div>
                  <h3 className="mt-2 text-lg font-bold text-white flex items-center gap-2">
                    <span>Get GhostPose on your phone</span>
                    <Smartphone className="h-4 w-4 text-slate-400" />
                  </h3>
                  <p className="mt-1.5 text-xs sm:text-sm text-slate-400 leading-relaxed">
                    Download the universal signed release APK directly to your
                    Android device.
                  </p>
                </div>

                <a
                  href={APK_DOWNLOAD_HREF}
                  download="ghostpose-v1.0.apk"
                  onClick={handleDownloadClick}
                  className="w-full py-3.5 px-5 rounded-xl bg-emerald-400 hover:bg-emerald-300 text-slate-950 font-extrabold text-sm flex items-center justify-center gap-2 transition-all shadow-md active:scale-[0.99]"
                >
                  <Download className="h-4 w-4 stroke-[2.5]" />
                  <span>Download APK (v1.0)</span>
                </a>
              </div>

              {/* Step 2 Card */}
              <div className="rounded-2xl bg-white/[0.03] border border-white/[0.08] p-5 flex flex-col justify-between gap-4">
                <div>
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-xs font-bold uppercase tracking-wider text-sky-400">
                      STEP 2
                    </span>
                    <span className="text-xs font-semibold text-emerald-400 flex items-center gap-1">
                      <Check className="h-3.5 w-3.5" /> 1-time prompt
                    </span>
                  </div>
                  <h3 className="mt-2 text-lg font-bold text-white flex items-center gap-2">
                    <span>Grant Overlay Permission</span>
                    <Layers className="h-4 w-4 text-slate-400" />
                  </h3>
                  <p className="mt-1.5 text-xs sm:text-sm text-slate-300 leading-relaxed">
                    Toggle{" "}
                    <code className="rounded bg-white/10 px-1.5 py-0.5 font-mono text-xs text-emerald-300">
                      Display over other apps
                    </code>{" "}
                    in Android settings so reference poses can float above your
                    camera viewfinder.
                  </p>
                </div>

                <div className="rounded-xl bg-black/40 border border-white/[0.06] px-3.5 py-2.5 text-[11px] text-slate-400">
                  <strong className="text-slate-200">Android 13–15 Tip:</strong>{" "}
                  If grayed out, open{" "}
                  <span className="text-white">
                    Settings → Apps → GhostPose → ⋮ → Allow restricted settings
                  </span>
                  .
                </div>
              </div>
            </div>

            <div className="mt-6 pt-4 border-t border-white/[0.08] text-center text-xs text-slate-400">
              <strong className="text-slate-200">Compatibility:</strong> Android
              8.0+ (ARM64 &amp; x86_64). Works natively over Google Camera,
              Samsung Camera, Moto Camera, Instagram, Snapchat, and TikTok.
            </div>
          </div>
        </section>

        {/* 7. COMPARISON TABLE (All the control. None of the clutter.) */}
        <section
          id="comparison"
          className="mx-auto max-w-4xl px-4 sm:px-6 py-16"
        >
          <div className="text-center mb-10">
            <h2 className="text-2xl sm:text-4xl font-extrabold text-white tracking-tight">
              All the control. None of the clutter.
            </h2>
            <p className="text-xs sm:text-sm text-slate-400 mt-2">
              How GhostPose compares to Stock Camera apps and Split-Screen
              workarounds
            </p>
          </div>

          <div className="rounded-3xl border border-white/[0.08] bg-white/[0.02] backdrop-blur-xl overflow-hidden text-xs sm:text-sm">
            <div className="grid grid-cols-4 border-b border-white/[0.08] bg-white/[0.03] font-semibold text-slate-300 py-4 px-3 sm:px-6">
              <div className="uppercase tracking-wider text-[10px] sm:text-xs text-slate-400 self-center">
                Capability
              </div>
              <div className="text-center font-extrabold text-emerald-400 self-center">
                GhostPose
              </div>
              <div className="text-center text-slate-400 self-center">
                Stock Camera
              </div>
              <div className="text-center text-slate-400 self-center">
                Split-Screen
              </div>
            </div>

            {[
              {
                label: "Live Translucent Pose Overlay",
                ghost: "Yes (10%–100%)",
                stock: "No",
                split: "No",
              },
              {
                label: "100% Shutter Touch Passthrough",
                ghost: "Yes (0ms lag)",
                stock: "N/A",
                split: "No",
              },
              {
                label: "Works inside Native Camera & Instagram",
                ghost: "Yes",
                stock: "No",
                split: "Crops Viewfinder",
              },
              {
                label: "Pinterest & Instagram Link Extractor",
                ghost: "Built-in",
                stock: "No",
                split: "Manual Screenshot",
              },
              {
                label: "Draggable HUD via Minus (—) Handle",
                ghost: "Yes",
                stock: "No",
                split: "No",
              },
              {
                label: "Ads, Tracking, or Paid Subscriptions",
                ghost: "Zero (Free)",
                stock: "None",
                split: "Often",
              },
            ].map((row, i) => (
              <div
                key={row.label}
                className={`grid grid-cols-4 py-3.5 px-3 sm:px-6 items-center ${
                  i < 5 ? "border-b border-white/[0.06]" : ""
                }`}
              >
                <div className="font-medium text-slate-200 pr-2">
                  {row.label}
                </div>
                <div className="text-center font-bold text-emerald-300 bg-emerald-500/10 border border-emerald-500/20 py-1.5 px-2 rounded-xl">
                  {row.ghost}
                </div>
                <div className="text-center text-slate-400">{row.stock}</div>
                <div className="text-center text-slate-400">{row.split}</div>
              </div>
            ))}
          </div>
        </section>

        {/* 8. SECURITY & TRUST BANNER + INTERACTIVE FAQ ACCORDION */}
        <section
          id="security-faq"
          className="mx-auto max-w-4xl px-4 sm:px-6 py-14"
        >
          <div className="text-center mb-10">
            <span className="font-mono text-xs font-bold uppercase tracking-widest text-emerald-400">
              Verified &amp; Transparent
            </span>
            <h2 className="mt-2 text-2xl sm:text-4xl font-extrabold text-white tracking-tight">
              Built for privacy. Verified for Android.
            </h2>
            <p className="mt-2 text-sm text-slate-400 max-w-xl mx-auto">
              Every APK release is signed with a 4096-bit RSA production key and
              runs 100% locally on your device.
            </p>
          </div>

          {/* 4-Badge Security Verification Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mb-6">
            <div className="rounded-2xl border border-white/[0.08] bg-white/[0.03] p-4 flex flex-col items-center text-center gap-1.5">
              <ShieldCheck className="h-6 w-6 text-emerald-400" />
              <span className="text-xs sm:text-sm font-bold text-white">
                Play Protect Verified
              </span>
              <span className="text-[11px] text-slate-400">
                Compliant manifest &amp; FGS declarations
              </span>
            </div>

            <div className="rounded-2xl border border-white/[0.08] bg-white/[0.03] p-4 flex flex-col items-center text-center gap-1.5">
              <EyeOff className="h-6 w-6 text-sky-400" />
              <span className="text-xs sm:text-sm font-bold text-white">
                Zero Analytics
              </span>
              <span className="text-[11px] text-slate-400">
                No telemetry, no ads, no tracking SDKs
              </span>
            </div>

            <div className="rounded-2xl border border-white/[0.08] bg-white/[0.03] p-4 flex flex-col items-center text-center gap-1.5">
              <Cpu className="h-6 w-6 text-emerald-400" />
              <span className="text-xs sm:text-sm font-bold text-white">
                100% Offline Capable
              </span>
              <span className="text-[11px] text-slate-400">
                Local Room SQLite library on device
              </span>
            </div>

            <div className="rounded-2xl border border-white/[0.08] bg-white/[0.03] p-4 flex flex-col items-center text-center gap-1.5">
              <Code2 className="h-6 w-6 text-sky-400" />
              <span className="text-xs sm:text-sm font-bold text-white">
                Open Source
              </span>
              <span className="text-[11px] text-slate-400">
                Inspectable Kotlin &amp; Compose codebase
              </span>
            </div>
          </div>

          {/* SHA-256 Release Signature Box */}
          <div className="mb-10 rounded-2xl border border-white/[0.08] bg-white/[0.03] p-4 sm:p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div className="flex flex-col gap-1">
              <span className="text-xs font-bold text-white flex items-center gap-1.5">
                <ShieldCheck className="h-4 w-4 text-emerald-400" />
                <span>RSA-4096 Release APK SHA-256 Fingerprint</span>
              </span>
              <code className="text-[11px] sm:text-xs font-mono text-slate-400 break-all">
                {SHA256_FINGERPRINT}
              </code>
            </div>
            <button
              type="button"
              onClick={handleCopyChecksum}
              className="self-start sm:self-center flex-shrink-0 rounded-xl border border-white/10 bg-white/[0.05] hover:bg-white/[0.1] px-3.5 py-2 text-xs font-semibold text-white flex items-center gap-1.5 transition-colors"
            >
              {copiedChecksum ? (
                <>
                  <Check className="h-3.5 w-3.5 text-emerald-400" />
                  <span className="text-emerald-300">Copied</span>
                </>
              ) : (
                <>
                  <Copy className="h-3.5 w-3.5 text-slate-300" />
                  <span>Copy SHA-256</span>
                </>
              )}
            </button>
          </div>

          {/* Interactive Expandable FAQ Accordion */}
          <FaqSection />
        </section>

        {/* 9. FINAL CALL TO ACTION */}
        <section className="mx-auto max-w-4xl px-4 sm:px-6 py-16 text-center flex flex-col items-center">
          <div className="h-16 w-16 rounded-2xl bg-[#16191E] border border-white/15 shadow-xl flex items-center justify-center mb-5 overflow-hidden">
            <img
              src="/ghostpose-logo.png"
              alt="GhostPose Icon"
              className="h-full w-full object-cover"
              onError={(e) => {
                (e.currentTarget as HTMLImageElement).style.display = "none";
              }}
            />
            <Sparkles className="h-7 w-7 text-emerald-400" />
          </div>

          <h2 className="text-3xl sm:text-5xl font-extrabold text-white tracking-tight leading-tight">
            Start your next shoot <br />
            <span className="text-emerald-400">with GhostPose.</span>
          </h2>
          <p className="mt-3 text-sm sm:text-base text-slate-400 max-w-md">
            Free forever. No subscriptions, no watermarks, no ads, and zero
            account logins.
          </p>

          <a
            href={APK_DOWNLOAD_HREF}
            download="ghostpose-v1.0.apk"
            onClick={handleDownloadClick}
            className="mt-7 px-9 py-4 rounded-full bg-emerald-400 hover:bg-emerald-300 text-slate-950 font-extrabold text-base sm:text-lg flex items-center gap-2.5 shadow-[0_12px_40px_-8px_rgba(16,185,129,0.55)] active:scale-95 transition-all"
          >
            <Download className="h-5 w-5 stroke-[2.5]" />
            <span>Download GhostPose APK (v1.0)</span>
          </a>

          <div className="mt-4 text-xs font-mono text-slate-400">
            v1.0.0 • ghostpose-v1.0.apk • Android 8.0+ (API 26–35)
          </div>
        </section>
      </main>

      {/* 10. FOOTER */}
      <footer className="w-full border-t border-white/[0.08] bg-[#08080A] py-10">
        <div className="mx-auto max-w-6xl px-4 sm:px-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6 text-xs text-slate-400">
          <div className="flex flex-col gap-2">
            <div className="flex items-center gap-2">
              <div className="h-6 w-6 rounded-lg bg-[#16191E] border border-white/15 flex items-center justify-center font-mono font-bold text-emerald-400 text-xs">
                P
              </div>
              <span className="font-bold text-sm text-white">
                Ghost<span className="text-emerald-400">Pose</span>
              </span>
              <span className="rounded-full bg-white/[0.06] px-2 py-0.5 font-mono text-[10px] text-slate-300">
                v1.0.0
              </span>
            </div>
            <p className="text-slate-500">
              Native Android Camera Pose Companion • Released under the MIT
              License.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-6 font-medium text-slate-300">
            <a
              href={APK_DOWNLOAD_HREF}
              download="ghostpose-v1.0.apk"
              className="hover:text-emerald-400 transition-colors"
            >
              Download APK
            </a>
            <a
              href="#install-guide"
              className="hover:text-emerald-400 transition-colors"
            >
              Install Guide
            </a>
            <a
              href="#security-faq"
              className="hover:text-emerald-400 transition-colors"
            >
              Android 15 Restricted Settings
            </a>
            <a
              href="#how-it-works"
              className="hover:text-emerald-400 transition-colors"
            >
              Changelog (v1.0)
            </a>
          </div>
        </div>
      </footer>
    </div>
  );
}
