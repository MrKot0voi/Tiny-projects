#!/usr/bin/env bash
# Диагностика: какой экран рисуется при перезагрузке ресурспаков (LoadingOverlay или другой класс).
set -u
OUT="${1:-build/reload}"
mkdir -p "$OUT"
MC=""
for j in $(find build "$HOME/.gradle" -name "*.jar" 2>/dev/null); do
  if unzip -l "$j" 2>/dev/null | grep -q 'net/minecraft/client/gui/Gui.class'; then MC="$j"; break; fi
done
echo "MC jar: $MC"
[ -z "$MC" ] && exit 0
unzip -Z1 "$MC" | grep -E '^net/minecraft/' | sed 's/\.class$//; s#/#.#g' > "$OUT/all-classes.txt"
grep -E 'Reload|Loading|Overlay|ResourceLoad' "$OUT/all-classes.txt" > "$OUT/candidates.txt"
for c in $(cat "$OUT/candidates.txt"); do
  javap -p -s -c -cp "$MC" "$c" > "$OUT/$(echo "$c" | tr '$' '_').txt" 2>/dev/null || true
done
ls "$OUT" | wc -l
