param(
    [string]$SourceRoot = (Join-Path $PSScriptRoot "..\stickerpregame"),
    [string]$DestRoot = (Join-Path $PSScriptRoot "..\app\src\main\assets\stickers")
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $SourceRoot)) {
    Write-Error "stickerpregame folder not found at: $SourceRoot`nCopy your StickerGoblin project into ./stickerpregame and run this script again."
}

$dataJsonPath = Join-Path $SourceRoot "data.json"
if (-not (Test-Path $dataJsonPath)) {
    Write-Error @"
data.json not found at: $dataJsonPath

Expected StickerGoblin layout:
  stickerpregame/
  ├── data.json          # sheet list + sticker words
  └── images/            # sheet PNGs referenced by data.json (e.g. images/1.png)
"@
}

$sheetsDir = Join-Path $DestRoot "sheets"
$itemsDir = Join-Path $DestRoot "items"
New-Item -ItemType Directory -Force -Path $sheetsDir | Out-Null
New-Item -ItemType Directory -Force -Path $itemsDir | Out-Null

Get-ChildItem $sheetsDir -File -ErrorAction SilentlyContinue | Remove-Item -Force
Get-ChildItem $itemsDir -File -ErrorAction SilentlyContinue | Remove-Item -Force

$data = Get-Content $dataJsonPath -Raw -Encoding UTF8 | ConvertFrom-Json
if (-not $data) {
    Write-Error "data.json is empty or invalid."
}

$sheets = @()
$stickers = @()
$sheetIndex = 0
$stickerIndex = 0
$missingImages = @()

foreach ($entry in $data) {
    $sheetNumber = $entry.sheet
    $imageRel = [string]$entry.image
    $entryStickers = @($entry.stickers)

    if ($null -eq $sheetNumber -or [string]::IsNullOrWhiteSpace($imageRel) -or $entryStickers.Count -eq 0) {
        Write-Warning "Skipping invalid entry (needs sheet, image, stickers): $($entry | ConvertTo-Json -Compress)"
        continue
    }

    $sheetIndex++
    $sheetId = "sheet_$sheetIndex"
    $sourceImage = Join-Path $SourceRoot ($imageRel -replace '/', '\')

    if (-not (Test-Path $sourceImage)) {
        $missingImages += $imageRel
        $extension = ".png"
    } else {
        $extension = [System.IO.Path]::GetExtension($sourceImage).ToLowerInvariant()
        if ([string]::IsNullOrWhiteSpace($extension)) { $extension = ".png" }
    }

    $destSheetName = "$sheetId$extension"
    $destSheetPath = Join-Path $sheetsDir $destSheetName

    if (Test-Path $sourceImage) {
        Copy-Item -Path $sourceImage -Destination $destSheetPath -Force
    }

    $stickerIds = @()
    foreach ($sticker in $entryStickers) {
        $word = [string]$sticker.word
        if ([string]::IsNullOrWhiteSpace($word)) { continue }

        $stickerIndex++
        $stickerId = "sticker_$stickerIndex"
        $stickerIds += $stickerId

        $vowels = $sticker.vowels
        if ($null -eq $vowels) { $vowels = $sticker.Vowels }
        if ($null -eq $vowels) { $vowels = 0 }

        $stickers += [ordered]@{
            id = $stickerId
            name = $word
            sheetId = $sheetId
            assetPath = ""
            accentHue = ($stickerIndex * 37) % 360
            vowels = [int]$vowels
        }
    }

    if ($stickerIds.Count -eq 0) {
        Write-Warning "Sheet $sheetNumber has no valid sticker words; skipping."
        continue
    }

    $sheets += [ordered]@{
        id = $sheetId
        name = "Sheet $sheetNumber"
        sheetNumber = [int]$sheetNumber
        assetPath = "stickers/sheets/$destSheetName"
        stickerIds = $stickerIds
    }
}

if ($sheets.Count -eq 0 -or $stickers.Count -eq 0) {
    Write-Error "No sheets or stickers were imported. Check data.json and images/."
}

$catalog = [ordered]@{
    source = "stickergoblin"
    sheets = $sheets
    stickers = $stickers
}

$catalogPath = Join-Path $DestRoot "catalog.json"
$catalog | ConvertTo-Json -Depth 8 | Set-Content -Path $catalogPath -Encoding UTF8

Write-Host "Imported $($sheets.Count) sheets and $($stickers.Count) word stickers from data.json."
Write-Host "Catalog written to $catalogPath"

if ($missingImages.Count -gt 0) {
    Write-Warning "Missing sheet images (catalog still created; app will show placeholders):"
    $missingImages | ForEach-Object { Write-Warning "  $_" }
}
