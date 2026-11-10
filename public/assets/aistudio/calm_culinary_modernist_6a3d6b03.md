---
name: Calm Culinary Modernist
colors:
  surface: '#f8f9fa'
  surface-dim: '#d9dadb'
  surface-bright: '#f8f9fa'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f3f4f5'
  surface-container: '#edeeef'
  surface-container-high: '#e7e8e9'
  surface-container-highest: '#e1e3e4'
  on-surface: '#191c1d'
  on-surface-variant: '#45474b'
  inverse-surface: '#2e3132'
  inverse-on-surface: '#f0f1f2'
  outline: '#76777b'
  outline-variant: '#c6c6cb'
  surface-tint: '#5c5e64'
  primary: '#000000'
  on-primary: '#ffffff'
  primary-container: '#191c21'
  on-primary-container: '#81848a'
  inverse-primary: '#c4c6cd'
  secondary: '#555f6d'
  on-secondary: '#ffffff'
  secondary-container: '#d6e0f1'
  on-secondary-container: '#596372'
  tertiary: '#000000'
  on-tertiary: '#ffffff'
  tertiary-container: '#002113'
  on-tertiary-container: '#009668'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e1e2e9'
  primary-fixed-dim: '#c4c6cd'
  on-primary-fixed: '#191c21'
  on-primary-fixed-variant: '#44474c'
  secondary-fixed: '#d9e3f4'
  secondary-fixed-dim: '#bdc7d8'
  on-secondary-fixed: '#121c28'
  on-secondary-fixed-variant: '#3e4755'
  tertiary-fixed: '#6ffbbe'
  tertiary-fixed-dim: '#4edea3'
  on-tertiary-fixed: '#002113'
  on-tertiary-fixed-variant: '#005236'
  background: '#f8f9fa'
  on-background: '#191c1d'
  surface-variant: '#e1e3e4'
typography:
  display:
    fontFamily: Inter
    fontSize: 44px
    fontWeight: '700'
    lineHeight: 52px
    letterSpacing: -0.03em
  headline-lg:
    fontFamily: Inter
    fontSize: 30px
    fontWeight: '700'
    lineHeight: 38px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.015em
  headline-sm:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '700'
    lineHeight: 14px
    letterSpacing: 0.05em
rounded:
  sm: 0.5rem
  DEFAULT: 1rem
  md: 1.5rem
  lg: 2rem
  xl: 3rem
  full: 9999px
spacing:
  gutter: 0.75rem
  margin: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.875rem
  space-lg: 1.25rem
  space-xl: 2rem
---

## Brand & Style

This design system delivers an effortless, ultra-clean mobile aesthetic rooted in Scandinavian-inspired minimalism and high-utility culinary workflows. Designed for busy home cooks and meal planners seeking absolute clarity over visual clutter, the interface conveys calm competence, precision, and lightness.

The visual style strips away heavy drop shadows and high-saturation flourishes in favor of expansive white space, light borders, subtle frosted glass HUD elements, and decisive high-contrast focal points. Key interactions are anchored by deep charcoal and soft-pill tactile elements, creating an intuitive flow that prioritizes speed, legibility, and modern mobile ergonomics.

## Colors

The palette revolves around a strict monochrome foundation augmented by functional muted tones:

- **Primary (`#181B20`)**: Deep obsidian charcoal reserved for high-impact calls-to-action, primary navigation states, active segmented pill controls, and primary typography.
- **Secondary (`#4B5563`)**: Cool slate grey for secondary actions, descriptive metadata, and subheadings.
- **Tertiary (`#10B981`)**: A fresh culinary sage emerald used sparingly for savings cues, checkmarks, completion badges, and nutritional highlights.
- **Neutral Canvas (`#F9FAFB`)**: Soft, clean off-white background with pure `#FFFFFF` assigned to elevated card containers, input wells, and action sheets.
- **Border Grayscale (`#E5E7EB` & `#F3F4F6`)**: Precise 1px hairline structural dividers and outlines.

## Typography

Typographic hierarchy relies exclusively on **Inter** to ensure maximum digital clarity, tight glyph metrics, and optimal tabular legibility for recipe pricing, timings, and ingredient quantities.

- **Display & Large Headlines**: Distinct negative tracking (`-0.02em` to `-0.03em`) lends a contemporary editorial poise to question headers (e.g., onboarding screens, meal plan categories).
- **Body Styles**: Default weights remain at regular (400) with line heights calibrated for effortless readability on compact touchscreens.
- **Labels & Micro-Tags**: Micro-headers (such as day-of-week badges and dietary indicators like `KID FRIENDLY`) utilize uppercase transformations with wide letter-spacing (`0.05em`) and bold weights for immediate recognition across dense culinary cards.

## Layout & Spacing

This design system uses a mobile-first fluid single-column layout spanning a standard 16px (`1rem`) outer margin. Form factor adaptations for tablets and wide devices center the core viewport within a maximum 480px column width for single-hand reachability.

- **Horizontal Rhythm**: Multi-card rows or ingredient selectors adhere to an 8px (`0.5rem`) to 12px (`0.75rem`) gutter, ensuring maximum surface hit area while preserving white space.
- **Vertical Rhythm**: Micro-spacing rules dictate strict 4px and 8px gaps between titles, metadata icons, and secondary tags, while primary section groupings transition using 20px (`1.25rem`) to 32px (`2rem`) vertical offsets.
- **Safe Areas & HUD Overlays**: Floating bottom CTA panels leave safe space for system indicators, floating 16px above the device dock.

## Elevation & Depth

Visual hierarchy is maintained without heavy or muddy drop shadows. The design system emphasizes clarity and physical separation through three discrete techniques:

1. **Low-Contrast Hairlines**: Surfaces are delineated using 1px borders (`#E5E7EB` or `#F3F4F6`), preserving flatness and structural discipline.
2. **Backdrop Blurs & Frosted Sheets**: Floating footer panels, bottom navigation bars, and dynamic status bars leverage frosted glass techniques (`backdrop-filter: blur(20px)` at `rgba(255, 255, 255, 0.85)`).
3. **Micro-Diffusion Shadows**: Only interactive floating action buttons and active card drags use shadows: `0 4px 20px -2px rgba(0, 0, 0, 0.05)`, ensuring no visual weight distracts from high-resolution food photography and critical typography.

## Shapes

The shape language combines welcoming curves with architectural structure:

- **Pill Geometry**: Form selectors, filter tabs, action buttons, counter controls, and tags use full pill borders (`rounded-full` / `9999px`) to emphasize tap accessibility and smooth tactile touch points.
- **Card & Media Frames**: Food imagery and recipe list cards utilize balanced rounded-2xl radii (`1rem` / `16px`), softening visual boundaries and harmonizing with standard mobile device screen corners.

## Components

### Buttons
- **Primary CTA**: Large full-width pill button (`height: 52px`, `border-radius: 9999px`) styled in `#181B20` with pure white Inter Bold typography (`15px`). Subtle scale down (`scale: 0.98`) on tap.
- **Secondary / Ghost Action**: Bordered pill button (`height: 34px`, `border-radius: 9999px`) with 1px `#E5E7EB` stroke, `#FFFFFF` interior, charcoal text, and companion inline icon (e.g., the circular "Swap" action).

### Chips & Filter Pills
- **Horizontal Date Scroller**: Segmented pill toggles with a smooth transitions. Active state uses an explicit 1.5px `#181B20` border with bold text on `#FFFFFF`; inactive items use transparent backgrounds with muted `#6B7280` text.
- **Dietary & Attribute Tags**: Compact micro-chips (`padding: 2px 8px`, `border-radius: 6px`) in pale gray `#F3F4F6` with uppercase `label-sm` font.

### Selection Tiles & Ingredient Checklist
- **Interactive Multi-select Cells**: Two-column layout with 44px minimum tap height, `12px` roundedness, 1px `#E5E7EB` border, and pure white fill. Features a radio/checkbox indicator on the right edge: an outer border circle that fills with an inner solid circle or checkmark when selected.

### Stepper & Quantity Adjuster
- Minimalist counter module centered in the canvas featuring large display digits (36px–44px) flanked by understated circular or raw glyph decrement (`–`) and increment (`+`) touch targets.

### Recipe & List Cards
- Flat-format rows consisting of a 72x72px to 80x80px rounded-xl food thumbnail on the left, followed by day/category micro-label, high-contrast dish title, inline metadata (cost per serving, preparation time, like counters), and contextual swap/edit actions aligned to the trailing edge.

### Floating Bottom Sheet & Sticky Summary
- Fixed translucent dock resting at the bottom of the viewport with subtle edge-to-edge separation, dynamically reflecting the live cost offset or completion stats directly above the primary action button.