package ru.berdinskiybear.armorhud.config;

/**
 * Замена ukulib StringTranslatable: имя для конфига и ключ перевода.
 */
public interface Translatable {
    String getName();

    String getTranslationKey();
}
