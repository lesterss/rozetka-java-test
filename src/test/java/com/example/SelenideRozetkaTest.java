package com.example;

import com.example.config.BrowserSetup;
import com.example.model.Product;
import com.example.pages.HomePage;
import com.example.pages.MobilePhonesPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.codeborne.selenide.Selenide.closeWebDriver;
import static com.codeborne.selenide.Selenide.cookies;
import static com.codeborne.selenide.Selenide.localStorage;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.sessionStorage;
import static com.codeborne.selenide.WebDriverRunner.hasWebDriverStarted;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test task General QA:
 * <ul>
 *   <li>Open rozetka.com.ua</li>
 *   <li>Open mobile phones page</li>
 *   <li>Get names and prices for all mobile devices for the first page</li>
 *   <li>Get prices and devices names with the label "Top Sales"</li>
 * </ul>
 *
 * Десктопна версія. Основний шлях — меню «Каталог»; запасний — прямий URL (див. другий тест).
 *
 * <pre>
 *   mvn test -Dtest=SelenideRozetkaTest
 *   mvn test -Dtest=SelenideRozetkaTest#mobilePhonesViaDirectUrl
 *   mvn test -Dtest=SelenideRozetkaTest "-Dmanual.captcha=true"
 *   mvn io.qameta.allure:allure-maven:serve
 * </pre>
 */
@Epic("Rozetka UI")
@Feature("Каталог мобільних телефонів")
public class SelenideRozetkaTest {

    @BeforeAll
    static void setUpBrowser() {
        BrowserSetup.configure();
    }

    @AfterEach
    void clearBrowserState() {
        if (!hasWebDriverStarted()) {
            return;
        }
        // Cookies/storage самі по собі не змінюють URL вкладки.
        // Після тесту з прямим URL браузер лишається на /mobile-phones/ — наступний тест
        // здається, ніби "одразу відкрив мобілки", поки не виконається новий open(...).
        // З Selenide 7.17: cookies()/localStorage() замість deprecated clearBrowser*().
        cookies().clear();
        localStorage().clear();
        sessionStorage().clear();
        open("about:blank");
    }

    @AfterAll
    static void tearDownBrowser() {
        closeWebDriver();
    }

    @Test
    @Story("Перша сторінка каталогу та лейбл ТОП ПРОДАЖІВ (через меню Каталог)")
    @Description("""
            Відкриває rozetka.com.ua (десктоп), переходить у Телефони через меню Каталог (без прямого URL),
            збирає назви й ціни з першої сторінки та окремо товари з лейблом «ТОП ПРОДАЖІВ».
            """)
    @DisplayName("Телефони через меню Каталог: назви/ціни з 1-ї сторінки та ТОП ПРОДАЖІВ")
    void mobilePhonesFirstPageAndTopSales() {
        // Given
        HomePage homePage = HomePage.openHome();

        // When
        MobilePhonesPage phonesPage = homePage.openMobilePhonesViaCatalog();

        // Then
        assertFirstPageProductsAndTopSales(phonesPage);
    }

    @Test
    @Story("Перша сторінка каталогу та лейбл ТОП ПРОДАЖІВ (прямий URL)")
    @Description("""
            Запасний шлях: відкриває сторінку телефонів за прямим URL
            (https://rozetka.com.ua/ua/mobile-phones/c80003/), без меню Каталог.
            Перевіряє лише збір назв/цін і ТОП ПРОДАЖІВ — зручно, коли hover/антибот ламає навігацію.
            """)
    @DisplayName("Телефони за прямим URL: назви/ціни з 1-ї сторінки та ТОП ПРОДАЖІВ")
    void mobilePhonesViaDirectUrl() {
        // Given
        // (окремої головної немає — відкриваємо каталог напряму)

        // When
        MobilePhonesPage phonesPage = MobilePhonesPage.openDirect();

        // Then
        assertFirstPageProductsAndTopSales(phonesPage);
    }

    /** Спільні кроки завдання: списки з першої сторінки + ТОП ПРОДАЖІВ. */
    private void assertFirstPageProductsAndTopSales(MobilePhonesPage phonesPage) {
        List<Product> allPhones = phonesPage.getAllProductsOnFirstPage();
        List<Product> topSales = phonesPage.getTopSalesFrom(allPhones);

        phonesPage
                .attachProducts("Усі телефони на 1-й сторінці", allPhones)
                .attachProducts("ТОП ПРОДАЖІВ", topSales);

        assertThat(allPhones)
                .as("Очікували телефони на першій сторінці")
                .isNotEmpty()
                .allSatisfy(product -> assertThat(product.name())
                        .as("У кожного телефону має бути назва")
                        .isNotBlank());

        assertThat(topSales)
                .as("Очікували телефони з лейблом ТОП ПРОДАЖІВ")
                .isNotEmpty();
    }
}
