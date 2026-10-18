package com.example.pages.components;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.ex.UIAssertionError;
import com.example.config.TestFlags;
import com.example.pages.MobilePhonesPage;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Condition.clickable;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.Wait;

/**
 * Дропдаун «Каталог» у хедері.
 *
 * УВАГА: hover нестабільний (банер зсуває layout, Angular догружає обробники пізніше).
 * Якщо меню перевіряти не потрібно — надійніше відкрити прямий URL сторінки телефонів.
 */
public class HeaderCatalogMenu {

    private static final Duration SUBMENU_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration MENU_OPEN_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration CATALOG_CLICK_WAIT = Duration.ofSeconds(2);
    private static final Duration SUBMENU_READY_TIMEOUT = Duration.ofSeconds(20);

    private final SelenideElement catalogButton = $(byAttribute("data-testid", "fat_menu_btn"));
    private final SelenideElement fatMenu = $("rz-fat-menu");
    /** css з href*= — byAttribute не вміє «contains». */
    private final SelenideElement smartphonesCategoryLink = $(
            "rz-fat-menu a[data-testid=fat_menu_category_link][href*='telefony-tv-i-ehlektronika']");
    private final SelenideElement phonesSubmenuLink = $(
            "rz-fat-menu a[data-testid=fat_menu_sub_title][href*='mobile-phones']");

    @Step("Відкрити мобільні телефони через меню Каталог (hover)")
    public MobilePhonesPage openMobilePhones() {
        openPhonesSubmenu();
        phonesSubmenuLink.click();
        return new MobilePhonesPage().waitUntilOpened();
    }

    /**
     * Повтори через Selenide Wait (без for/while): меню часто закривається через пізній банер.
     */
    private void openPhonesSubmenu() {
        Wait()
                .withTimeout(SUBMENU_READY_TIMEOUT)
                .pollingEvery(Duration.ofMillis(500))
                .withMessage("Підменю «Телефони» не з'явилось після hover по Каталогу")
                .until(driver -> {
                    try {
                        openCatalogAndHoverSmartphones();
                        phonesSubmenuLink.shouldBe(clickable, SUBMENU_TIMEOUT);
                        return true;
                    } catch (UIAssertionError e) {
                        System.out.println(
                                ">>> Меню Каталог закрилось або підменю не з'явилось — повторюю");
                        return false;
                    }
                });
    }

    private void openCatalogAndHoverSmartphones() {
        openCatalogWhenReady();
        smartphonesCategoryLink.shouldBe(visible, MENU_OPEN_TIMEOUT).hover();
    }

    /**
     * Кнопка може бути в DOM раніше, ніж Angular розетки підвісить обробник кліку.
     * Готовність = після кліку реально з'явився rz-fat-menu.
     */
    private void openCatalogWhenReady() {
        if (fatMenu.is(visible)) {
            return;
        }

        Wait()
                .withTimeout(TestFlags.catalogReadyTimeout())
                .pollingEvery(Duration.ofMillis(400))
                .withMessage("Кнопка «Каталог» видима, але меню так і не відкрилось (JS ще не готовий?)")
                .until(driver -> {
                    if (fatMenu.is(visible)) {
                        return true;
                    }
                    catalogButton.shouldBe(clickable).click();
                    try {
                        fatMenu.shouldBe(visible, CATALOG_CLICK_WAIT);
                        return true;
                    } catch (UIAssertionError e) {
                        System.out.println(
                                ">>> Клік по \"Каталог\" не відкрив меню — повторюю");
                        return false;
                    }
                });
    }
}
