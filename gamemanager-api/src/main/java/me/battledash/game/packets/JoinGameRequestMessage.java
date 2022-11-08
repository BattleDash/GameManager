package me.battledash.game.packets;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.battledash.sider.messages.SiderMessage;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JoinGameRequestMessage extends SiderMessage {

    private UUID playerId;
    private UUID gameId;
    private PlayerLoginState state;

}
