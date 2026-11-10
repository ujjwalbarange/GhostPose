"use client";

import React, { useState } from "react";
import { ChevronDown, ShieldCheck, Layers, Camera, Lock } from "lucide-react";

interface FaqItem {
  id: string;
  question: string;
  answer: React.ReactNode;
  badge: string;
  icon: React.ReactNode;
}

const FAQ_ITEMS: FaqItem[] = [
  {
    id: "android-13-14-15-install",
    badge: "Android 13 / 14 / 15 Guide",
    icon: <ShieldCheck className="h-4 w-4 text-emerald-400" />,
    question: "How do I install the APK on Android 13, 14, or 15?",
    answer: (
      <div className="space-y-2.5 text-sm text-slate-300 leading-relaxed">
        <p>
          Installing GhostPose directly takes less than 15 seconds. Because it
          uses Android&apos;s system overlay capability outside the Play Store,
          Android 13+ includes a one-time security step called{" "}
          <strong className="text-white">Restricted Settings</strong>:
        </p>
        <ol className="list-decimal list-inside space-y-1.5 text-slate-300">
          <li>
            Tap <strong className="text-white">Download GhostPose APK</strong>{" "}
            and open the file. If prompted, allow your browser to{" "}
            <code className="rounded bg-white/10 px-1.5 py-0.5 font-mono text-xs text-emerald-300">
              Install unknown apps
            </code>
            .
          </li>
          <li>
            On Android 13, 14, or 15, if{" "}
            <code className="rounded bg-white/10 px-1.5 py-0.5 font-mono text-xs text-emerald-300">
              Display over other apps
            </code>{" "}
            is grayed out with a &ldquo;Restricted setting&rdquo; notice, open{" "}
            <strong className="text-white">
              Settings → Apps → GhostPose
            </strong>
            .
          </li>
          <li>
            Tap the <strong className="text-white">⋮ (three dots)</strong> icon
            in the top-right corner of the App Info screen and select{" "}
            <strong className="text-emerald-400">
              Allow restricted settings
            </strong>{" "}
            (verify with your fingerprint/PIN).
          </li>
          <li>
            Return to GhostPose and toggle on{" "}
            <strong className="text-white">Display over other apps</strong>.
            You&apos;re ready to shoot!
          </li>
        </ol>
      </div>
    ),
  },
  {
    id: "overlay-permission-reason",
    badge: "Architecture",
    icon: <Layers className="h-4 w-4 text-sky-400" />,
    question: "Why does the app need Overlay Permission?",
    answer: (
      <div className="space-y-2 text-sm text-slate-300 leading-relaxed">
        <p>
          GhostPose uses Android&apos;s native{" "}
          <code className="rounded bg-white/10 px-1.5 py-0.5 font-mono text-xs text-sky-300">
            WindowManager (SYSTEM_ALERT_WINDOW)
          </code>{" "}
          dual-window architecture so your reference pose can float directly on
          top of your real camera app.
        </p>
        <p>
          When you tap <strong className="text-white">Lock Pose</strong>,
          GhostPose applies{" "}
          <code className="rounded bg-white/10 px-1.5 py-0.5 font-mono text-xs text-emerald-300">
            FLAG_NOT_TOUCHABLE | FLAG_NOT_FOCUSABLE
          </code>{" "}
          to the image layer. This guarantees that 100% of your finger taps pass
          straight through the ghost pose into your native camera shutter, zoom
          rings, and focus reticle with zero lag.
        </p>
      </div>
    ),
  },
  {
    id: "third-party-cameras",
    badge: "Compatibility",
    icon: <Camera className="h-4 w-4 text-emerald-400" />,
    question: "Does it work with Instagram, Snapchat, and stock cameras?",
    answer: (
      <div className="space-y-2 text-sm text-slate-300 leading-relaxed">
        <p>
          <strong className="text-white">Yes—every camera app on Android.</strong>{" "}
          Because GhostPose operates at the system window layer rather than
          replacing your camera with a low-quality custom camera SDK, you keep
          100% of your phone&apos;s native HDR processing, night mode, 0.5x
          ultra-wide lens, and stabilization.
        </p>
        <p>
          It works seamlessly over{" "}
          <strong className="text-white">
            Google Pixel Camera, Samsung Camera, Moto Camera, Instagram Stories,
            Snapchat, TikTok, and Leica/Xiaomi Camera
          </strong>
          .
        </p>
      </div>
    ),
  },
  {
    id: "privacy-offline",
    badge: "Privacy & Security",
    icon: <Lock className="h-4 w-4 text-sky-400" />,
    question: "Does GhostPose access my camera feed or upload my photos?",
    answer: (
      <div className="space-y-2 text-sm text-slate-300 leading-relaxed">
        <p>
          <strong className="text-white">Never.</strong> GhostPose does{" "}
          <em>not</em> even request the Android{" "}
          <code className="rounded bg-white/10 px-1.5 py-0.5 font-mono text-xs text-emerald-300">
            android.permission.CAMERA
          </code>{" "}
          permission! It cannot see, record, or read your camera sensor.
        </p>
        <p>
          All saved poses and opacity presets are stored strictly offline on
          your device in a local SQLite (Room) database with zero analytics
          SDKs, zero telemetry, and no account sign-in.
        </p>
      </div>
    ),
  },
];

export default function FaqSection() {
  const [openId, setOpenId] = useState<string | null>(
    "android-13-14-15-install"
  );

  const toggleItem = (id: string) => {
    setOpenId((prev) => (prev === id ? null : id));
  };

  return (
    <div className="w-full space-y-3">
      {FAQ_ITEMS.map((item) => {
        const isOpen = openId === item.id;
        return (
          <div
            key={item.id}
            className={`rounded-2xl border transition-colors ${
              isOpen
                ? "border-emerald-500/40 bg-white/[0.05]"
                : "border-white/[0.08] bg-white/[0.02] hover:bg-white/[0.04]"
            } backdrop-blur-xl overflow-hidden`}
          >
            <button
              type="button"
              onClick={() => toggleItem(item.id)}
              aria-expanded={isOpen}
              className="w-full px-5 py-4 text-left flex items-center justify-between gap-4"
            >
              <div className="flex flex-col gap-1">
                <div className="flex items-center gap-2">
                  {item.icon}
                  <span className="font-mono text-[11px] font-semibold uppercase tracking-wider text-emerald-400">
                    {item.badge}
                  </span>
                </div>
                <span className="text-base sm:text-lg font-bold text-white">
                  {item.question}
                </span>
              </div>
              <div
                className={`flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full border border-white/10 bg-white/5 text-slate-300 transition-transform duration-200 ${
                  isOpen ? "rotate-180 bg-emerald-500/20 text-emerald-300" : ""
                }`}
              >
                <ChevronDown className="h-4 w-4" />
              </div>
            </button>

            {isOpen && (
              <div className="px-5 pb-5 pt-1 border-t border-white/[0.06]">
                {item.answer}
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
}
