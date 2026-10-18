package com.example.pages;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.ex.UIAssertionError;
import com.example.pages.components.HeaderCatalogMenu;
import com.example.support.SiteBlockHandler;
import com.example.support.SlowNetwork;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

/**
 * Головна сторінка rozetka.com.ua (десктоп).
 */
public class HomePage {

    public static final String URL = "https://rozetka.com.ua/";

    private static final Duration PAGE_SETTLE_TIMEOUT = Duration.ofSeconds(5);

    /** На сторінці 500 шапка з «Каталог» є, а цього сайдбару — немає. */
    private final SelenideElement mainSidebar = $("rz-main-page-sidebar");
    private final SelenideElement catalogButton = $(byAttribute("data-testid", "fat_menu_btn"));
    private final SelenideElement topBannerLink = $("rz-top-page-banner a");

    private final HeaderCatalogMenu catalogMenu = new HeaderCatalogMenu();

    @Step("Відкрити головну сторінку Rozetka")
    public static HomePage openHome() {
        SlowNetwork.enableIfRequested();
        open(URL);
        return new HomePage().waitUntilLoaded();
    }

    @Step("Дочекатись завантаження головної сторінки")
    public HomePage waitUntilLoaded() {
        SiteBlockHandler.ensureNotBlocked("Відкриття головної сторінки", "rz-main-page-sidebar");
        mainSidebar.shouldBe(visible);
        catalogButton.shouldBe(visible);
        waitForLayoutToSettle();
        return this;
    }

    /**
     * Жовтий банер часто підвантажується пізніше і зсуває layout — дропдаун Каталогу тоді закривається.
     * Банер опційний: його відсутність не вважаємо помилкою.
     */
    private void waitForLayoutToSettle() {
        try {
            topBannerLink.should(exist, PAGE_SETTLE_TIMEOUT);
        } catch (UIAssertionError ignored) {
            // банер може не показуватись
        }
    }

    /**
     * Складніший шлях до телефонів (кнопка Каталог у хедері + hover).
     * Альтернатива: прямий URL — див. коментар у {@link HeaderCatalogMenu}.
     */
    @Step("Відкрити сторінку мобільних телефонів через меню Каталог")
    public MobilePhonesPage openMobilePhonesViaCatalog() {
        return catalogMenu.openMobilePhones();
    }
}
