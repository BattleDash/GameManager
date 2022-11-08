package me.battledash.game.example;

import me.battledash.game.game.GMPlayer;
import me.battledash.game.game.Game;
import me.battledash.game.pool.GamePool;
import me.battledash.game.state.BasePhase;
import me.battledash.game.state.GamePhase;
import me.battledash.game.state.PhaseMachine;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class ExampleGame extends Game {

    public ExampleGame(Plugin plugin, GamePool pool) {
        super(plugin, pool);
    }

    @Override
    public BasePhase createMainPhase() {
        PhaseMachine machine = new PhaseMachine(this);
        for (int i = 0; i < 50; i++) {
            machine.add(new PrintPhase(this, "Test " + i));
        }
        return machine;
    }

    static class PrintPhase extends GamePhase {
        private final String toPrint;

        public PrintPhase(Game game, String toPrint) {
            super(game);
            ChatColor[] values = ChatColor.values();
            this.toPrint = values[ThreadLocalRandom.current().nextInt(values.length)] + toPrint;
        }

        @Override
        protected void onStart() {
            getPlugin().getLogger().info(toPrint);
            broadcast(toPrint);
            broadcastTitle(toPrint);
        }

        @Override
        public Duration getDuration() {
            return Duration.ofMillis(500);
        }
    }

}
