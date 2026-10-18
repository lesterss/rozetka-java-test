package com.example.support;

import com.example.config.TestFlags;
import io.qameta.allure.Step;
import org.openqa.selenium.chromium.ChromiumDriver;

import java.util.Map;

import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.webdriver;

/**
 * Емуляція повільного інтернету через Chrome DevTools Protocol (як F12 → Network → Slow 3G).
 * Увімкнення: -Dslow.network=true
 */
public final class SlowNetwork {

    private SlowNetwork() {
    }

    /**
     * CDP працює лише з уже запущеним Chrome — тому спочатку about:blank.
     */
    @Step("Увімкнути повільну мережу (CDP Slow 3G), якщо -Dslow.network=true")
    public static void enableIfRequested() {
        if (!TestFlags.SLOW_NETWORK) {
            return;
        }
        open("about:blank");
        ChromiumDriver driver = (ChromiumDriver) webdriver().driver().getWebDriver();
        driver.executeCdpCommand("Network.enable", Map.of());
        driver.executeCdpCommand("Network.emulateNetworkConditions", Map.of(
                "offline", false,
                "latency", TestFlags.SLOW_NETWORK_LATENCY_MS,
                "downloadThroughput", TestFlags.SLOW_NETWORK_DOWNLOAD_BYTES_PER_SEC,
                "uploadThroughput", TestFlags.SLOW_NETWORK_UPLOAD_BYTES_PER_SEC
        ));
        System.out.printf(
                ">>> Увімкнено повільну мережу (CDP): latency=%d ms, download≈%d Kbit/s%n",
                TestFlags.SLOW_NETWORK_LATENCY_MS,
                TestFlags.SLOW_NETWORK_DOWNLOAD_BYTES_PER_SEC * 8 / 1024);
    }

    @Step("Вимкнути повільну мережу (якщо була увімкнена)")
    public static void disableIfEnabled() {
        if (!TestFlags.SLOW_NETWORK) {
            return;
        }
        ChromiumDriver driver = (ChromiumDriver) webdriver().driver().getWebDriver();
        driver.executeCdpCommand("Network.emulateNetworkConditions", Map.of(
                "offline", false,
                "latency", 0,
                "downloadThroughput", -1,
                "uploadThroughput", -1
        ));
        System.out.println(">>> Повільну мережу вимкнено");
    }
}
