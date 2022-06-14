package se.xfunserver.xfunstocks.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

@UtilityClass
public class Logger {
    private final String PREFIX = "xFunStocks -";

    public void severe(String message) {
        String logMessage = buildLogMessage(message);
        Bukkit.getLogger().severe(logMessage);
    }

    private String buildLogMessage(String message) {
        return String.format("%s %s", PREFIX, message);
    }
}