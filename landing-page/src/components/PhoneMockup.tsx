"use client";

import React, { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  Lock,
  Unlock,
  Radio,
  X,
  EyeOff,
  Eye,
  Droplets,
  FlipHorizontal2,
  RotateCcw,
  Crop,
  CheckCircle2,
  BookmarkPlus,
  Camera,
  Image as ImageIcon,
  RefreshCw,
  Sparkles,
} from "lucide-react";

const SAMPLE_POSES = [
  {
    id: "mirror",
    name: "0.5x Flash Mirror",
    tag: "0.5x Wide",
    url: "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=900&q=80",
  },
  {
    id: "street",
    name: "Low Angle Street",
    tag: "Full Body",
    url: "https://images.unsplash.com/photo-1509631179647-0177331693ae?auto=format&fit=crop&w=900&q=80",
  },
  {
    id: "golden",
    name: "Candid Golden Hour",
    tag: "Outdoor",
    url: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=900&q=80",
  },
];

export default function PhoneMockup() {
  const [opacity, setOpacity] = useState<number>(40);
  const [isMirrored, setIsMirrored] = useState<boolean>(false);
  const [showGrid, setShowGrid] = useState<boolean>(true);
  const [isLockedPassthrough, setIsLockedPassthrough] = useState<boolean>(true);
  const [isPanelVisible, setIsPanelVisible] = useState<boolean>(true);
  const [isHudMinimized, setIsHudMinimized] = useState<boolean>(false);
  const [poseScale, setPoseScale] = useState<number>(1);
  const [activePoseIdx, setActivePoseIdx] = useState<number>(0);
  const [shutterFlash, setShutterFlash] = useState<boolean>(false);
  const [shotCount, setShotCount] = useState<number>(0);
  const [savedToast, setSavedToast] = useState<boolean>(false);

  const currentPose = SAMPLE_POSES[activePoseIdx];

  const triggerShutter = () => {
    setShutterFlash(true);
    setShotCount((prev) => prev + 1);
    setTimeout(() => setShutterFlash(false), 180);
  };

  const handleResetScale = () => {
    setPoseScale(1);
    setIsMirrored(false);
    setOpacity(40);
  };

  const handleSavePose = () => {
    setSavedToast(true);
    setTimeout(() => setSavedToast(false), 1800);
  };

  const cyclePose = () => {
    setActivePoseIdx((prev) => (prev + 1) % SAMPLE_POSES.length);
  };

  return (
    <div className="relative flex flex-col items-center select-none">
      {/* Subtle Ambient Glow Behind Device */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -inset-6 rounded-[56px] bg-gradient-to-tr from-emerald-500/20 via-sky-500/15 to-transparent blur-2xl"
      />

      {/* Interactive Try-Me Hint Pill above Phone */}
      <div className="mb-3 flex items-center gap-2 rounded-full border border-white/10 bg-white/[0.04] px-3.5 py-1.5 text-xs font-medium text-slate-300 backdrop-blur-md">
        <Sparkles className="h-3.5 w-3.5 text-emerald-400" />
        <span>Interactive Demo: Drag opacity slider or tap controls below</span>
      </div>

      {/* Hardware Phone Frame (320px - 350px width) */}
      <div className="relative w-[316px] sm:w-[344px] aspect-[9/19.2] rounded-[46px] border-[6px] border-[#23262F] bg-[#0B0C0E] p-2 shadow-[0_30px_80px_-15px_rgba(0,0,0,0.85),0_0_40px_-10px_rgba(16,185,129,0.22)]">
        {/* Inner Display Bezel */}
        <div className="relative h-full w-full overflow-hidden rounded-[38px] bg-[#111317] flex flex-col justify-between">
          {/* 1. Live Camera Viewfinder Background Scene */}
          <div
            onClick={() => {
              if (isLockedPassthrough) triggerShutter();
            }}
            className={`absolute inset-0 bg-cover bg-center transition-transform duration-300 ${
              isLockedPassthrough ? "cursor-crosshair" : "cursor-grab"
            }`}
            style={{
              backgroundImage: `url('https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=900&q=80')`,
            }}
          >
            <div className="absolute inset-0 bg-gradient-to-b from-black/40 via-transparent to-black/75" />
          </div>

          {/* 2. Translucent Reference Ghost Pose Layer */}
          <div
            onClick={() => {
              if (isLockedPassthrough) triggerShutter();
            }}
            className="absolute inset-0 flex items-center justify-center overflow-hidden pointer-events-none"
          >
            <img
              src={currentPose.url}
              alt={currentPose.name}
              style={{
                opacity: opacity / 100,
                transform: `scale(${poseScale}) scaleX(${isMirrored ? -1 : 1})`,
              }}
              className="h-full w-full object-cover transition-transform duration-150"
            />

            {/* Subtle Neon Silhouette Alignment Target */}
            <svg
              viewBox="0 0 200 320"
              style={{
                opacity: Math.min(0.75, (opacity / 100) * 1.15),
                transform: `scaleX(${isMirrored ? -1 : 1})`,
              }}
              className="absolute w-3/4 h-3/4 stroke-[#6FFBBE] fill-none pointer-events-none transition-transform duration-150"
              strokeWidth="1.6"
              strokeDasharray="5 4"
              strokeLinecap="round"
            >
              <circle cx="100" cy="58" r="22" />
              <path d="M68 92 C78 83, 122 83, 132 92 L138 178 C138 188, 62 188, 62 178 Z" />
              <path d="M46 130 L58 115 M154 130 L142 115" />
              <circle
                cx="100"
                cy="58"
                r="3"
                className="fill-[#6FFBBE] stroke-none"
              />
            </svg>
          </div>

          {/* 3. Rule-of-Thirds 3x3 Viewfinder Alignment Grid */}
          {showGrid && (
            <div className="pointer-events-none absolute inset-0 grid grid-cols-3 grid-rows-3">
              <div className="border-r border-b border-white/20" />
              <div className="border-r border-b border-white/20" />
              <div className="border-b border-white/20" />
              <div className="border-r border-b border-white/20" />
              <div className="border-r border-b border-white/20" />
              <div className="border-b border-white/20" />
              <div className="border-r border-white/20" />
              <div className="border-r border-white/20" />
              <div />
            </div>
          )}

          {/* Camera Shutter Flash Effect */}
          <AnimatePresence>
            {shutterFlash && (
              <motion.div
                initial={{ opacity: 0.9 }}
                animate={{ opacity: 0 }}
                exit={{ opacity: 0 }}
                transition={{ duration: 0.18 }}
                className="pointer-events-none absolute inset-0 z-40 bg-white"
              />
            )}
          </AnimatePresence>

          {/* 4. Top Status Bar & Floating GhostPose Anchor Pills (Exact App Match) */}
          <div className="relative z-20 pt-3 px-2.5 flex flex-col gap-2">
            {/* Android Punch-Hole & Status Time */}
            <div className="flex items-center justify-between px-3 text-[10px] font-semibold text-white/80">
              <span>13:41</span>
              <div className="h-2.5 w-2.5 rounded-full bg-black border border-white/20" />
              <span>5G • 94%</span>
            </div>

            {/* Top Floating Pills Row: Ghost Pose Menu + Hide Panel + Close Cross */}
            <div className="flex items-center justify-between gap-1.5 pt-0.5">
              {/* Left Pill: Ghost Pose Menu */}
              <button
                type="button"
                onClick={() => setIsLockedPassthrough((prev) => !prev)}
                className="flex items-center gap-1.5 rounded-full bg-white/95 px-3 py-1.5 text-[11px] font-semibold text-[#191C1D] shadow-md active:scale-95 transition-transform"
              >
                <span className="relative flex h-2.5 w-2.5 items-center justify-center">
                  <span className="absolute h-2.5 w-2.5 rounded-full bg-[#6FFBBE]/60" />
                  <span
                    className={`relative h-1.5 w-1.5 rounded-full ${
                      isLockedPassthrough ? "bg-[#005236]" : "bg-amber-500"
                    }`}
                  />
                </span>
                <span className="whitespace-nowrap">Ghost Pose Menu</span>
                {isLockedPassthrough ? (
                  <Lock className="h-3 w-3 text-[#005236]" />
                ) : (
                  <Unlock className="h-3 w-3 text-amber-600" />
                )}
              </button>

              {/* Right Pills: Hide Panel / Controls + X Close */}
              <div className="flex items-center gap-1.5">
                <button
                  type="button"
                  onClick={() => setIsPanelVisible((prev) => !prev)}
                  className="flex items-center gap-1 rounded-full bg-white/95 px-2.5 py-1.5 text-[11px] font-semibold text-[#191C1D] shadow-md active:scale-95 transition-transform"
                >
                  <Radio className="h-3 w-3 text-[#009668]" />
                  <span className="whitespace-nowrap">
                    {isPanelVisible ? "Hide Panel" : "Controls"}
                  </span>
                </button>

                <button
                  type="button"
                  aria-label="Close overlay preview"
                  onClick={() => setIsPanelVisible(false)}
                  className="flex h-7 w-7 items-center justify-center rounded-full bg-[#2E3132]/90 text-white shadow-md active:scale-90 transition-transform"
                >
                  <X className="h-3.5 w-3.5" />
                </button>
              </div>
            </div>

            {/*Draggabe / Floating Overlay Controls Card (Matches Screenshot_20261110-134151) */}
            {isPanelVisible && (
              <motion.div
                drag="y"
                dragConstraints={{ top: 0, bottom: 110 }}
                dragElastic={0.08}
                className="mt-1 rounded-[24px] bg-white/95 p-3.5 text-[#191C1D] shadow-[0_16px_40px_rgba(0,0,0,0.45)] backdrop-blur-xl"
              >
                {/* Card Header with Hold-and-Drag Minus ("—") Pill */}
                <div className="flex items-center justify-between gap-2">
                  <div
                    title="Hold and drag to reposition panel"
                    className="flex items-center gap-2 cursor-grab active:cursor-grabbing"
                  >
                    <div className="flex h-5 w-8 items-center justify-center rounded-full bg-[#F3F4F5]">
                      <div className="h-1 w-5 rounded-full bg-[#9CA3AF]" />
                    </div>
                    <span className="text-[13px] font-bold tracking-tight text-[#191C1D]">
                      Overlay Controls
                    </span>
                  </div>

                  <div className="flex items-center gap-1.5">
                    <button
                      type="button"
                      onClick={() => setIsHudMinimized((prev) => !prev)}
                      className="flex items-center gap-1 rounded-full bg-[#EDEEEF] px-2.5 py-1 text-[10px] font-semibold text-[#45474B] hover:bg-[#E1E3E4] transition-colors"
                    >
                      {isHudMinimized ? (
                        <>
                          <Eye className="h-3 w-3" />
                          <span>Expand HUD</span>
                        </>
                      ) : (
                        <>
                          <EyeOff className="h-3 w-3" />
                          <span>Hide HUD</span>
                        </>
                      )}
                    </button>
                    <button
                      type="button"
                      onClick={() => setIsPanelVisible(false)}
                      className="flex h-6 w-6 items-center justify-center rounded-full bg-[#EDEEEF] text-[#45474B]"
                    >
                      <X className="h-3 w-3" />
                    </button>
                  </div>
                </div>

                {/* Expandable Middle HUD Controls (Instant Zero-Distortion Toggle) */}
                {!isHudMinimized && (
                  <div className="mt-2.5 flex flex-col gap-2.5">
                    {/* Guide Opacity Slider Box */}
                    <div className="rounded-2xl bg-[#F3F4F5] px-3 py-2">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-1.5 text-[11px] font-semibold text-[#45474B]">
                          <Droplets className="h-3.5 w-3.5 text-[#45474B]" />
                          <span>Guide Opacity</span>
                        </div>
                        <span className="rounded-full bg-black px-2 py-0.5 text-[10px] font-bold text-white">
                          {opacity}%
                        </span>
                      </div>

                      <div className="my-1.5 flex items-center">
                        <input
                          type="range"
                          min={10}
                          max={100}
                          value={opacity}
                          onChange={(e) => setOpacity(Number(e.target.value))}
                          aria-label="Guide Opacity"
                          className="ghost-slider w-full"
                          style={{
                            background: `linear-gradient(to right, #000000 0%, #000000 ${
                              ((opacity - 10) / 90) * 100
                            }%, #E1E3E4 ${
                              ((opacity - 10) / 90) * 100
                            }%, #E1E3E4 100%)`,
                            borderRadius: "9999px",
                          }}
                        />
                      </div>

                      <div className="flex items-center justify-between text-[9px] font-semibold text-[#76777B]">
                        <span>Faint 10%</span>
                        <span>Balanced</span>
                        <span>Solid 100%</span>
                      </div>
                    </div>

                    {/* 3-Column Transformation Buttons Row */}
                    <div className="grid grid-cols-3 gap-1.5">
                      <button
                        type="button"
                        onClick={() => setIsMirrored((prev) => !prev)}
                        className={`flex flex-col items-center justify-center gap-1 rounded-2xl py-2 px-1 text-[10px] font-semibold transition-colors ${
                          isMirrored
                            ? "bg-[#2E3132] text-white"
                            : "bg-[#F3F4F5] text-[#191C1D]"
                        }`}
                      >
                        <FlipHorizontal2
                          className={`h-4 w-4 ${
                            isMirrored ? "text-[#6FFBBE]" : "text-[#555F6D]"
                          }`}
                        />
                        <span className="truncate max-w-full">
                          {isMirrored ? "Flip H (Mirror)" : "Flip H"}
                        </span>
                      </button>

                      <button
                        type="button"
                        onClick={handleResetScale}
                        className="flex flex-col items-center justify-center gap-1 rounded-2xl bg-[#F3F4F5] py-2 px-1 text-[10px] font-semibold text-[#191C1D] active:bg-[#E1E3E4] transition-colors"
                      >
                        <RotateCcw className="h-4 w-4 text-[#555F6D]" />
                        <span>Reset Scale</span>
                      </button>

                      <button
                        type="button"
                        onClick={() => setShowGrid((prev) => !prev)}
                        className={`flex flex-col items-center justify-center gap-1 rounded-2xl py-2 px-1 text-[10px] font-semibold transition-colors ${
                          showGrid
                            ? "bg-[#2E3132] text-white"
                            : "bg-[#F3F4F5] text-[#191C1D]"
                        }`}
                      >
                        <Crop
                          className={`h-4 w-4 ${
                            showGrid ? "text-[#6FFBBE]" : "text-[#555F6D]"
                          }`}
                        />
                        <span>Crop &amp; Align</span>
                      </button>
                    </div>
                  </div>
                )}

                {/* Primary Passthrough Lock CTA + Save Bookmark Button */}
                <div className="mt-2.5 flex items-center gap-2">
                  <button
                    type="button"
                    onClick={() => setIsLockedPassthrough((prev) => !prev)}
                    className={`flex-1 flex flex-col items-center justify-center rounded-full py-2 px-3 text-white transition-colors shadow-sm ${
                      isLockedPassthrough ? "bg-[#005236]" : "bg-black"
                    }`}
                  >
                    <div className="flex items-center gap-1.5 text-xs font-bold">
                      {isLockedPassthrough ? (
                        <CheckCircle2 className="h-3.5 w-3.5 text-[#6FFBBE]" />
                      ) : (
                        <Lock className="h-3.5 w-3.5 text-[#6FFBBE]" />
                      )}
                      <span>
                        {isLockedPassthrough
                          ? "Passthrough Active"
                          : "Lock Pose (Passthrough)"}
                      </span>
                    </div>
                    <span className="text-[9px] text-white/80">
                      {isLockedPassthrough
                        ? "Tap viewfinder anywhere to shoot"
                        : "Taps pass directly to shutter"}
                    </span>
                  </button>

                  <button
                    type="button"
                    onClick={handleSavePose}
                    aria-label="Save Pose to Library"
                    className="flex h-11 w-11 flex-shrink-0 items-center justify-center rounded-full bg-[#F3F4F5] text-[#191C1D] hover:bg-[#E1E3E4] active:scale-95 transition-all"
                  >
                    <BookmarkPlus className="h-4 w-4" />
                  </button>
                </div>
              </motion.div>
            )}
          </div>

          {/* Toast Feedback inside Mockup */}
          <AnimatePresence>
            {savedToast && (
              <motion.div
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: 10 }}
                className="relative z-30 mx-auto rounded-full bg-black/85 px-3.5 py-1.5 text-[11px] font-semibold text-[#6FFBBE] backdrop-blur-md border border-white/10"
              >
                Saved &ldquo;{currentPose.name}&rdquo; to Pose Hub
              </motion.div>
            )}
          </AnimatePresence>

          {/* 5. Underlying Native Camera Shutter Dock (Proves Touch Passthrough) */}
          <div className="relative z-10 px-4 pb-5 pt-3 bg-gradient-to-t from-black/90 via-black/50 to-transparent flex flex-col gap-2">
            <div className="flex items-center justify-between text-[10px] font-medium text-white/75 px-1">
              <span className="rounded-full bg-white/10 px-2.5 py-0.5 backdrop-blur-sm">
                Pose: {currentPose.name}
              </span>
              {shotCount > 0 && (
                <span className="text-emerald-400 font-mono">
                  {shotCount} {shotCount === 1 ? "photo" : "photos"} captured
                </span>
              )}
            </div>

            <div className="flex items-center justify-around pt-1">
              <button
                type="button"
                onClick={cyclePose}
                title="Switch sample pose"
                className="flex flex-col items-center gap-0.5 text-white/85 hover:text-white active:scale-95 transition-transform"
              >
                <div className="flex h-9 w-9 items-center justify-center rounded-full bg-white/15 backdrop-blur-md border border-white/15">
                  <ImageIcon className="h-4 w-4" />
                </div>
                <span className="text-[9px]">Next Pose</span>
              </button>

              {/* Camera Shutter Button */}
              <button
                type="button"
                onClick={triggerShutter}
                aria-label="Trigger Camera Shutter"
                className="group relative flex h-14 w-14 items-center justify-center rounded-full border-[3px] border-white p-1 active:scale-90 transition-transform"
              >
                <div className="h-full w-full rounded-full bg-white group-hover:bg-emerald-300 transition-colors flex items-center justify-center">
                  <Camera className="h-5 w-5 text-black/80" />
                </div>
              </button>

              <button
                type="button"
                onClick={() => setPoseScale((s) => (s === 1 ? 1.15 : 1))}
                title="Toggle 0.5x / 1x Zoom"
                className="flex flex-col items-center gap-0.5 text-white/85 hover:text-white active:scale-95 transition-transform"
              >
                <div className="flex h-9 w-9 items-center justify-center rounded-full bg-white/15 backdrop-blur-md border border-white/15">
                  <RefreshCw className="h-4 w-4" />
                </div>
                <span className="text-[9px]">
                  {poseScale > 1 ? "1.2x Zoom" : "1.0x Fit"}
                </span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
