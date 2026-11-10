#!/usr/bin/env bash
# Временная диагностика (ветка runclient-test): сигнатуры и байткод для точной маски, скольжения креатива и автотеста.
set -u
OUT="${1:-diag/dump2}"
mkdir -p "$OUT/bytecode"
MC=""
for j in $(find build "$HOME/.gradle" -name "*.jar" 2>/dev/null); do
  if unzip -l "$j" 2>/dev/null | grep -q 'net/minecraft/client/gui/Gui.class'; then MC="$j"; break; fi
done
echo "MC jar: $MC"
[ -z "$MC" ] && exit 0
CLASSES='net.minecraft.client.gui.screens.ConnectScreen net.minecraft.client.multiplayer.ServerData net.minecraft.client.multiplayer.resolver.ServerAddress net.minecraft.client.gui.render.GuiRenderer net.minecraft.client.gui.screens.ChatScreen net.minecraft.client.gui.screens.OptionsScreen net.minecraft.client.gui.GuiGraphics$ScissorStack net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen$ItemPickerMenu net.minecraft.client.gui.components.ChatComponent$DrawingBackgroundGraphicsAccess net.minecraft.client.gui.navigation.ScreenRectangle net.minecraft.client.gui.GuiGraphics net.minecraft.client.gui.screens.inventory.AbstractContainerScreen net.minecraft.client.gui.components.ChatComponent$ChatGraphicsAccess net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen'
for c in $CLASSES; do
  n=$(echo "$c" | tr '$' '_')
  javap -p -s -cp "$MC" "$c" > "$OUT/$n.txt" 2>/dev/null
  javap -p -s -c -cp "$MC" "$c" > "$OUT/bytecode/$n.txt" 2>/dev/null
done
ls "$OUT" | wc -l
