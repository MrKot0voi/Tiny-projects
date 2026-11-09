# No Peeking! — NeoForge 1.21.11 (порт с Fabric 1.2.4)

Порт мода No Peeking! (GPL-3.0, авторы Wojciech Paruch, joshieman06, themg95) на NeoForge для Minecraft 1.21.11.

Что изменено относительно Fabric-версии:
- Cloth Config (AutoConfig) заменён на `ModConfigSpec`; экран настроек открывается через стандартный экран конфигов NeoForge;
- ModMenu-интеграция удалена, её заменяет экран NeoForge;
- миксин `renderWidget` переведён на Mojang-имена (`EditBox`, `value`), логика та же.

Сборка: `./gradlew build`, запуск клиента: `./gradlew runClient`.
