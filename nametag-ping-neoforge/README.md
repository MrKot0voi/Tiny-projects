# Nametag Ping — NeoForge 1.21.11 (порт с Fabric 1.1.0)

Порт мода Nametag Ping от ic3dwtf (MIT) на NeoForge для Minecraft 1.21.11.
Логика взята из ветки 26.2 (Mojang-маппинги), согласована с исходниками NeoForge 1.21.11.

Изменения относительно Fabric-версии:
- Cloth Config и ModMenu заменены на `ModConfigSpec` и экран настроек NeoForge;
- точка входа `@Mod(dist = CLIENT)`;
- миксины на `EntityRenderer.extractRenderState` и `TextDisplayRenderer` без изменений по логике.

Сборка: `./gradlew build`, запуск клиента: `./gradlew runClient`.
