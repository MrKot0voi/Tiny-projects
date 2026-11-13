#!/usr/bin/env bash
# Диагностика: что рисует ранний экран NeoForge (earlydisplay) и какие SPI-сервисы грузятся.
set -u
OUT="${1:-build/early}"
mkdir -p "$OUT"
BASE="https://maven.neoforged.net/releases/net/neoforged/fancymodloader"
curl -sfL -o "$OUT/earlydisplay.jar" "$BASE/earlydisplay/10.0.36/earlydisplay-10.0.36.jar" || echo "no earlydisplay" >> "$OUT/errors.txt"
curl -sfL -o "$OUT/loader.jar" "$BASE/loader/10.0.36/loader-10.0.36.jar" || echo "no loader" >> "$OUT/errors.txt"
for j in earlydisplay loader; do
  [ -f "$OUT/$j.jar" ] || continue
  unzip -Z1 "$OUT/$j.jar" > "$OUT/$j-entries.txt"
  mkdir -p "$OUT/$j-services"
  for s in $(grep '^META-INF/services/' "$OUT/$j-entries.txt"); do
    unzip -p "$OUT/$j.jar" "$s" > "$OUT/$j-services/$(basename "$s")" 2>/dev/null
  done
  for c in $(grep -E '\.class$' "$OUT/$j-entries.txt" | grep -iE 'LoadingScreen|Renderer|Color|Early|Immediate|Window' | sed 's/\.class$//; s#/#.#g'); do
    javap -p -c -constants -cp "$OUT/$j.jar" "$c" > "$OUT/$j-$(echo "$c" | tr '.' '_').txt" 2>&1 || true
  done
done
ls -la "$OUT" | head -40

# SPI для графической инициализации (Sodium отсюда стартует раньше окна загрузки).
curl -sfL -o "$OUT/neoforge.jar" "https://maven.neoforged.net/releases/net/neoforged/neoforge/21.11.48/neoforge-21.11.48-universal.jar" || echo "no neoforge" >> "$OUT/errors.txt"
if [ -f "$OUT/neoforge.jar" ]; then
  unzip -Z1 "$OUT/neoforge.jar" > "$OUT/neoforge-entries.txt"
  for c in $(grep -E '^net/neoforged/neoforgespi/earlywindow/.*\.class$' "$OUT/neoforge-entries.txt" | sed 's/\.class$//; s#/#.#g'); do
    javap -p -s -cp "$OUT/neoforge.jar" "$c" > "$OUT/neoforge-$(echo "$c" | tr '.' '_').txt" 2>&1 || true
  done
fi
