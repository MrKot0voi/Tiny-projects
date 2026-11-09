# Combat Hitboxes — NeoForge 1.21.11 (порт с Fabric 1.0.10)

Порт мода Combat Hitboxes от Sootysplash (Apache-2.0) на NeoForge для Minecraft 1.21.11.

Что изменено относительно Fabric-версии:
- точка входа: `ModInitializer` заменён на `@Mod(dist = CLIENT)`;
- конфиг: Cloth Config (`autoconfig`) заменён на `ModConfigSpec` (`combat_hitboxes-client.toml`);
- ModMenu заменён на стандартный экран настроек NeoForge (`ConfigurationScreen`);
- логика рендера хитбоксов и скрытия стрел/жала пчелы не менялась.

Сборка: `./gradlew build`, запуск клиента: `./gradlew runClient`.
