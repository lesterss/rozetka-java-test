package com.example.config;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;

/**
 * Глобальні налаштування браузера і Allure для всіх UI-тестів.
 */
public final class BrowserSetup {

    private BrowserSetup() {
    }

    public static void configure() {
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = TestFlags.defaultTimeoutMs();
        SelenideLogger.addListener("AllureSelenide",
                new AllureSelenide().screenshots(true).savePageSource(true));
    }
}
