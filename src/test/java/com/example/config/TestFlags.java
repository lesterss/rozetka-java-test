package com.example.config;

import java.time.Duration;

/**
 * Прапорці запуску через -D... у Maven (див. README).
 */
public final class TestFlags {

    private TestFlags() {
    }

    /**
     * Ручний режим капчі: mvn test "-Dmanual.captcha=true"
     * Тест не обходить капчу — чекає, поки людина пройде її у вікні браузера.
     */
    public static final boolean MANUAL_CAPTCHA = Boolean.getBoolean("manual.captcha");

    public static final Duration MANUAL_CAPTCHA_TIMEOUT = Duration.ofMinutes(2);

    /**
     * Повільна мережа (CDP ≈ DevTools Slow 3G): mvn test "-Dslow.network=true"
     */
    public static final boolean SLOW_NETWORK = Boolean.getBoolean("slow.network");

    public static final int SLOW_NETWORK_LATENCY_MS = 400;

    public static final long SLOW_NETWORK_DOWNLOAD_BYTES_PER_SEC = 500L * 1024 / 8;

    public static final long SLOW_NETWORK_UPLOAD_BYTES_PER_SEC = 500L * 1024 / 8;

    public static long defaultTimeoutMs() {
        return SLOW_NETWORK ? 30_000L : 10_000L;
    }

    public static Duration catalogReadyTimeout() {
        return Duration.ofSeconds(SLOW_NETWORK ? 45 : 15);
    }
}
