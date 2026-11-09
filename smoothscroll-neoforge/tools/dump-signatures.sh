#!/usr/bin/env bash
# Диагностика для портирования: выгружает javap-сигнатуры нужных классов из замапленного Mojang-джарника.
# Временный инструмент, удалим после порта.
set -u
OUT="${1:-build/signatures}"
mkdir -p "$OUT"
MC=""
for j in $(find build "$HOME/.gradle" -name "*.jar" 2>/dev/null); do
  if unzip -l "$j" 2>/dev/null | grep -q 'net/minecraft/client/gui/Gui.class'; then
    MC="$j"
    break
  fi
done
echo "MC jar: $MC"
if [ -z "$MC" ]; then
  echo "MC jar not found"
  exit 0
fi
unzip -Z1 "$MC" | grep -E '^net/minecraft/' | sed 's/\.class$//; s#/#.#g' > "$OUT/all-classes.txt"
grep -E 'ChatComponent|CommandSuggestions|CreativeModeInventoryScreen|AbstractScrollWidget|AbstractContainerScreen|EditBox|AbstractWidget|ScreenRectangle|MouseHandler|DeltaTracker|GuiGraphics|\.Gui(\$.*)?$|\.Minecraft$|ItemPicker|RenderPipelines|Scroll|SelectionList|Inventory$|Font$|Matrix3x2|ARGB|ColorHelper' "$OUT/all-classes.txt" > "$OUT/candidates.txt"
wc -l "$OUT/candidates.txt"
while read -r c; do
  javap -p -s -cp "$MC" "$c" > "$OUT/$(echo "$c" | tr '$' '_').txt" 2>/dev/null || echo "fail: $c" >> "$OUT/errors.txt"
done < "$OUT/candidates.txt"
echo "dumped: $(ls "$OUT" | wc -l) files"
