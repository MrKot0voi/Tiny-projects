#!/usr/bin/env bash
# Временная диагностика: javap-сигнатуры Mojang-классов для работы с текстурами.
set -u
OUT="${1:-build/sigs}"
mkdir -p "$OUT"
MC=""
for j in $(find build "$HOME/.gradle" -name "*.jar" 2>/dev/null); do
  if unzip -l "$j" 2>/dev/null | grep -q 'net/minecraft/client/gui/Gui.class'; then MC="$j"; break; fi
done
echo "MC jar: $MC"
[ -z "$MC" ] && exit 0
for c in com.mojang.blaze3d.platform.NativeImage \
         net.minecraft.client.renderer.texture.DynamicTexture \
         net.minecraft.client.renderer.texture.TextureManager \
         net.minecraft.client.renderer.texture.AbstractTexture \
         net.minecraft.server.packs.resources.Resource \
         net.minecraft.server.packs.resources.ResourceManager \
         net.minecraft.client.renderer.RenderPipelines \
         net.minecraft.client.gui.GuiGraphics; do
  javap -p -s -cp "$MC" "$c" > "$OUT/$(echo "$c" | tr '.' '_').txt" 2>&1 || echo "fail $c" >> "$OUT/errors.txt"
done
ls "$OUT" | wc -l
