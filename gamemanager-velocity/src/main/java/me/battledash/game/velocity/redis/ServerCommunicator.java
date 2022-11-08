package me.battledash.game.velocity.redis;

import com.velocitypowered.api.proxy.Player;
import me.battledash.game.GameInstance;
import me.battledash.game.packets.PlayerLoginState;
import me.battledash.game.packets.JoinGameRequestMessage;
import me.battledash.game.packets.JoinGameResponseMessage;
import me.battledash.game.packets.SendTitleMessage;
import me.battledash.game.velocity.GameManager;
import net.battledash.sider.messages.SiderMessageChannel;
import net.battledash.sider.messages.SiderMessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.UUID;

public class ServerCommunicator {

    private final SiderMessageChannel commonChannel;
    private final SiderMessageChannel joinTicketingChannel;

    private final GameManager gameManager;

    public ServerCommunicator(GameManager gameManager) {
        this.gameManager = gameManager;

        SiderMessageManager messageManager = gameManager.getSider().getMessageManager();
        commonChannel = messageManager.getChannel("game_manager_common");
        joinTicketingChannel = messageManager.getChannel("game_manager_join_ticketing");

        commonChannel.listen(SendTitleMessage.class, SendTitleMessage::new, this::onTitleRequest);
    }

    public void sendGameJoinRequest(Player player, GameInstance game, PlayerLoginState state,
                                    SiderMessageChannel.MessageListener<JoinGameResponseMessage> callback) {
        JoinGameRequestMessage message = new JoinGameRequestMessage(player.getUniqueId(), game.getId(), state);
        joinTicketingChannel.send(message, JoinGameResponseMessage.class, JoinGameResponseMessage::new, callback,
                game.getServerInstance().getSiderId());
    }

    public void sendGameJoinRequest(Player player, GameInstance game,
                                    SiderMessageChannel.MessageListener<JoinGameResponseMessage> callback) {
        sendGameJoinRequest(player, game, new PlayerLoginState(), callback);
    }

    public void onTitleRequest(String senderSiderId, SendTitleMessage message) {
        for (UUID recipient : message.getRecipients()) {
            Player player = gameManager.getServer().getPlayer(recipient).orElse(null);

            if (player == null) {
                continue;
            }

            LegacyComponentSerializer legacySection = LegacyComponentSerializer.legacySection();
            TextComponent titleComponent = legacySection.deserializeOr(message.getTitle(), Component.empty());
            TextComponent subtitleComponent = legacySection.deserializeOr(message.getSubtitle(), Component.empty());

            Title.Times times = Title.Times.of(
                    Duration.ofMillis(message.getFadeIn()),
                    Duration.ofMillis(message.getStay()),
                    Duration.ofMillis(message.getFadeOut())
            );

            player.showTitle(Title.title(titleComponent, subtitleComponent, times));
        }
    }

}
