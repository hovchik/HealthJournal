# Google Play Store assets

Everything you need to fill in the **Main store listing** and **Graphics**
sections of the Google Play Console for HealthJournal.

## Directory layout

```
play-store/
├── graphics/
│   ├── icon_512.png                # 512×512 high-res app icon (32-bit PNG)
│   ├── feature_graphic_1024x500.png  # 1024×500 feature graphic
│   └── src/
│       └── icon_512.svg            # Vector source for the app icon
├── listing/
│   ├── en-US/  ru-RU/  es-ES/  zh-CN/  hy-AM/
│   │   ├── title.txt               # ≤30 chars
│   │   ├── short_description.txt   # ≤80 chars
│   │   └── full_description.txt    # ≤4000 chars
└── README.md
```

## What still needs a real device

Google Play requires **at least two phone screenshots** (1080×1920 or similar
9:16, PNG or JPEG). We did not generate mocked-up screenshots because the Play
Store policy requires them to "accurately represent the app". Capture these
on a phone / emulator and drop them in `play-store/graphics/screenshots/phone/`:

1. Home dashboard with a few sample entries.
2. Add-symptom screen with the intensity slider visible.
3. AI report for the doctor (Reports tab).
4. Medications list with a reminder badge.
5. Settings → Security (biometric / PIN).

Optional: 7" and 10" tablet screenshots (1200×1920 / 1920×1200 etc.).

## How to upload

1. In Play Console open the app → **Grow → Store presence → Main store listing**.
2. Set the **default language** to English (US) and paste:
   - `listing/en-US/title.txt` → App name
   - `listing/en-US/short_description.txt` → Short description
   - `listing/en-US/full_description.txt` → Full description
3. Upload graphics:
   - `graphics/icon_512.png` → App icon
   - `graphics/feature_graphic_1024x500.png` → Feature graphic
   - Phone screenshots → Phone screenshots (min. 2, max. 8)
4. For every other locale (`ru-RU`, `es-ES`, `zh-CN`, `hy-AM`): click
   **Manage translations → Add your own translations**, choose the locale, and
   paste the three text files from the matching folder.
5. **App category**: Lifestyle (personal journal — not Health & Fitness).
   Selecting Health & Fitness, or declaring any "health app" feature in the
   App content form, requires an organization developer account under the
   Play Console Requirements policy. Individual accounts must keep the
   category and declarations aligned with a personal journaling tool.
6. **Tags**: Personal journal, Daily planner, Reminder.
7. **Contact details**: the dev contact address you use for Play Support.
8. **Privacy policy URL**: point to a hosted copy of `PRIVACY_POLICY.md`.

## App content questionnaires

Use these repository docs as the source of truth when filling the Console
forms:

| Console section | Source doc |
|---|---|
| Data safety | `DATA_SAFETY.md` |
| Privacy policy | `PRIVACY_POLICY.md` |
| Terms of use | `TERMS_OF_USE.md` |
| Target audience | 13+ (see §5 of the Privacy Policy) |
| Ads | No ads |
| Government app | No |
| News app | No |
| Financial services | No |
| VPN service | No |
| COVID-19 contact-tracing | No |
| Health features | **None declared.** The app is a personal journal; symptom and medication notes are free-form text the user types, not data read from a platform health API. Do not tick "medical app", "human subjects research", or "Health Connect client" in the App content form. |
| Permissions declaration | `USE_EXACT_ALARM` — "Calendar or reminder app" (appointment and dose reminders must not drift) |

## Regenerating the graphics

The icon is source-controlled as SVG; PNGs are rendered with `cairosvg`.

```bash
pip install --user cairosvg Pillow

# 512×512 app icon
python3 -c "import cairosvg; cairosvg.svg2png(
    url='play-store/graphics/src/icon_512.svg',
    write_to='play-store/graphics/icon_512.png',
    output_width=512, output_height=512)"

# Feature graphic (composes icon + gradient + text)
python3 play-store/graphics/src/build_feature_graphic.py
```

## Character-count check

All strings were measured against Play Console limits and fit:

| Locale | title | short | full |
|---|---|---|---|
| en-US | 26/30 | 79/80 | 3112/4000 |
| ru-RU | 22/30 | 78/80 | 3050/4000 |
| es-ES | 22/30 | 77/80 | 3363/4000 |
| zh-CN | 11/30 | 31/80 | 1236/4000 |
| hy-AM | 21/30 | 62/80 | 3431/4000 |
