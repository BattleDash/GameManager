package me.battledash.game;

import lombok.Getter;
import lombok.Setter;
import me.battledash.game.example.ExampleGame;
import me.battledash.game.pool.GamePool;
import net.battledash.sider.Sider;
import net.battledash.sider.depot.SiderDepot;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static me.battledash.game.util.EnvUtil.getEnvOrDefault;

@Getter
public class GameManager extends JavaPlugin {

    private static final String KILL_REASON_SHUTDOWN = "Server shutdown";

    @Getter
    private static GameManager instance;

    private Sider sider;
    private ServerInstance serverInstance;
    private SiderDepot<GameInstance> gamesDepot;

    public void onLoad() {
        instance = this;

        sider = new Sider(
                getEnvOrDefault("REDIS_HOST", "localhost"),
                Integer.parseInt(getEnvOrDefault("REDIS_PORT", "6379"))
        );

        gamesDepot = sider.createDepot(GameInstance.class, "game_manager_games");
    }

    @Setter
    private GamePool pool;

    @Override
    public void onEnable() {
        try {
            String hostAddress = getEnvOrDefault("GAME_MANAGER_HOST", InetAddress.getLocalHost().getHostAddress());
            int port = getServer().getPort();
            serverInstance = new ServerInstance(hostAddress, port, sider.getSiderId());
        } catch (UnknownHostException e) {
            getLogger().info("Failed to retrieve server host address, " +
                    "consider setting GAME_MANAGER_HOST in env to the IP that the proxy should send players to.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }

        pool = new GamePool(1, pool -> new ExampleGame(this, pool));
    }

    @Override
    public void onDisable() {
        pool.kill(KILL_REASON_SHUTDOWN);
    }

}
