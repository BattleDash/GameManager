package me.battledash.game.pool;

import lombok.Getter;
import me.battledash.game.GameManager;
import me.battledash.game.game.Game;
import me.battledash.game.redis.ProxyCommunicator;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class GamePool {

    private static final String KILL_REASON_OVERPOPULATED = "There were too many game instances in the server. " +
            "This should never happen, please contact an administrator";

    private final List<Game> instances = new ArrayList<>();
    private final Function<GamePool, Game> gameSupplier;
    private final ProxyCommunicator proxyCommunicator;

    private final int poolSize;

    public GamePool(int count, Function<GamePool, Game> gameSupplier) {
        this.poolSize = count;
        this.gameSupplier = gameSupplier;
        this.proxyCommunicator = new ProxyCommunicator(this);

        populate();

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new JoinHandler(this), GameManager.getInstance());
    }

    public void populate() {
        if (instances.size() == poolSize) {
            return;
        }

        if (instances.size() > poolSize) {
            List<Game> extraGames = getRemovableGames(instances.size() - poolSize);
            extraGames.forEach(game -> game.kill(KILL_REASON_OVERPOPULATED));
            instances.removeAll(extraGames);
        }

        while (instances.size() < poolSize) {
            instances.add(initializeNewGame());
        }
    }

    private Game initializeNewGame() {
        Game game = gameSupplier.apply(this);
        game.createMainPhase().start();
        return game;
    }

    private List<Game> getRemovableGames(int count) {
        if (count > instances.size()) {
            count = instances.size();
        }

        return instances.stream()
                .sorted(Comparator.comparingInt(a -> a.getPlayers().size()))
                .limit(count).collect(Collectors.toList());
    }

    public void kill(String reason) {
        for (Game instance : instances) {
            instance.kill(reason);
        }
    }

}
