import type { Metadata, Viewport } from "next";
import { Inter, Space_Grotesk } from "next/font/google";
import "./globals.css";

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-inter",
  display: "swap",
});

const spaceGrotesk = Space_Grotesk({
  subsets: ["latin"],
  variable: "--font-mono",
  display: "swap",
});

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  maximumScale: 5,
  themeColor: "#0A0A0C",
};

export const metadata: Metadata = {
  title: "GhostPose — Copy Any Pose Direct on Your Android Camera",
  description:
    "Overlay reference poses from Pinterest, Instagram, or your gallery with dynamic transparency directly over any Android camera app. 100% touch passthrough, zero ads, offline library.",
  keywords: [
    "GhostPose",
    "Android camera overlay",
    "pose reference app",
    "Pinterest pose overlay",
    "camera ghost overlay APK",
    "touch passthrough camera guide",
  ],
  authors: [{ name: "GhostPose Engineering" }],
  openGraph: {
    title: "GhostPose — Copy Any Pose Direct on Your Camera",
    description:
      "Overlay reference poses from Pinterest, Instagram, or your gallery with dynamic transparency directly over your camera. Full touch passthrough—zero distractions.",
    type: "website",
    siteName: "GhostPose",
    locale: "en_US",
  },
  twitter: {
    card: "summary_large_image",
    title: "GhostPose — Android Camera Pose Overlay Utility",
    description:
      "Overlay reference poses over any camera app with 100% shutter touch passthrough. Free & Open Source APK.",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className={`dark ${inter.variable} ${spaceGrotesk.variable}`}>
      <body className="font-sans bg-[#0A0A0C] text-slate-50 selection:bg-emerald-400 selection:text-slate-950 min-h-screen flex flex-col">
        {children}
      </body>
    </html>
  );
}
