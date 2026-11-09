# Shield Fixes — NeoForge 1.21.11 (порт с Fabric 2.1.0)

Порт мода Shield Fixes от Walksy (MIT) на NeoForge для Minecraft 1.21.11.

Что изменено относительно Fabric-версии:
- точка входа `@Mod(dist = CLIENT)` вместо `ModInitializer`;
- настройки WalksyLib (`Config.builder`) заменены на `ModConfigSpec`: `Mod Enabled` и `Factor Delay`; экран настроек NeoForge;
- логика миксинов и менеджеров не менялась. Все состояния щита берутся из WalksyLib (вариант NeoForge).

Зависимость: WalksyLib (NeoForge, 1.21.11). Её jar собирается в CI и кладётся в `libs/` (только compile-time); в игре нужно установить WalksyLib NeoForge рядом с модом.

Сборка: `./gradlew build` (сначала положи jar WalksyLib в `libs/`).
