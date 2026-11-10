# Smooth Scroll (NeoForge 1.21.11)

Порт мода [Smooth Scroll](https://modrinth.com/mod/smooth-scroll) by SlovakSmileSK (GPL-3.0) с Fabric на NeoForge 1.21.11.
Лицензия: GPL-3.0 (см. `LICENSE`).

## Что портировано

- Конфиг на `ModConfigSpec` (раздел в экране конфигурации NeoForge), ключи и дефолты как в 2.6.7.
- Хотбар: плавный селектор, переполнение, статичный селектор, маска.
- Списки и поле ввода: плавная прокрутка `AbstractScrollArea` и `EditBox`.
- Подсказки команд: плавная прокрутка `SuggestionsList`.
- Чат: плавная прокрутка и сдвиг содержимого (`ChatComponent`).
- Креативное меню: сглаживание прокрутки рядов (`CreativeModeInventoryScreen`).

## Что не портировано

- Анимация открытия чата (Opening smoothness).
- Скольжение рядов креативного меню (отрисовка соседнего ряда), остаётся только сглаживание.
- Точная (precise) маска: в Mojang `GuiGraphics` обрезка идёт иначе, пиксельная точность не воспроизведена.
- Совместимость с CondensedInventory и Flow.

## Сборка

GitHub Actions: `.github/workflows/build-smoothscroll.yml`. Локально: `gradle build` в этой папке (нужен JDK 21).
