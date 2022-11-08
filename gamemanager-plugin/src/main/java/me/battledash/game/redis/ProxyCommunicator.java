package me.battledash.game.redis;

import me.battledash.game.game.Game;
import me.battledash.game.GameManager;
import me.battledash.game.pool.GamePool;
import me.battledash.game.game.JoinTicket;
import me.battledash.game.packets.GameTicketCode;
import me.battledash.game.packets.JoinGameRequestMessage;
import me.battledash.game.packets.JoinGameResponseMessage;
import net.battledash.sider.messages.SiderMessage;
import net.battledash.sider.messages.SiderMessageChannel;
import net.battledash.sider.messages.SiderMessageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ProxyCommunicator {

    private final GamePool pool;
    private final SiderMessageChannel commonChannel;
    private final SiderMessageChannel joinTicketingChannel;

    public ProxyCommunicator(GamePool pool) {
        this.pool = pool;

        SiderMessageManager messageManager = GameManager.getInstance().getSider().getMessageManager();
        commonChannel = messageManager.getChannel("game_manager_common");
        joinTicketingChannel = messageManager.getChannel("game_manager_join_ticketing");

        joinTicketingChannel.listen(JoinGameRequestMessage.class, JoinGameRequestMessage::new, this::onJoinRequest);
    }

    public <T extends SiderMessage> void sendCommon(T message) {
        commonChannel.send(message);
    }

    public void onJoinRequest(String senderSiderId, JoinGameRequestMessage message) {
        GameTicketCode code = GameTicketCode.INVALID_GAME;

        Game game = pool.getInstances().stream()
                .filter(g -> g.getId().equals(message.getGameId()))
                .findFirst().orElse(null);

        UUID playerId = message.getPlayerId();
        if (game != null) {
            code = game.requestJoinStatus(playerId);
        }

        UUID ticketId = null;

        if (code == GameTicketCode.SUCCESS) {
            try {
                JoinTicket ticket = game.issueJoinTicket(playerId, message.getState());
                ticketId = ticket.getId();
            } catch (IllegalArgumentException e) {
                code = GameTicketCode.ALREADY_ISSUED;
            }

            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                code = GameTicketCode.SUCCESS_NO_CONNECT;

                // Use the ticket directly, the player is already on this server
                JoinTicket ticket = game.useJoinTicket(playerId);
                if (ticket != null) {
                    game.addPlayer(player, ticket.getState());
                }
            }
        }

        joinTicketingChannel.send(new JoinGameResponseMessage(message.getNonce(), code, ticketId), senderSiderId);
    }

}
