package org.ingestor.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "binance.rest")
public class BinanceRestProperties {

    /**
     * Binance Spot REST base URL.
     *
     * Default:
     * https://api.binance.com
     */
    private String baseUrl = "https://api.binance.com";
}
