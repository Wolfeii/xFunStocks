package se.xfunserver.xfunstocks.config.models;

import lombok.Getter;
import se.xfunserver.xfunstocks.config.common.ConfigSection;

@Getter
public class SqlSettings {

    private final boolean enabled;
    private final String ip;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public SqlSettings(ConfigSection configSection) {
        this.enabled = configSection.getBoolean("enabled");
        this.ip = configSection.getString("ip");
        this.port = configSection.getInt("port");
        this.database = configSection.getString("database");
        this.username = configSection.getString("username");
        this.password = configSection.getString("password");
    }
}