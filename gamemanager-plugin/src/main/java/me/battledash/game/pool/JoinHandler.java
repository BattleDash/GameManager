package me.battledash.game.pool;

import lombok.RequiredArgsConstructor;
import me.battledash.game.game.Game;
import me.battledash.game.game.JoinTicket;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

@RequiredArgsConstructor
public class JoinHandler implements Listener {

    private final GamePool pool;

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        for (Game game : pool.getInstances()) {
            JoinTicket ticket = game.useJoinTicket(playerId);
            if (ticket != null) {
                game.addPlayer(player, ticket.getState());
            }
        }
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        for (Game game : pool.getInstances()) {
            game.removePlayer(player);
        }
    }

}
