package se.xfunserver.xfunstocks.signs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.zankowski.iextrading4j.api.stocks.Quote;
import se.xfunserver.xfunstocks.config.Settings;
import se.xfunserver.xfunstocks.config.models.SignSettings;
import se.xfunserver.xfunstocks.inventories.InventoryManager;
import se.xfunserver.xfunstocks.signs.listeners.SignListener;
import se.xfunserver.xfunstocks.signs.models.QuoteSign;
import se.xfunserver.xfunstocks.stocks.StockManager;
import se.xfunserver.xfunstocks.utils.Utils;
import se.xfunserver.xfunstocks.xFunStocks;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class SignManager {

    private final xFunStocks plugin;
    private final StockManager stockManager;
    private final Settings settings;

    private List<QuoteSign> signs;

    public SignManager(xFunStocks plugin, Settings settings, StockManager stockManager, InventoryManager inventoryManager) {
        this.plugin = plugin;
        this.settings = settings;
        this.stockManager = stockManager;

        this.signs = loadAllSigns();

        plugin.getServer().getScheduler()
                        .runTaskTimer(plugin,
                                () -> {
                                    for (QuoteSign sign : getSigns()) {
                                        sign.update();
                                    }
                        }, 20 * 5, 20 * 5);

        plugin
                .getServer()
                .getPluginManager()
                .registerEvents(new SignListener(this, inventoryManager), plugin);
    }

    public List<QuoteSign> loadAllSigns() {

        FileConfiguration companiesReg = YamlConfiguration.loadConfiguration(new File(getPlugin().getDataFolder(), "signs.yml"));
        List<QuoteSign> companiesSignsToReturn = new ArrayList<>();

        if (companiesReg.getConfigurationSection("signs") == null) return Collections.emptyList();

        Set<Integer> companiesSignsIds = Objects.requireNonNull(companiesReg.getConfigurationSection("signs"))
                .getKeys(false).stream().map(Integer::parseInt).collect(Collectors.toSet());

        for (int companySignId : companiesSignsIds)
            companiesSignsToReturn.add(new QuoteSign(getPlugin(), companySignId).load());
        return companiesSignsToReturn;
    }

    public List<QuoteSign> getSigns() {
        return signs;
    }

    public void saveAll() {
        for (QuoteSign quoteSign : getSigns()) {
            quoteSign.save();
        }
    }

    public void registerStockSign(QuoteSign sign) {
        if (!signs.contains(sign))
            signs.add(sign);
    }

    public void deleteStockSign(QuoteSign sign) {
        FileConfiguration registration = YamlConfiguration.loadConfiguration(
                new File(getPlugin().getDataFolder(),
                        "signs.yml"));

        registration.set("signs." + sign.getId(), null);
        signs.remove(sign);

        saveFile(registration);
    }

    public boolean isStockSign(Sign sign) {
        if (sign == null || sign.getLines().length == 0) {
            return false;
        }

        return sign.getLine(0).equalsIgnoreCase(Utils.color("&3&lSKY STOCKS"));
    }

    public void saveFile(FileConfiguration configuration) {
        try {
            configuration.save(new File(getPlugin().getDataFolder(), "signs.yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Settings getSettings() {
        return settings;
    }

    public StockManager getStockManager() {
        return stockManager;
    }

    public xFunStocks getPlugin() {
        return plugin;
    }
}
