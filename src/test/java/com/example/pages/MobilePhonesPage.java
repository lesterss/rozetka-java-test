package com.example.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.example.model.Product;
import com.example.support.SiteBlockHandler;
import com.example.support.SlowNetwork;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.Wait;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;

/**
 * Перша сторінка каталогу мобільних телефонів.
 */
public class MobilePhonesPage {

    /** Прямий URL каталогу — стабільніший шлях, якщо меню Каталог перевіряти не потрібно. */
    public static final String DIRECT_URL = "https://rozetka.com.ua/ua/mobile-phones/c80003/";

    private final ElementsCollection productTiles = $$("rz-product-tile");

    /**
     * Відкриває сторінку телефонів напряму (без головної і без hover по Каталогу).
     * Зручно перевіряти збір назв/цін і ТОП ПРОДАЖІВ, коли меню нестабільне або сайт блокує навігацію.
     */
    @Step("Відкрити сторінку мобільних телефонів за прямим URL")
    public static MobilePhonesPage openDirect() {
        SlowNetwork.enableIfRequested();
        open(DIRECT_URL);
        return new MobilePhonesPage().waitUntilOpened("Відкриття сторінки телефонів за прямим URL");
    }

    @Step("Дочекатись відкриття сторінки мобільних телефонів")
    public MobilePhonesPage waitUntilOpened() {
        return waitUntilOpened("Відкриття сторінки телефонів через меню");
    }

    private MobilePhonesPage waitUntilOpened(String blockCheckStep) {
        webdriver().shouldHave(urlContaining("mobile-phones"));
        SiteBlockHandler.ensureNotBlocked(blockCheckStep, "rz-product-tile");
        productTiles.first().shouldBe(visible);
        SlowNetwork.disableIfEnabled();
        return this;
    }

    /**
     * Rozetka рендерить картки поступово — чекаємо, поки їхня кількість перестане змінюватись.
     */
    @Step("Зібрати назви й ціни всіх телефонів на першій сторінці")
    public List<Product> getAllProductsOnFirstPage() {
        ElementsCollection tiles = waitUntilTilesStable();
        return tiles.asFixedIterable().stream()
                .map(this::readProduct)
                .toList();
    }

    @Step("Відфільтрувати товари з лейблом ТОП ПРОДАЖІВ (Top Sales)")
    public List<Product> getTopSalesFrom(List<Product> allProducts) {
        return allProducts.stream()
                .filter(Product::topSales)
                .toList();
    }

    @Step("Додати список товарів у звіт: {title}")
    public MobilePhonesPage attachProducts(String title, List<Product> products) {
        String body = products.stream()
                .map(Product::toString)
                .collect(Collectors.joining(System.lineSeparator()));
        String report = "=== " + title + " (" + products.size() + ") ==="
                + System.lineSeparator() + body;
        System.out.println(report);
        Allure.addAttachment(title, "text/plain", report, ".txt");
        return this;
    }

    private ElementsCollection waitUntilTilesStable() {
        ElementsCollection tiles = productTiles.shouldHave(sizeGreaterThan(0));
        int[] previousSize = {-1};
        Wait().pollingEvery(Duration.ofSeconds(1))
                .withMessage("Кількість карток товарів не перестала змінюватись")
                .until(driver -> {
                    int currentSize = tiles.size();
                    boolean stable = currentSize == previousSize[0];
                    previousSize[0] = currentSize;
                    return stable;
                });
        return tiles;
    }

    private Product readProduct(SelenideElement tile) {
        String name = tile.$("a.tile-title").getText().trim();
        SelenideElement price = tile.$(".price");
        String priceText = price.exists() ? price.getText().trim() : "немає ціни";
        boolean topSales = tile.$(".promo-label_type_popularity").exists();
        return new Product(name, priceText, topSales);
    }
}
