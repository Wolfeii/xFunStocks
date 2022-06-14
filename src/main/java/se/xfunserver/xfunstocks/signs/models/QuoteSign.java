package se.xfunserver.xfunstocks.signs.models;

import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;
import pl.zankowski.iextrading4j.api.stocks.Quote;
import se.xfunserver.xfunstocks.config.common.ConfigSection;
import se.xfunserver.xfunstocks.utils.Utils;
import se.xfunserver.xfunstocks.xFunStocks;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class QuoteSign {

    private final xFunStocks stockMarket;

    private int id;
    private String symbol;
    private Location location;

    public QuoteSign(xFunStocks stockMarket, int id, String symbol, Location location) {
        this.id = id;
        this.symbol = symbol;
        this.location = location;

        this.stockMarket = stockMarket;
    }

    public QuoteSign(xFunStocks stockMarket, String symbol, Location location) {
        this.stockMarket = stockMarket;

        ConfigSection signConfig = new ConfigSection(stockMarket, "signs");
        Optional<ConfigSection> signsSection = Optional.ofNullable(signConfig.getSection("signs"));
        AtomicInteger nextId = new AtomicInteger();

        signsSection.ifPresent(configSection -> {
            nextId.set(configSection.getKeys().size() + 1);
        });

        this.id = nextId.get();
        this.location = location;
        this.symbol = symbol;
    }

    public QuoteSign(xFunStocks plugin, int id) {
        this.stockMarket = plugin;
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public Sign toBukkitSign() {
        if (this.getLocation().getBlock().getState() instanceof Sign)
            return (Sign) this.getLocation().getBlock().getState();
        return null;
    }

    public void save() {
        File signsFile = new File(stockMarket.getDataFolder(), "signs.yml");
        try {
            if (!signsFile.exists()) {
                signsFile.createNewFile();
            }

            FileConfiguration signsConfig = YamlConfiguration.loadConfiguration(signsFile);

            signsConfig.set("signs." + this.getId() + ".symbol", this.symbol);
            signsConfig.set("signs." + this.getId() + ".location", this.getLocation());
            signsConfig.save(signsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public QuoteSign load() {
        File signsFile = new File(stockMarket.getDataFolder(), "signs.yml");
        try {
            if (!signsFile.exists()) {
                signsFile.createNewFile();
            }

            FileConfiguration signsConfig = YamlConfiguration.loadConfiguration(signsFile);

            this.location = signsConfig.getLocation("signs." + this.getId() + ".location");
            this.symbol = signsConfig.getString("signs." + this.getId() + ".symbol");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

    public void update() {
        Sign sign = this.toBukkitSign();
        Quote quote;

        try {
            quote = stockMarket.getStockManager().getStock(this.symbol);
        } catch (Exception e) {
            return;
        }

        if (quote == null || sign == null)
            return;

        String changePercent = Utils.color("&7" + stockMarket.getStockManager().getServerPrice(quote)) + " " +
                Utils.color((quote.getChangePercent().compareTo(BigDecimal.ZERO) > 0 ? "&a(+" : "&c(")
                         + quote.getChangePercent().setScale(2, RoundingMode.CEILING) + "%)"
                );

        sign.setLine(0, Utils.color("&3&lSKY STOCKS"));
        sign.setLine(1, Utils.color("&b" + quote.getSymbol()));
        sign.setLine(2, " ");
        sign.setLine(3, changePercent);

        sign.update(true);
    }
}
