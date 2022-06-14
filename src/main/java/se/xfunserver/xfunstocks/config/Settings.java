package se.xfunserver.xfunstocks.config;

import lombok.Getter;
import pl.zankowski.iextrading4j.client.IEXCloudClient;
import pl.zankowski.iextrading4j.client.IEXCloudTokenBuilder;
import pl.zankowski.iextrading4j.client.IEXTradingApiVersion;
import pl.zankowski.iextrading4j.client.IEXTradingClient;
import se.xfunserver.xfunstocks.config.common.ConfigSection;
import se.xfunserver.xfunstocks.config.models.SqlSettings;
import se.xfunserver.xfunstocks.xFunStocks;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Set;

public class Settings {

    private static final String DECIMAL_FORMAT = "#,##0.00";
    private static final String SINGLE_FORMAT = "0.#";

    private final ConfigSection configFile;
    @Getter
    private final SqlSettings sqlSettings;

    @Getter
    private final IEXCloudClient cloudClient;

    public Settings(xFunStocks stocks) {
        this.configFile = new ConfigSection(stocks, "config");
        this.sqlSettings = new SqlSettings(configFile.getSection("sql"));
        cloudClient = IEXTradingClient.create(IEXTradingApiVersion.IEX_CLOUD_STABLE,
                new IEXCloudTokenBuilder()
                        .withPublishableToken("pk_2647c42c28c04785b547b1bd21559ceb")
                        .build());
    }

    public Locale getLocale() {
        return configFile.getLocale("locale");
    }

    public int getCacheMinutes() {
        return configFile.getInt("cache.expire-minutes");
    }

    public String getUnknownData() {
        return configFile.getString("unknown-data");
    }

    public int getTransactionCooldownSeconds() {
        return configFile.getInt("transaction-cooldown-seconds");
    }

    public int getMinutesBetweenSale() {
        return configFile.getInt("minutes-between-sale");
    }

    public BigDecimal getPriceMultiplier() {
        return configFile.getBigDecimal("price-multiplier");
    }

    public boolean isAboveMinimumPrice(BigDecimal price) {
        return price.compareTo(configFile.getBigDecimal("minimum-price")) >= 0;
    }

    public boolean isAllowedCurrency(String currency) {
        return isMatch(currency, "allowed-currencies");
    }

    public boolean isAllowedExchange(String exchange) {
        return isMatch(exchange, "allowed-exchanges");
    }

    private boolean isMatch(String value, String path) {
        Set<String> configValues = configFile.getStringSet(path);
        if (configValues.isEmpty()) {
            return true;
        }

        return configValues.stream()
                .anyMatch(setValue -> setValue.equalsIgnoreCase(value));
    }

    public String format(BigDecimal input) {
        if (input == null) {
            return getUnknownData();
        }

        return new DecimalFormat(DECIMAL_FORMAT, DecimalFormatSymbols.getInstance(getLocale()))
                .format(input);
    }

    public String formatSingle(BigDecimal input) {
        if (input == null) {
            return getUnknownData();
        }

        return new DecimalFormat(SINGLE_FORMAT, DecimalFormatSymbols.getInstance(getLocale()))
                .format(input);
    }

    public String formatSigFig(BigDecimal input) {
        if (input == null) {
            return getUnknownData();
        }

        return formatSigFig(input.longValue());
    }

    public String formatSigFig(Long input) {
        if (input == null) {
            return getUnknownData();
        }

        String suffixes = "kmbt";
        if (input < 1000) {
            return String.valueOf(input).replace(".0", "");
        }

        int exponent = (int) (Math.log(input) / Math.log(1000));
        return String.format("%.1f%c", input / Math.pow(1000, exponent), suffixes.charAt(exponent - 1));
    }
}
