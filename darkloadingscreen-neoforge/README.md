# Dark Loading Screen — NeoForge 1.21.11 (порт с Fabric 1.6.17)

Порт мода Dark Loading Screen (A5b84, LGPL-3.0-or-later) на NeoForge для Minecraft 1.21.11.

Логика миксинов взята из версии 1.6.17 (1.21.6), имена переведены на Mojang-маппинги 1.21.11.

Что изменено относительно Fabric-версии:
- Cloth Config и ModMenu убраны. Настройки теперь в `ModConfigSpec` (`dark_loading_screen-client.toml`), экран настроек NeoForge. Цвета задаются шестнадцатеричной строкой RRGGBB;
- кастомный экран предпросмотра (PreviewSplashOverlay) не портирован;
- compat-миксин для Semitranslucency Fix не портирован (в исходнике был закомментирован);
- `modid` стал `dark_loading_screen`, потому что NeoForge не принимает дефис.

Сборка: `./gradlew build`, запуск клиента: `./gradlew runClient`.
