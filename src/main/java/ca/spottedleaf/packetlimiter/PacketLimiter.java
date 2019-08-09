package ca.spottedleaf.packetlimiter;

import ca.spottedleaf.packetlimiter.config.PacketLimiterConfig;
import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class PacketLimiter extends JavaPlugin implements Listener {

    private PacketLimiterConfig config;

    private boolean loadFailed;
    private PacketListener packetListener;

    public PacketLimiterConfig getConfigData() {
        return this.config;
    }

    public PacketListener getPacketListener() {
        return this.packetListener;
    }

    @Override
    public void onLoad() {
        this.saveDefaultConfig();
        final FileConfiguration config = this.getConfig();
        this.config = new PacketLimiterConfig(config);

        if (this.config.interval <= 0.0 || this.config.maxPacketRate <= 0.0) {
            this.loadFailed = true;
            throw new IllegalStateException("Config is invalid! Options may not be negative");
        }

        final Logger logger = this.getLogger();

        logger.info("Packet sampling interval: " + this.config.interval + "s");
        logger.info("Max packet rate: " + this.config.maxPacketRate + "packets/s");
    }

    @Override
    public void onEnable() {
        if (this.loadFailed) {
            Bukkit.getScheduler().runTask(this, () -> {
                Bukkit.getPluginManager().disablePlugin(PacketLimiter.this);
            });
            return;
        }

        this.packetListener = new PacketListener(this, this.config.kickMessage, this.config.maxPacketRate, this.config.interval * 1000.0);

        ProtocolLibrary.getProtocolManager().addPacketListener(this.packetListener);
        Bukkit.getPluginManager().registerEvents(this.packetListener, this);
    }

    @Override
    public void onDisable() {
        if (this.packetListener != null) {
            ProtocolLibrary.getProtocolManager().removePacketListeners(this);
        }
    }
}