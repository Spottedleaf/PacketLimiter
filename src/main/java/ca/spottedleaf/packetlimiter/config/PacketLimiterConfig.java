package ca.spottedleaf.packetlimiter.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public final class PacketLimiterConfig {

    public static final int CURRENT_CONFIG_VERSION = 0;

    /** in seconds */
    public final double interval;

    /** in packets per second */
    public final double maxPacketRate;
    public final String kickMessage;

    public final int version;

    public PacketLimiterConfig(final FileConfiguration config) {
        this.interval = config.getDouble("interval"); // seconds
        this.maxPacketRate = config.getDouble("max-packet-rate"); // packets per second
        this.kickMessage = ChatColor.translateAlternateColorCodes('&', config.getString("kick-message"));

        this.version = config.getInt("config-version");

        if (this.version > CURRENT_CONFIG_VERSION) {
            throw new IllegalStateException("Trying to load newer config version! This plugin is for " + CURRENT_CONFIG_VERSION + ", but the config says " + this.version);
        }
    }
}