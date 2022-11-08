package me.battledash.game.packets;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.battledash.sider.messages.SiderMessage;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class JoinGameResponseMessage extends SiderMessage {

    private GameTicketCode code;
    private UUID ticketId;

    public JoinGameResponseMessage(UUID nonce, GameTicketCode code, UUID ticketId) {
        super(nonce);
        this.code = code;
        this.ticketId = ticketId;
    }

}
