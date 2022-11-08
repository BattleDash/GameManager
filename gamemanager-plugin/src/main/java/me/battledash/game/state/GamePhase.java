package me.battledash.game.state;

import lombok.Getter;
import me.battledash.game.GameManager;
import me.battledash.game.game.GMPlayer;
import me.battledash.game.game.Game;
import me.battledash.game.packets.SendTitleMessage;
import me.battledash.game.redis.ProxyCommunicator;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public abstract class GamePhase extends BasePhase implements Listener {

    private final Plugin plugin;
    private final Game game;

    private final Set<Listener> listeners = new HashSet<>();
    private final Set<BukkitTask> tasks = new HashSet<>();

    public GamePhase(Game game) {
        this.game = game;
        this.plugin = game.getPlugin();
    }

    @Override
    public void start() {
        super.start();
        register(this);
    }

    @Override
    public final void end() {
        super.end();
        if (!isEnded()) return;
        listeners.forEach(HandlerList::unregisterAll);
        tasks.forEach(BukkitTask::cancel);
        listeners.clear();
        tasks.clear();
    }

    protected Collection<GMPlayer> getPlayers() {
        return game.getPlayers();
    }

    protected Collection<? extends Player> getBukkitPlayers() {
        return game.getPlayers().stream().map(GMPlayer::getPlayer).collect(Collectors.toSet());
    }

    protected void broadcast(String message) {
        getBukkitPlayers().forEach(p -> p.sendMessage(message));
    }

    protected void broadcastTitle(String message) {
        ProxyCommunicator communicator = game.getPool().getProxyCommunicator();

        UUID[] recipients = getBukkitPlayers().stream().map(Player::getUniqueId).toArray(UUID[]::new);
        SendTitleMessage titleMessage = new SendTitleMessage(message, null, Duration.ZERO, Duration.ofSeconds(2),
                Duration.ZERO, recipients);

        communicator.sendCommon(titleMessage);
    }

    protected void register(Listener listener) {
        listeners.add(listener);
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    protected void schedule(Runnable runnable, long delay) {
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay);
        tasks.add(task);
    }

    protected void scheduleRepeating(Runnable runnable, long delay, long interval) {
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, interval);
        tasks.add(task);
    }

}
