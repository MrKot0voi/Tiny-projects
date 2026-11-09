# Armor Chroma — NeoForge 1.21.11 (порт с Fabric 1.21.6)

Порт мода Armor Chroma (исходная версия 1.2.11+1.21.x для Fabric) на NeoForge для Minecraft 1.21.11.

## Сборка

1. Установи JDK 21.
2. Положи Gradle wrapper (`gradlew`, `gradle/wrapper/`) из NeoForge MDK для 1.21.11 или выполни `gradle wrapper` с Gradle 9.x.
3. Проверь версии в `gradle.properties`: `neo_version` и `moddev_version` (актуальные значения — на https://projects.neoforged.net).
4. `./gradlew build` — jar появится в `build/libs/`.
5. `./gradlew runClient` — запуск клиента для проверки.

## Что изменено относительно Fabric-версии

- Точка входа: `ClientModInitializer` заменён на `@Mod(dist = CLIENT)`.
- Reload-листенер иконок: `SimpleResourceReloadListener` заменён на `ResourceManagerReloadListener`, регистрация через `RegisterClientReloadListenersEvent`.
- Cloth Config + ModMenu заменены на `ModConfigSpec` (файл `config/ArmorChromaConfig.java`), экран настроек открывается через стандартный экран конфигов NeoForge.
- Debug-команда `/armorchroma` регистрируется только вне production-среды.
- Миксины переписаны на Mojang-имена (в Fabric-версии были intermediary-имена вида `method_58478`). Mixin-конфиг объявлен в `neoforge.mods.toml` через `[[mixins]]`.
- Все промежуточные имена Minecraft переведены в Mojang-маппинги.

## Что НЕ проверено при портировании (нужно сверить при первой сборке)

Порт писался без доступа к jar-файлам NeoForge и Minecraft 1.21.11 (их не получилось скачать в окружении, где велась работа), поэтому имена классов и методов взяты из API 1.21.x по памяти. Ожидаемые места, где могут быть ошибки компиляции или поведения:

1. `build.gradle` / `gradle.properties` — версии `neo_version` и `moddev_version` нужно подставить актуальные.
2. `InGameHudMixin` — отступ `ARMOR_ROW_OFFSET = 10` подобран по смыслу. В оригинале это было приватное поле vanilla `field_32170`; сверь значение с `Gui.renderArmor` в 1.21.11 и поправь.
3. `GuiArmor.drawTexturedGlintRect` — в оригинале для свечения использовался отдельный пайплайн (`field_56816`), здесь стоит `RenderPipelines.GUI_TEXTURED`. Если свечение выглядит неправильно, подбери пайплайн (см. `RenderPipelines` в 1.21.11).
4. `ARGB.gray(float)`, `ItemRenderer.ENCHANTED_GLINT_ITEM`, `EquipmentAssets.TURTLE_SCUTE`, `EquipmentSlot.VALUES`, `ItemStack.isFoil()`, `ChatFormatting.GRAY` — проверь имена.
5. `FMLEnvironment.production` — используется для условия регистрации debug-команды.
6. `[[mixins]]` в `neoforge.mods.toml` — если миксины не применяются, добавь конфиг по документации NeoForge для MixinExtras/mixins.
7. Альтернативный пак иконок (`resourcepacks/alternative-icons`) в сборку не встроен и автоматически не регистрируется. Его можно заархивировать в zip и включить вручную в меню ресурс-паков, либо добавить регистрацию через `AddPackFindersEvent`.
8. Ключи локализации для экрана настроек NeoForge (`armorchroma.configuration.*`) в `en_us.json` не добавлены; без них в экране будут видны технические имена опций.

Если при сборке появятся ошибки, пришли лог Gradle — поправлю.
