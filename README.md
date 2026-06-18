# StickerApk

Android port of the stickerpregame picker with animated sheet shuffling and sequential sticker reveals.

## Setup

1. Copy your **StickerGoblin** project into `./stickerpregame` with this layout:

```
stickerpregame/
├── data.json          # required — sheet numbers, image paths, sticker words
├── images/            # required — sheet PNGs (e.g. 1.png, 5.png, 12.png)
├── main.py
└── stickergoblin/     # Python app code (not imported)
```

Each `data.json` entry looks like:

```json
{
  "sheet": 1,
  "image": "images/1.png",
  "stickers": [
    { "word": "Eldrazi", "vowels": 3 },
    { "word": "Guacamole", "vowels": 4 }
  ]
}
```

Stickers are **words**, not separate image files. Only the full sheet images in `images/` are copied.

2. Import assets into the Android app:

```powershell
.\scripts\import-stickerpregame.ps1
```

3. Open the project in Android Studio, or build from the command line:

```powershell
.\gradlew.bat assembleDebug
```

## App behavior

- **Before roll:** random sticker sheets fade and scale in at three positions, cycling every ~900ms.
- **Roll:** tap **Roll 3 sheets** to draw three sheets (500ms apart), then see all 9 words.
- **Winner:** the word with the most distinct vowels wins (a, e, i, o, u, y; duplicates don't count).
- **Theme:** dark by default; switch to light or system in **Options**.

## Project layout

- `app/src/main/java/com/stickerapk/` — Compose UI and roll logic
- `app/src/main/assets/stickers/` — imported catalog and images
- `scripts/import-stickerpregame.ps1` — copies images from `stickerpregame` and regenerates `catalog.json`
