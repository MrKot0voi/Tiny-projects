# uku's Armor HUD — NeoForge 1.21.11 (порт с Fabric 0.10.2)

Порт uku's Armor HUD (MIT; оригинал BerdinskiyBear, форк uku) на NeoForge для Minecraft 1.21.11.

Что изменено относительно Fabric-версии:
- точка входа `@Mod(dist = CLIENT)` вместо `ModInitializer`;
- ukulib (конфиг и экран настроек) убран: ukulib под NeoForge 1.21.11 не выпущен. Параметры хранятся в `config/ukus-armor-hud.json` и правятся в файле;
- клавиша переключения виджета — через `RegisterKeyMappingsEvent`;
- интеграция с Bedrockify удалена (мод не нужен для работы виджета);
- миксины оставлены без изменений логики.

Сборка: `./gradlew build`, запуск клиента: `./gradlew runClient`.
