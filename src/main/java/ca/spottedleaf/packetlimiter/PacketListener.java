package ca.spottedleaf.packetlimiter;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class PacketListener extends PacketAdapter implements Listener {

    private static final DecimalFormat ONE_DECIMAL_PLACE = new DecimalFormat("0.0");
    private final ConcurrentHashMap<UUID, PlayerInfo> playerPacketInfo = new ConcurrentHashMap<>();

    private final String kickMessage;
    private final double maxPacketRate;
    private final double interval;

    public PacketListener(final PacketLimiter plugin, final String kickMessage, final double maxPacketRate, final double interval) {
        super(plugin, ListenerPriority.LOWEST, getPackets(), ListenerOptions.ASYNC, ListenerOptions.INTERCEPT_INPUT_BUFFER);
        // we want to listen at the earliest stage so we can reduce the overhead of packets going through other plugins,
        // as well as other plugin logic being executed for cancelled packets

        this.kickMessage = kickMessage;
        this.maxPacketRate = maxPacketRate;
        this.interval = interval;

        // ... support reload
        for (final Player player : Bukkit.getOnlinePlayers()) {
            this.playerPacketInfo.put(player.getUniqueId(), new PlayerInfo(new PacketBucket(this.interval, 150), player.getUniqueId()));
        }
    }

    private static List<PacketType> getPackets() {
        final List<PacketType> packets = new ArrayList<>();

        for (final PacketType type : PacketType.values()) {
            if (type.isClient() && type.getProtocol() == PacketType.Protocol.PLAY && type.isSupported()) {
                packets.add(type);
            }
        }

        return packets;
    }

    @Override
    public void onPacketReceiving(final PacketEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final UUID targetUniqueId = event.getPlayer().getUniqueId();
        PlayerInfo info = this.playerPacketInfo.get(targetUniqueId);

        if (info == null) {
            return;
        }

        final PacketBucket bucket = info.packets;

        synchronized (bucket) {
            final PlayerInfo currInfo = this.playerPacketInfo.get(targetUniqueId);

            if (currInfo != info) {
                return;
            }

            if (info.violatedLimit) {
                event.setCancelled(true);
                return;
            }

            final int packets = bucket.incrementPackets(1);

            if (bucket.getCurrentPacketRate() > this.maxPacketRate) {
                info.violatedLimit = true;
                event.setCancelled(true);

                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    final Player player = Bukkit.getPlayer(targetUniqueId);
                    if (player == null) {
                        return;
                    }

                    player.kickPlayer(this.kickMessage);
                    this.plugin.getLogger().log(Level.WARNING, "Player {0} ({1}) was kicked for sending too many packets! {2} in the last {3} seconds",
                            new Object[] {player.getName(), player.getUniqueId(), packets, ONE_DECIMAL_PLACE.format(bucket.intervalTime / 1000.0)});
                });
            }
        }
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final UUID player = event.getPlayer().getUniqueId();

        this.playerPacketInfo.put(player, new PlayerInfo(new PacketBucket(this.interval, 150), player));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        this.playerPacketInfo.remove(event.getPlayer().getUniqueId());
    }

    public static final class PlayerInfo {

        public final PacketBucket packets;
        public final UUID player;
        public boolean violatedLimit;

        public PlayerInfo(final PacketBucket packets, final UUID player) {
            this.packets = packets;
            this.player = player;
        }
    }
}