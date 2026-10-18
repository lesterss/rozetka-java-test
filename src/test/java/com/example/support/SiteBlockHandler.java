package com.example.support;

import com.codeborne.selenide.ex.UIAssertionError;
import com.example.config.TestFlags;
import io.qameta.allure.Step;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

import static com.codeborne.selenide.Condition.clickable;
import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.Wait;
import static com.codeborne.selenide.Selenide.refresh;
import static com.codeborne.selenide.Selenide.screenshot;
import static com.codeborne.selenide.WebDriverRunner.url;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Розпізнає антибот-сторінки Rozetka/Cloudflare (500 і капча) і дає зрозуміле падіння.
 * Капчу не обходить; у ручному режимі чекає кліку людини.
 */
public final class SiteBlockHandler {

    private static final String SERVER_ERROR_PAGE = "rz-server-error";
    private static final String SERVER_ERROR_RELOAD_BUTTON =
            "rz-server-error button[onclick*='location.reload']";
    private static final String CAPTCHA_WIDGET = "[name=cf-turnstile-response]";
    private static final int SERVER_ERROR_RELOAD_ATTEMPTS = 2;
    private static final Duration RELOAD_BUTTON_TIMEOUT = Duration.ofSeconds(5);

    private SiteBlockHandler() {
    }

    /**
     * Чекає очікуваний елемент або сторінку блокування.
     * На 500 — до 2 кліків «Перезавантажити сторінку» (+ F5, якщо кнопка не спрацювала).
     */
    @Step("Перевірити, що сайт не заблокував крок: {step}")
    public static void ensureNotBlocked(String step, String expectedElementCss) {
        for (int reloads = 0; ; reloads++) {
            $(String.join(", ", expectedElementCss, SERVER_ERROR_PAGE, CAPTCHA_WIDGET)).should(exist);

            if ($(CAPTCHA_WIDGET).exists()) {
                handleCaptcha(step, expectedElementCss);
            }
            if (!$(SERVER_ERROR_PAGE).exists()) {
                return;
            }
            if (reloads == SERVER_ERROR_RELOAD_ATTEMPTS) {
                failBlocked(step, "сайт показав сторінку помилки 500 (\"Щось пішло не так...\")"
                        + " навіть після " + SERVER_ERROR_RELOAD_ATTEMPTS + " перезавантажень");
            }
            reloadServerErrorPage(step, reloads + 1);
        }
    }

    private static void handleCaptcha(String step, String expectedElementCss) {
        if (!TestFlags.MANUAL_CAPTCHA) {
            failBlocked(step, "сайт показав капчу Cloudflare (\"Трохи зачекайте...\")");
        }
        System.out.printf("""
                >>> Крок "%s": сайт показав капчу Cloudflare.
                >>> Пройди її вручну у відкритому вікні браузера. Тест чекає до %d хв.%n""",
                step, TestFlags.MANUAL_CAPTCHA_TIMEOUT.toMinutes());
        $(String.join(", ", expectedElementCss, SERVER_ERROR_PAGE))
                .should(exist.because("капчу Cloudflare не пройдено вручну за "
                        + TestFlags.MANUAL_CAPTCHA_TIMEOUT.toMinutes() + " хв"),
                        TestFlags.MANUAL_CAPTCHA_TIMEOUT);
        System.out.println(">>> Капчу пройдено, тест продовжує роботу.");
    }

    private static void reloadServerErrorPage(String step, int attempt) {
        System.out.printf(">>> Крок \"%s\": сторінка 500, натискаю \"Перезавантажити сторінку\" (спроба %d з %d)%n",
                step, attempt, SERVER_ERROR_RELOAD_ATTEMPTS);
        WebElement oldPage = $("html").toWebElement();
        $(SERVER_ERROR_RELOAD_BUTTON).shouldBe(clickable).click();
        if (!pageBecameStale(oldPage, RELOAD_BUTTON_TIMEOUT)) {
            System.out.println(">>> Кнопка не перезавантажила сторінку, перезавантажую через браузер (як F5)");
            refresh();
        }
    }

    private static boolean pageBecameStale(WebElement oldPage, Duration timeout) {
        try {
            Wait().withTimeout(timeout).until(ExpectedConditions.stalenessOf(oldPage));
            return true;
        } catch (TimeoutException | UIAssertionError e) {
            return false;
        }
    }

    private static void failBlocked(String step, String reason) {
        String screenshotPath = screenshot("blocked_by_site");
        fail("""
                Крок "%s": %s.
                Це не дефект тесту і не помилка в селекторах: антибот-захист Rozetka (Cloudflare)
                розпізнав автоматизований браузер (navigator.webdriver, чистий профіль без cookie,
                багато запусків з однієї IP-адреси) і замість сторінки віддав блокування.
                Що робити: зачекати 15-30 хвилин або запустити тест з іншої мережі, не запускати тест багато разів підряд.
                Якщо була капча, можна запустити з -Dmanual.captcha=true і пройти її вручну у вікні браузера.
                URL: %s
                Скріншот: %s"""
                .formatted(step, reason, url(), screenshotPath));
    }
}
